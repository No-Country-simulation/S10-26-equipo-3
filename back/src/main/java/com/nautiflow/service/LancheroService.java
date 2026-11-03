package com.nautiflow.service;

import com.nautiflow.dto.RegistroLancheroResponse;
import com.nautiflow.dto.LancheroRegistroDTO;
import com.nautiflow.exception.ErrorRegistroException;
import com.nautiflow.exception.SolicitudInvalidaException;
import com.nautiflow.service.ArchivoLancheroStorage.ArchivoAlmacenado;
import com.nautiflow.service.ArchivoLancheroStorage.TipoArchivo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;

@Service
public class LancheroService {
    private static final Logger LOGGER = LoggerFactory.getLogger(LancheroService.class);

    private final LancheroPersistenceService persistenceService;
    private final ArchivoLancheroStorage archivoStorage;
    private final Executor uploadExecutor;

    public LancheroService(
            LancheroPersistenceService persistenceService,
            ArchivoLancheroStorage archivoStorage,
            @Qualifier("registroLancheroUploadExecutor") Executor uploadExecutor
    ) {
        this.persistenceService = persistenceService;
        this.archivoStorage = archivoStorage;
        this.uploadExecutor = uploadExecutor;
    }

    public RegistroLancheroResponse registrarLanchero(
            LancheroRegistroDTO solicitud,
            MultipartFile licencia,
            MultipartFile seguro,
            MultipartFile antecedentes,
            List<MultipartFile> fotos
    ) {
        long inicioRegistro = System.nanoTime();
        try {
            List<MultipartFile> fotosRecibidas = fotos == null ? List.of() : fotos;
            if (fotosRecibidas.size() > 5) {
                throw new SolicitudInvalidaException("Se permiten como máximo 5 fotos.");
            }

            long inicioValidacionCorreo = System.nanoTime();
            try {
                persistenceService.validarCorreoDisponible(solicitud.correo());
            } finally {
                LOGGER.info("Validación inicial de correo: {} ms", milisegundosDesde(inicioValidacionCorreo));
            }

            List<String> rutas = new ArrayList<>();
            RegistroLancheroResponse respuesta;
            String etapa = "validación y subida de documentos";
            try {
                List<SubidaPendiente> archivos = new ArrayList<>();
                archivos.add(new SubidaPendiente("licencia", licencia, TipoArchivo.DOCUMENTO));
                archivos.add(new SubidaPendiente("seguro", seguro, TipoArchivo.DOCUMENTO));
                archivos.add(new SubidaPendiente("antecedentes", antecedentes, TipoArchivo.DOCUMENTO));
                long inicioProcesamientoFotos = System.nanoTime();
                try {
                    for (int indice = 0; indice < fotosRecibidas.size(); indice++) {
                        MultipartFile foto = fotosRecibidas.get(indice);
                        LOGGER.info(
                                "Procesando foto de registro: indice={}, contentType={}, sizeBytes={}",
                                indice + 1,
                                foto.getContentType(),
                                foto.getSize()
                        );
                        etapa = "conversión/subida de foto " + (indice + 1);
                        archivos.add(new SubidaPendiente(
                                "foto-" + (indice + 1),
                                foto,
                                TipoArchivo.FOTO
                        ));
                    }
                } finally {
                    LOGGER.info("Procesamiento total de fotografías (cantidad={}): {} ms",
                            fotosRecibidas.size(), milisegundosDesde(inicioProcesamientoFotos));
                }

                etapa = "subida de documentos y fotos";
                List<ArchivoAlmacenado> archivosGuardados = guardarArchivos(archivos, rutas);
                ArchivoAlmacenado archivoLicencia = archivosGuardados.get(0);
                ArchivoAlmacenado archivoSeguro = archivosGuardados.get(1);
                ArchivoAlmacenado archivoAntecedentes = archivosGuardados.get(2);
                List<String> rutasFotos = archivosGuardados.subList(3, archivosGuardados.size()).stream()
                        .map(ArchivoAlmacenado::clave)
                        .toList();

                etapa = "persistencia de lanchero, documentos y fotos";
                long inicioPersistencia = System.nanoTime();
                try {
                    respuesta = persistenceService.registrar(
                            solicitud,
                            archivoLicencia.clave(),
                            archivoLicencia.tipoContenido(),
                            archivoSeguro.clave(),
                            archivoSeguro.tipoContenido(),
                            archivoAntecedentes.clave(),
                            archivoAntecedentes.tipoContenido(),
                            rutasFotos
                    );
                } finally {
                    LOGGER.info("Invocación de persistencia PostgreSQL: {} ms",
                            milisegundosDesde(inicioPersistencia));
                }
            } catch (RuntimeException | IOException exception) {
                LOGGER.error(
                        "Falló el registro de lanchero durante '{}'; fotosRecibidas={}, objetosSubidosAntesDelError={}",
                        etapa,
                        fotosRecibidas.size(),
                        rutas.size(),
                        exception
                );
                long inicioLimpieza = System.nanoTime();
                try {
                    archivoStorage.eliminar(rutas);
                } catch (RuntimeException cleanupException) {
                    exception.addSuppressed(cleanupException);
                } finally {
                    LOGGER.info("Limpieza de archivos en Cloudflare R2 tras error: {} ms",
                            milisegundosDesde(inicioLimpieza));
                }
                if (exception instanceof RuntimeException runtimeException) {
                    throw runtimeException;
                }
                throw new ErrorRegistroException("No se pudieron guardar los archivos.", exception);
            }

            return respuesta;
        } finally {
            LOGGER.info("Tiempo total registro de lanchero: {} ms", milisegundosDesde(inicioRegistro));
        }
    }

    private ArchivoLancheroStorage.ArchivoAlmacenado guardarArchivoMedido(
            String nombreArchivo,
            MultipartFile archivo,
            TipoArchivo tipo
    ) throws IOException {
        long inicio = System.nanoTime();
        try {
            return archivoStorage.guardar(archivo, tipo, nombreArchivo);
        } finally {
            LOGGER.info("Tiempo total de procesamiento y subida del archivo {} (tipo={}): {} ms",
                    nombreArchivo, tipo, milisegundosDesde(inicio));
        }
    }

    private List<ArchivoAlmacenado> guardarArchivos(
            List<SubidaPendiente> archivos,
            List<String> rutas
    ) throws IOException {
        List<CompletableFuture<ArchivoAlmacenado>> tareas = new ArrayList<>();
        RuntimeException errorEnvio = null;
        for (SubidaPendiente archivo : archivos) {
            try {
                tareas.add(CompletableFuture.supplyAsync(() -> {
                    try {
                        return guardarArchivoMedido(archivo.nombre(), archivo.archivo(), archivo.tipo());
                    } catch (IOException exception) {
                        throw new CompletionException(exception);
                    }
                }, uploadExecutor));
            } catch (RuntimeException exception) {
                errorEnvio = exception;
                break;
            }
        }

        CompletableFuture.allOf(tareas.toArray(CompletableFuture[]::new))
                .handle((resultado, exception) -> null)
                .join();

        List<ArchivoAlmacenado> guardados = new ArrayList<>();
        Throwable errorSubida = errorEnvio;
        for (CompletableFuture<ArchivoAlmacenado> tarea : tareas) {
            try {
                ArchivoAlmacenado archivoGuardado = tarea.join();
                guardados.add(archivoGuardado);
                rutas.add(archivoGuardado.clave());
            } catch (CompletionException exception) {
                Throwable causa = causaRaiz(exception);
                if (errorSubida == null) {
                    errorSubida = causa;
                } else if (errorSubida != causa) {
                    errorSubida.addSuppressed(causa);
                }
            }
        }

        if (errorSubida instanceof IOException exception) {
            throw exception;
        }
        if (errorSubida instanceof RuntimeException exception) {
            throw exception;
        }
        if (errorSubida instanceof Error error) {
            throw error;
        }
        if (errorSubida != null) {
            throw new IllegalStateException("No se pudieron completar las subidas de archivos.", errorSubida);
        }
        return guardados;
    }

    private Throwable causaRaiz(CompletionException exception) {
        Throwable causa = exception;
        while (causa instanceof CompletionException && causa.getCause() != null) {
            causa = causa.getCause();
        }
        return causa;
    }

    private long milisegundosDesde(long inicioNano) {
        return (System.nanoTime() - inicioNano) / 1_000_000;
    }

    private record SubidaPendiente(String nombre, MultipartFile archivo, TipoArchivo tipo) {
    }

}
