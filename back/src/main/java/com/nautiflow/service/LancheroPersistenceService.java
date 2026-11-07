package com.nautiflow.service;

import com.nautiflow.dto.LancheroRegistroDTO;
import com.nautiflow.dto.RegistroLancheroResponse;
import com.nautiflow.exception.ConflictoRegistroException;
import com.nautiflow.exception.SolicitudInvalidaException;
import com.nautiflow.model.Documento;
import com.nautiflow.model.Lancha;
import com.nautiflow.model.Lanchero;
import com.nautiflow.model.Zona;
import com.nautiflow.repository.DocumentoRepository;
import com.nautiflow.repository.LancheroRepository;
import com.nautiflow.repository.LanchaRepository;
import com.nautiflow.repository.RegistroEmailOutboxRepository;
import com.nautiflow.repository.ZonaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Servicio transaccional que persiste el lanchero, su lancha, sus documentos y la tarea de correo pendiente.
 */
@Service
public class LancheroPersistenceService {
    private static final Logger LOGGER = LoggerFactory.getLogger(LancheroPersistenceService.class);

    private final LancheroRepository lancheroRepository;
    private final LanchaRepository lanchaRepository;
    private final DocumentoRepository documentoRepository;
    private final RegistroEmailOutboxRepository registroEmailOutboxRepository;
    private final ZonaRepository zonaRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Inyecta los repositorios, el codificador de contraseñas y el outbox de correos.
     */
    public LancheroPersistenceService(
            LancheroRepository lancheroRepository,
            LanchaRepository lanchaRepository,
            DocumentoRepository documentoRepository,
            RegistroEmailOutboxRepository registroEmailOutboxRepository,
            ZonaRepository zonaRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.lancheroRepository = lancheroRepository;
        this.lanchaRepository = lanchaRepository;
        this.documentoRepository = documentoRepository;
        this.registroEmailOutboxRepository = registroEmailOutboxRepository;
        this.zonaRepository = zonaRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Guarda en una sola transacción el lanchero, su lancha, sus documentos y el correo pendiente en el outbox.
     */
    @Transactional
    public RegistroLancheroResponse registrar(
            LancheroRegistroDTO solicitud,
            String rutaLicencia,
            String tipoContenidoLicencia,
            String rutaSeguro,
            String tipoContenidoSeguro,
            String rutaAntecedentes,
            String tipoContenidoAntecedentes,
            List<String> rutasFotos
    ) {
        long inicioPersistencia = System.nanoTime();
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCompletion(int status) {
                    String resultado = status == STATUS_COMMITTED
                            ? "confirmada"
                            : status == STATUS_ROLLED_BACK ? "revertida" : "desconocido";
                    LOGGER.info("Persistencia PostgreSQL y finalización de transacción: {} ms (resultado={})",
                            milisegundosDesde(inicioPersistencia), resultado);
                }
            });
        }

        List<Long> zonasIds = solicitud.zonasIds() == null ? List.of() : solicitud.zonasIds();
        if (zonasIds.stream().anyMatch(id -> id == null || id <= 0)) {
            throw new SolicitudInvalidaException("Los IDs de zonas deben ser positivos.");
        }
        if (new HashSet<>(zonasIds).size() != zonasIds.size()) {
            throw new SolicitudInvalidaException("No se permiten IDs de zonas duplicados.");
        }
        List<Zona> zonasEncontradas = zonaRepository.findAllById(zonasIds);
        if (zonasEncontradas.size() != zonasIds.size()) {
            throw new SolicitudInvalidaException("Una o más zonas seleccionadas no existen.");
        }
        Map<Long, Zona> zonasPorId = zonasEncontradas.stream()
                .collect(Collectors.toMap(Zona::getIdZona, zona -> zona));
        List<Zona> zonas = zonasIds.stream().map(zonasPorId::get).toList();

        validarCorreoDisponible(solicitud.correo());

        String nombreCompleto = solicitud.nombreCompleto().strip();
        if (nombreCompleto.length() > 201) {
            throw new SolicitudInvalidaException(
                    "El nombre completo no puede superar 201 caracteres."
            );
        }

        Lanchero lanchero = new Lanchero();
        lanchero.setNombreCompleto(nombreCompleto);
        lanchero.setWhatsapp(solicitud.whatsapp().strip());
        lanchero.setCorreo(solicitud.correo().strip());
        String contrasena = solicitud.contrasena();
        if (contrasena == null || contrasena.isBlank()) {
            throw new SolicitudInvalidaException("La contraseña es obligatoria.");
        }
        lanchero.setPasswordHash(passwordEncoder.encode(contrasena));
        lanchero = lancheroRepository.save(lanchero);

        Lancha lancha = new Lancha();
        lancha.setLanchero(lanchero);
        lanchero.setLancha(lancha);
        lancha.setNombre(solicitud.nombreEmbarcacion().strip());
        lancha.setCapacidad(solicitud.capacidadPasajeros());
        lancha.setZonas(new HashSet<>(zonas));
        lancha = lanchaRepository.save(lancha);

        guardarDocumento(lanchero, "LICENCIA", rutaLicencia, tipoContenidoLicencia);
        guardarDocumento(lanchero, "SEGURO", rutaSeguro, tipoContenidoSeguro);
        guardarDocumento(lanchero, "ANTECEDENTES", rutaAntecedentes, tipoContenidoAntecedentes);

        for (String ruta : rutasFotos) {
            guardarDocumento(lanchero, "FOTO_LANCHA", ruta, "image/webp");
        }
        registroEmailOutboxRepository.encolar(lanchero.getIdLanchero(), lanchero.getCorreo());

        return new RegistroLancheroResponse(
                lanchero.getIdLanchero(),
                lancha.getIdLancha(),
                lanchero.getEstadoSolicitud().name(),
                "Solicitud de registro recibida correctamente."
        );
    }

    /**
     * Comprueba que no exista otro lanchero con el mismo correo.
     */
    public void validarCorreoDisponible(String correo) {
        if (lancheroRepository.existsByCorreo(correo.strip())) {
            throw new ConflictoRegistroException("Ya existe un lanchero con ese correo electrónico.");
        }
    }

    /**
     * Crea y guarda el metadato de un documento asociado al lanchero.
     */
    private void guardarDocumento(Lanchero lanchero, String tipo, String clave, String tipoContenido) {
        Documento documento = new Documento();
        documento.setLanchero(lanchero);
        documento.setTipoDocumento(tipo);
        documento.setClaveArchivo(clave);
        documento.setTipoContenido(tipoContenido);
        documentoRepository.save(documento);
    }

    /**
     * Calcula los milisegundos transcurridos desde un instante en nanosegundos.
     */
    private long milisegundosDesde(long inicioNano) {
        return (System.nanoTime() - inicioNano) / 1_000_000;
    }
}
