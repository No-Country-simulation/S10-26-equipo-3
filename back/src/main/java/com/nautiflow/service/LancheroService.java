package com.nautiflow.service;

import com.nautiflow.dto.RegistroLancheroResponse;
import com.nautiflow.dto.LancheroRegistroDTO;
import com.nautiflow.exception.ErrorRegistroException;
import com.nautiflow.exception.SolicitudInvalidaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class LancheroService {
    private static final Logger LOGGER = LoggerFactory.getLogger(LancheroService.class);

    private final LancheroPersistenceService persistenceService;
    private final ArchivoLancheroStorage archivoStorage;
    private final RegistroNotificacionService notificacionService;

    public LancheroService(
            LancheroPersistenceService persistenceService,
            ArchivoLancheroStorage archivoStorage,
            RegistroNotificacionService notificacionService
    ) {
        this.persistenceService = persistenceService;
        this.archivoStorage = archivoStorage;
        this.notificacionService = notificacionService;
    }

    public RegistroLancheroResponse registrarLanchero(
            LancheroRegistroDTO solicitud,
            MultipartFile licencia,
            MultipartFile seguro,
            MultipartFile antecedentes,
            List<MultipartFile> fotos
    ) {
        List<MultipartFile> fotosRecibidas = fotos == null ? List.of() : fotos;
        if (fotosRecibidas.size() > 5) {
            throw new SolicitudInvalidaException("Se permiten como máximo 5 fotos.");
        }

        List<String> rutas = new ArrayList<>();
        RegistroLancheroResponse respuesta;
        String etapa = "validación y subida de documentos";
        try {
            ArchivoLancheroStorage.ArchivoAlmacenado archivoLicencia =
                    archivoStorage.guardar(licencia, ArchivoLancheroStorage.TipoArchivo.DOCUMENTO);
            rutas.add(archivoLicencia.clave());
            ArchivoLancheroStorage.ArchivoAlmacenado archivoSeguro =
                    archivoStorage.guardar(seguro, ArchivoLancheroStorage.TipoArchivo.DOCUMENTO);
            rutas.add(archivoSeguro.clave());
            ArchivoLancheroStorage.ArchivoAlmacenado archivoAntecedentes =
                    archivoStorage.guardar(antecedentes, ArchivoLancheroStorage.TipoArchivo.DOCUMENTO);
            rutas.add(archivoAntecedentes.clave());

            List<String> rutasFotos = new ArrayList<>();
            for (int indice = 0; indice < fotosRecibidas.size(); indice++) {
                MultipartFile foto = fotosRecibidas.get(indice);
                LOGGER.info(
                        "Procesando foto de registro: indice={}, contentType={}, sizeBytes={}",
                        indice + 1,
                        foto.getContentType(),
                        foto.getSize()
                );
                etapa = "conversión/subida de foto " + (indice + 1);
                ArchivoLancheroStorage.ArchivoAlmacenado archivoFoto =
                        archivoStorage.guardar(foto, ArchivoLancheroStorage.TipoArchivo.FOTO);
                rutas.add(archivoFoto.clave());
                rutasFotos.add(archivoFoto.clave());
            }

            etapa = "persistencia de lanchero, documentos y fotos";
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
        } catch (RuntimeException | IOException exception) {
            LOGGER.error(
                    "Falló el registro de lanchero durante '{}'; fotosRecibidas={}, objetosSubidosAntesDelError={}",
                    etapa,
                    fotosRecibidas.size(),
                    rutas.size(),
                    exception
            );
            try {
                archivoStorage.eliminar(rutas);
            } catch (RuntimeException cleanupException) {
                exception.addSuppressed(cleanupException);
            }
            if (exception instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            throw new ErrorRegistroException("No se pudieron guardar los archivos.", exception);
        }

        notificacionService.enviarConfirmacionPorCorreo(respuesta.idLanchero(), solicitud.correo());
        return respuesta;
    }

}
