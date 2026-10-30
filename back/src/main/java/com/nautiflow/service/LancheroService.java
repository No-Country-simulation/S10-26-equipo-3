package com.nautiflow.service;

import com.nautiflow.dto.LancheroRegistroDTO;
import com.nautiflow.exception.ErrorRegistroException;
import com.nautiflow.exception.SolicitudInvalidaException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class LancheroService {
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

    public RegistroRespuesta registrarLanchero(
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
        RegistroRespuesta respuesta;
        try {
            String rutaLicencia = archivoStorage.guardar(licencia, ArchivoLancheroStorage.TipoArchivo.DOCUMENTO);
            rutas.add(rutaLicencia);
            String rutaSeguro = archivoStorage.guardar(seguro, ArchivoLancheroStorage.TipoArchivo.DOCUMENTO);
            rutas.add(rutaSeguro);
            String rutaAntecedentes = archivoStorage.guardar(antecedentes, ArchivoLancheroStorage.TipoArchivo.DOCUMENTO);
            rutas.add(rutaAntecedentes);

            List<String> rutasFotos = new ArrayList<>();
            for (MultipartFile foto : fotosRecibidas) {
                String ruta = archivoStorage.guardar(foto, ArchivoLancheroStorage.TipoArchivo.FOTO);
                rutas.add(ruta);
                rutasFotos.add(ruta);
            }

            respuesta = persistenceService.registrar(solicitud, rutaLicencia, rutaSeguro, rutaAntecedentes, rutasFotos);
        } catch (RuntimeException | IOException exception) {
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

    public record RegistroRespuesta(Long idLanchero, Long idLancha, String estado, String mensaje) {
    }
}
