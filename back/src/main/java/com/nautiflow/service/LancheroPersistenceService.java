package com.nautiflow.service;

import com.nautiflow.dto.LancheroRegistroDTO;
import com.nautiflow.exception.ConflictoRegistroException;
import com.nautiflow.exception.SolicitudInvalidaException;
import com.nautiflow.model.Documento;
import com.nautiflow.model.Lancha;
import com.nautiflow.model.Lanchero;
import com.nautiflow.repository.DocumentoRepository;
import com.nautiflow.repository.LancheroRepository;
import com.nautiflow.repository.LanchaRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class LancheroPersistenceService {
    private final LancheroRepository lancheroRepository;
    private final LanchaRepository lanchaRepository;
    private final DocumentoRepository documentoRepository;
    private final PasswordEncoder passwordEncoder;

    public LancheroPersistenceService(
            LancheroRepository lancheroRepository,
            LanchaRepository lanchaRepository,
            DocumentoRepository documentoRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.lancheroRepository = lancheroRepository;
        this.lanchaRepository = lanchaRepository;
        this.documentoRepository = documentoRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public LancheroService.RegistroRespuesta registrar(
            LancheroRegistroDTO solicitud,
            String rutaLicencia,
            String rutaSeguro,
            String rutaAntecedentes,
            List<String> rutasFotos
    ) {
        if (lancheroRepository.existsByDocumentoIdentidad(solicitud.documentoIdentidad())) {
                throw new ConflictoRegistroException("Ya existe un lanchero con ese documento de identidad.");
        }
        if (lancheroRepository.existsByCorreo(solicitud.correo())) {
                throw new ConflictoRegistroException("Ya existe un lanchero con ese correo electrónico.");
        }

        String[] nombre = solicitud.nombreCompleto().strip().split("\\s+", 2);
        if (nombre.length != 2 || nombre[0].length() > 100 || nombre[1].length() > 100) {
            throw new SolicitudInvalidaException(
                    "El nombre completo debe incluir nombre y apellido, con un máximo de 100 caracteres cada uno."
            );
        }

        Lanchero lanchero = new Lanchero();
        lanchero.setNombre(nombre[0]);
        lanchero.setApellido(nombre[1]);
        lanchero.setDocumentoIdentidad(solicitud.documentoIdentidad().strip());
        lanchero.setTelefono(solicitud.whatsapp().strip());
        lanchero.setCorreo(solicitud.correo().strip());
        lanchero.setPasswordHash(passwordEncoder.encode(solicitud.contrasena()));
        lanchero = lancheroRepository.save(lanchero);

        Lancha lancha = new Lancha();
        lancha.setLanchero(lanchero);
        lancha.setNombre(solicitud.nombreEmbarcacion().strip());
        lancha.setCapacidad(solicitud.capacidadPasajeros());
        lancha = lanchaRepository.save(lancha);

        guardarDocumento(lanchero, "LICENCIA", rutaLicencia);
        guardarDocumento(lanchero, "SEGURO", rutaSeguro);
        guardarDocumento(lanchero, "ANTECEDENTES", rutaAntecedentes);

        for (String ruta : rutasFotos) {
            guardarDocumento(lanchero, "FOTO_EMBARCACION", ruta);
        }

        return new LancheroService.RegistroRespuesta(
                lanchero.getIdLanchero(),
                lancha.getIdLancha(),
                lanchero.getEstado(),
                "Solicitud de registro recibida correctamente."
        );
    }

    private void guardarDocumento(Lanchero lanchero, String tipo, String ruta) {
        Documento documento = new Documento();
        documento.setLanchero(lanchero);
        documento.setTipoDocumento(tipo);
        documento.setRutaArchivo(ruta);
        documentoRepository.save(documento);
    }
}
