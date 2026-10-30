package com.nautiflow.exception.handler;

import com.nautiflow.exception.AlmacenamientoNoConfiguradoException;
import com.nautiflow.exception.ArchivoDemasiadoGrandeException;
import com.nautiflow.exception.ConflictoRegistroException;
import com.nautiflow.exception.ErrorAlmacenamientoException;
import com.nautiflow.exception.ErrorRegistroException;
import com.nautiflow.exception.SolicitudInvalidaException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import java.util.stream.Collectors;

@RestControllerAdvice
public class LancheroExceptionHandler {
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorRespuesta> manejarJsonInvalido(HttpMessageNotReadableException exception) {
        return ResponseEntity.badRequest()
                .body(new ErrorRespuesta("La parte 'solicitud' debe contener un JSON válido."));
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<ErrorRespuesta> manejarParteRequeridaAusente(MissingServletRequestPartException exception) {
        return ResponseEntity.badRequest()
                .body(new ErrorRespuesta("Falta una parte requerida en la solicitud: " + exception.getRequestPartName() + "."));
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorRespuesta> manejarTipoDeContenidoNoSoportado(
            HttpMediaTypeNotSupportedException exception
    ) {
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                .body(new ErrorRespuesta("El tipo de contenido de la solicitud no es compatible."));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorRespuesta> manejarValidacion(MethodArgumentNotValidException exception) {
        String mensaje = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return ResponseEntity.badRequest().body(new ErrorRespuesta(mensaje));
    }

    @ExceptionHandler(SolicitudInvalidaException.class)
    public ResponseEntity<ErrorRespuesta> manejarSolicitudInvalida(SolicitudInvalidaException exception) {
        return ResponseEntity.badRequest().body(new ErrorRespuesta(exception.getMessage()));
    }

    @ExceptionHandler(ArchivoDemasiadoGrandeException.class)
    public ResponseEntity<ErrorRespuesta> manejarArchivoDemasiadoGrande(ArchivoDemasiadoGrandeException exception) {
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                .body(new ErrorRespuesta(exception.getMessage()));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorRespuesta> manejarSolicitudDemasiadoGrande(MaxUploadSizeExceededException exception) {
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                .body(new ErrorRespuesta("La solicitud supera el tamaño máximo permitido."));
    }

    @ExceptionHandler(ConflictoRegistroException.class)
    public ResponseEntity<ErrorRespuesta> manejarConflictoDeRegistro(ConflictoRegistroException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorRespuesta(exception.getMessage()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorRespuesta> manejarConflictoDeDatos(DataIntegrityViolationException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorRespuesta("Los datos registrados entran en conflicto con un registro existente."));
    }

    @ExceptionHandler(ErrorRegistroException.class)
    public ResponseEntity<ErrorRespuesta> manejarErrorDeRegistro(ErrorRegistroException exception) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorRespuesta(exception.getMessage()));
    }

    @ExceptionHandler(ErrorAlmacenamientoException.class)
    public ResponseEntity<ErrorRespuesta> manejarErrorDeAlmacenamiento(ErrorAlmacenamientoException exception) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(new ErrorRespuesta(exception.getMessage()));
    }

    @ExceptionHandler(AlmacenamientoNoConfiguradoException.class)
    public ResponseEntity<ErrorRespuesta> manejarAlmacenamientoNoConfigurado(
            AlmacenamientoNoConfiguradoException exception
    ) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new ErrorRespuesta(exception.getMessage()));
    }

    public record ErrorRespuesta(String mensaje) {
    }
}
