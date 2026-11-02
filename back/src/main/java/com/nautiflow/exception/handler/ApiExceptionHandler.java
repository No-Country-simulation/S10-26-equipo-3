package com.nautiflow.exception.handler;

import com.nautiflow.exception.AlmacenamientoNoConfiguradoException;
import com.nautiflow.exception.ApiErrorResponse;
import com.nautiflow.exception.ArchivoDemasiadoGrandeException;
import com.nautiflow.exception.ConflictoRegistroException;
import com.nautiflow.exception.ErrorAlmacenamientoException;
import com.nautiflow.exception.ErrorRegistroException;
import com.nautiflow.exception.SolicitudInvalidaException;
import com.nautiflow.service.AdminAuthService.CredencialesInvalidasException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.Objects;
import java.util.stream.Collectors;

@RestControllerAdvice
public class ApiExceptionHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(CredencialesInvalidasException.class)
    public ResponseEntity<ApiErrorResponse> manejarCredencialesInvalidas(
            CredencialesInvalidasException exception,
            HttpServletRequest request
    ) {
        return responder(HttpStatus.UNAUTHORIZED, "El correo o la contraseña no son correctos.", request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> manejarValidacionDeCuerpo(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        String mensaje = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getDefaultMessage())
                .filter(Objects::nonNull)
                .collect(Collectors.joining("; "));
        if (mensaje.isBlank()) {
            mensaje = "La solicitud no cumple las validaciones requeridas.";
        }
        return responder(HttpStatus.BAD_REQUEST, mensaje, request);
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiErrorResponse> manejarErroresDeBinding(
            BindException exception,
            HttpServletRequest request
    ) {
        String mensaje = exception.getFieldErrors().stream()
                .map(error -> error.getDefaultMessage())
                .filter(Objects::nonNull)
                .collect(Collectors.joining("; "));
        return responder(
                HttpStatus.BAD_REQUEST,
                mensaje.isBlank() ? "Uno o más valores de la solicitud no son válidos." : mensaje,
                request
        );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> manejarRestriccionesDeMetodo(
            ConstraintViolationException exception,
            HttpServletRequest request
    ) {
        String mensaje = exception.getConstraintViolations().stream()
                .map(violacion -> violacion.getMessage())
                .collect(Collectors.joining("; "));
        return responder(HttpStatus.BAD_REQUEST, mensaje, request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> manejarCuerpoIlegible(
            HttpMessageNotReadableException exception,
            HttpServletRequest request
    ) {
        return responder(
                HttpStatus.BAD_REQUEST,
                "El cuerpo de la solicitud está vacío, mal formado o no cumple el formato esperado.",
                request
        );
    }

    @ExceptionHandler({
            MissingServletRequestParameterException.class,
            MissingServletRequestPartException.class,
            MethodArgumentTypeMismatchException.class
    })
    public ResponseEntity<ApiErrorResponse> manejarParametroInvalido(
            Exception exception,
            HttpServletRequest request
    ) {
        return responder(
                HttpStatus.BAD_REQUEST,
                "Falta un parámetro requerido o uno de sus valores no es válido.",
                request
        );
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiErrorResponse> manejarTipoDeContenidoNoSoportado(
            HttpMediaTypeNotSupportedException exception,
            HttpServletRequest request
    ) {
        return responder(
                HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                "El tipo de contenido de la solicitud no es compatible con este endpoint.",
                request
        );
    }

    @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
    public ResponseEntity<ApiErrorResponse> manejarTipoDeRespuestaNoAceptable(
            HttpMediaTypeNotAcceptableException exception,
            HttpServletRequest request
    ) {
        return responder(
                HttpStatus.NOT_ACCEPTABLE,
                "El formato de respuesta solicitado no está disponible.",
                request
        );
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiErrorResponse> manejarMetodoNoSoportado(
            HttpRequestMethodNotSupportedException exception,
            HttpServletRequest request
    ) {
        return responder(
                HttpStatus.METHOD_NOT_ALLOWED,
                "El método HTTP no está permitido para este recurso.",
                request
        );
    }

    @ExceptionHandler({
            ArchivoDemasiadoGrandeException.class,
            MaxUploadSizeExceededException.class
    })
    public ResponseEntity<ApiErrorResponse> manejarCargaDemasiadoGrande(
            Exception exception,
            HttpServletRequest request
    ) {
        String mensaje = exception instanceof ArchivoDemasiadoGrandeException
                ? exception.getMessage()
                : "La solicitud supera el tamaño máximo permitido.";
        return responder(HttpStatus.PAYLOAD_TOO_LARGE, mensaje, request);
    }

    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<ApiErrorResponse> manejarMultipartInvalido(
            MultipartException exception,
            HttpServletRequest request
    ) {
        if (contieneLimiteExcedido(exception)) {
            return responder(
                    HttpStatus.PAYLOAD_TOO_LARGE,
                    "La solicitud supera el tamaño máximo permitido.",
                    request
            );
        }
        return responder(HttpStatus.BAD_REQUEST, "La solicitud multipart no es válida.", request);
    }

    @ExceptionHandler(SolicitudInvalidaException.class)
    public ResponseEntity<ApiErrorResponse> manejarSolicitudInvalida(
            SolicitudInvalidaException exception,
            HttpServletRequest request
    ) {
        return responder(HttpStatus.BAD_REQUEST, exception.getMessage(), request);
    }

    @ExceptionHandler(ConflictoRegistroException.class)
    public ResponseEntity<ApiErrorResponse> manejarConflictoDeRegistro(
            ConflictoRegistroException exception,
            HttpServletRequest request
    ) {
        return responder(HttpStatus.CONFLICT, exception.getMessage(), request);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse> manejarConflictoDeDatos(
            DataIntegrityViolationException exception,
            HttpServletRequest request
    ) {
        return responder(
                HttpStatus.CONFLICT,
                "La operación entra en conflicto con el estado o los datos existentes.",
                request
        );
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiErrorResponse> manejarEstadoHttp(
            ResponseStatusException exception,
            HttpServletRequest request
    ) {
        HttpStatus status = HttpStatus.resolve(exception.getStatusCode().value());
        if (status == null) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        String mensaje = status.is5xxServerError()
                ? "Ocurrió un error interno al procesar la solicitud."
                : Objects.requireNonNullElse(exception.getReason(), status.getReasonPhrase());
        return responder(status, mensaje, request);
    }

    @ExceptionHandler(AlmacenamientoNoConfiguradoException.class)
    public ResponseEntity<ApiErrorResponse> manejarAlmacenamientoNoConfigurado(
            AlmacenamientoNoConfiguradoException exception,
            HttpServletRequest request
    ) {
        return responder(
                HttpStatus.SERVICE_UNAVAILABLE,
                "El servicio de almacenamiento no está disponible.",
                request
        );
    }

    @ExceptionHandler(ErrorAlmacenamientoException.class)
    public ResponseEntity<ApiErrorResponse> manejarErrorDeAlmacenamiento(
            ErrorAlmacenamientoException exception,
            HttpServletRequest request
    ) {
        return responder(
                HttpStatus.BAD_GATEWAY,
                "No fue posible completar la operación de almacenamiento.",
                request
        );
    }

    @ExceptionHandler(ErrorRegistroException.class)
    public ResponseEntity<ApiErrorResponse> manejarErrorDeRegistro(
            ErrorRegistroException exception,
            HttpServletRequest request
    ) {
        return responder(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "No fue posible completar el registro.",
                request
        );
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiErrorResponse> manejarRecursoNoEncontrado(
            NoResourceFoundException exception,
            HttpServletRequest request
    ) {
        return responder(HttpStatus.NOT_FOUND, "El recurso solicitado no existe.", request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> manejarErrorInesperado(
            Exception exception,
            HttpServletRequest request
    ) {
        LOGGER.error("Error inesperado procesando {} {}.", request.getMethod(), request.getRequestURI(), exception);
        return responder(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Ocurrió un error interno al procesar la solicitud.",
                request
        );
    }

    private boolean contieneLimiteExcedido(Throwable exception) {
        Throwable causa = exception.getCause();
        while (causa != null) {
            if (causa instanceof MaxUploadSizeExceededException) {
                return true;
            }
            causa = causa.getCause();
        }
        return false;
    }

    private ResponseEntity<ApiErrorResponse> responder(
            HttpStatus status,
            String mensaje,
            HttpServletRequest request
    ) {
        return ResponseEntity.status(status)
                .body(ApiErrorResponse.of(status, mensaje, request.getRequestURI()));
    }
}
