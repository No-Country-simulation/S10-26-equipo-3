package com.nautiflow;

import com.nautiflow.repository.LanchaRepository;
import com.nautiflow.repository.LancheroRepository;
import com.nautiflow.repository.DocumentoRepository;
import com.nautiflow.repository.RegistroEmailOutboxRepository;
import com.nautiflow.repository.ResenaRepository;
import com.nautiflow.repository.ViajeRepository;
import com.nautiflow.repository.ZonaRepository;
import com.nautiflow.model.EstadoSolicitud;
import com.nautiflow.model.EstadoOperativoLanchero;
import com.nautiflow.model.Documento;
import com.nautiflow.model.Lanchero;
import com.nautiflow.model.Lancha;
import com.nautiflow.model.Resena;
import com.nautiflow.model.Viaje;
import com.nautiflow.model.Zona;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class LanchaRepositoryAvailabilityTest {
    @Autowired
    private LanchaRepository lanchaRepository;

    @Autowired
    private RegistroEmailOutboxRepository registroEmailOutboxRepository;

    @Autowired
    private DocumentoRepository documentoRepository;

    @Autowired
    private LancheroRepository lancheroRepository;

    @Autowired
    private ViajeRepository viajeRepository;

    @Autowired
    private ResenaRepository resenaRepository;

    @Autowired
    private ZonaRepository zonaRepository;

    @Test
    void availabilityQueryExecutesAgainstConfiguredDatabase() {
        OffsetDateTime fechaHoraConsulta = OffsetDateTime.now(ZoneOffset.UTC);
        assertNotNull(lanchaRepository.buscarDisponibles(
                null,
                null,
                null,
                EstadoSolicitud.APROBADO,
                EstadoOperativoLanchero.ACTIVO,
                fechaHoraConsulta
        ));
        assertNotNull(lanchaRepository.buscarDisponibles(
                4,
                1L,
                2L,
                EstadoSolicitud.APROBADO,
                EstadoOperativoLanchero.ACTIVO,
                fechaHoraConsulta
        ));
    }

    @Test
    @Transactional
    void outboxClaimQueryExecutesWithinRollbackTransaction() {
        registroEmailOutboxRepository.reclamarSiguiente(5, 120);
    }

    @Test
    @Transactional
    void featuredQueryIncludesOnlyApprovedBoatsAndOrdersByRatingThenNewBoats() {
        Zona origen = crearZona("origen");
        Zona destino = crearZona("destino");
        Lancha calificacionCuatro = crearLancha("cuatro", EstadoSolicitud.APROBADO, origen, destino);
        Lancha calificacionCinco = crearLancha("cinco", EstadoSolicitud.APROBADO, origen, destino);
        Lancha calificacionCincoConMasResenas =
                crearLancha("cinco-mas-resenas", EstadoSolicitud.APROBADO, origen, destino);
        Lancha calificacionCincoMismoConteo =
                crearLancha("cinco-mismo-conteo", EstadoSolicitud.APROBADO, origen, destino);
        Lancha nuevaInactiva = crearLancha("nueva", EstadoSolicitud.APROBADO, origen, destino);
        Lancha rechazada = crearLancha("rechazada", EstadoSolicitud.RECHAZADO, origen, destino);

        agregarResena(calificacionCuatro, origen, destino, 5);
        agregarResena(calificacionCuatro, origen, destino, 3);
        agregarResena(calificacionCinco, origen, destino, 5);
        agregarResena(calificacionCincoConMasResenas, origen, destino, 5);
        agregarResena(calificacionCincoConMasResenas, origen, destino, 5);
        agregarResena(calificacionCincoMismoConteo, origen, destino, 5);
        agregarResena(rechazada, origen, destino, 5);
        guardarDocumento(calificacionCuatro.getLanchero(), "FOTO_LANCHA", "nautiflow/fotos/primera.webp");
        guardarDocumento(calificacionCuatro.getLanchero(), "FOTO_LANCHA", "nautiflow/fotos/segunda.webp");
        guardarDocumento(calificacionCuatro.getLanchero(), "LICENCIA", "nautiflow/documentos/licencia.pdf");

        var resultados = lanchaRepository.buscarDestacadas(EstadoSolicitud.APROBADO, PageRequest.of(0, 10));

        int indiceCincoConMasResenas = indiceDe(resultados, calificacionCincoConMasResenas.getIdLancha());
        int indiceCinco = indiceDe(resultados, calificacionCinco.getIdLancha());
        int indiceCincoMismoConteo = indiceDe(resultados, calificacionCincoMismoConteo.getIdLancha());
        int indiceCuatro = indiceDe(resultados, calificacionCuatro.getIdLancha());
        int indiceNueva = indiceDe(resultados, nuevaInactiva.getIdLancha());
        assertTrue(indiceCincoConMasResenas >= 0);
        assertTrue(indiceCinco > indiceCincoConMasResenas);
        assertTrue(indiceCincoMismoConteo < indiceCinco);
        assertTrue(indiceCuatro > indiceCinco);
        assertTrue(indiceNueva > indiceCuatro);
        assertEquals(5.0, resultados.get(indiceCincoConMasResenas).getPromedioCalificacion());
        assertEquals(2L, resultados.get(indiceCincoConMasResenas).getTotalResenas());
        assertEquals(5.0, resultados.get(indiceCinco).getPromedioCalificacion());
        assertEquals(1L, resultados.get(indiceCinco).getTotalResenas());
        assertEquals(4.0, resultados.get(indiceCuatro).getPromedioCalificacion());
        assertEquals(2L, resultados.get(indiceCuatro).getTotalResenas());
        assertNull(resultados.get(indiceNueva).getPromedioCalificacion());
        assertEquals(0L, resultados.get(indiceNueva).getTotalResenas());
        assertEquals(-1, indiceDe(resultados, rechazada.getIdLancha()));
        assertEquals(1, resultados.stream()
                .filter(lancha -> lancha.getIdLancha().equals(calificacionCuatro.getIdLancha()))
                .count());

        var fotos = documentoRepository.buscarFotosLancha(
                List.of(calificacionCuatro.getLanchero().getIdLanchero())
        );
        assertEquals(2, fotos.size());
        assertEquals("nautiflow/fotos/primera.webp", fotos.get(0).getClaveArchivo());
    }

    @Test
    @Transactional
    void featuredQueryAppliesTheTenBoatLimit() {
        Zona origen = crearZona("limite-origen");
        Zona destino = crearZona("limite-destino");
        for (int indice = 0; indice < 12; indice++) {
            crearLancha("limite-" + indice, EstadoSolicitud.APROBADO, origen, destino);
        }

        assertEquals(10, lanchaRepository.buscarDestacadas(
                EstadoSolicitud.APROBADO,
                PageRequest.of(0, 10)
        ).size());
    }

    private Zona crearZona(String sufijo) {
        Zona zona = new Zona();
        zona.setNombre("test-" + UUID.randomUUID() + "-" + sufijo);
        return zonaRepository.saveAndFlush(zona);
    }

    private Lancha crearLancha(String sufijo, EstadoSolicitud estado, Zona origen, Zona destino) {
        Lanchero lanchero = new Lanchero();
        lanchero.setNombreCompleto("Lanchero " + sufijo);
        lanchero.setWhatsapp("+570000000000");
        lanchero.setCorreo(UUID.randomUUID() + "@test.invalid");
        lanchero.setEstadoSolicitud(estado);
        lanchero.setEstadoViaje(EstadoOperativoLanchero.INACTIVO);
        lanchero = lancheroRepository.saveAndFlush(lanchero);

        Lancha lancha = new Lancha();
        lancha.setLanchero(lanchero);
        lancha.setNombre("Lancha " + sufijo);
        lancha.setCapacidad(4);
        lancha.agregarZona(origen);
        lancha.agregarZona(destino);
        return lanchaRepository.saveAndFlush(lancha);
    }

    private void agregarResena(Lancha lancha, Zona origen, Zona destino, int estrellas) {
        Viaje viaje = new Viaje();
        viaje.setLancha(lancha);
        viaje.setZonaOrigen(origen);
        viaje.setZonaDestino(destino);
        viaje.setFechaHoraInicio(OffsetDateTime.now(ZoneOffset.UTC));
        viaje.setCantidadPasajeros(1);
        viaje.setTuristaNombreCompleto("Pasajero de prueba");
        viaje.setTuristaWhatsapp("+570000000000");
        viaje = viajeRepository.saveAndFlush(viaje);

        Resena resena = new Resena();
        resena.setViaje(viaje);
        resena.setEstrellas(estrellas);
        resenaRepository.saveAndFlush(resena);
    }

    private void guardarDocumento(Lanchero lanchero, String tipo, String clave) {
        Documento documento = new Documento();
        documento.setLanchero(lanchero);
        documento.setTipoDocumento(tipo);
        documento.setClaveArchivo(clave);
        documento.setTipoContenido(tipo.equals("FOTO_LANCHA") ? "image/webp" : "application/pdf");
        documentoRepository.saveAndFlush(documento);
    }

    private int indiceDe(List<? extends LanchaRepository.DestacadaProjection> lanchas, Long idLancha) {
        for (int indice = 0; indice < lanchas.size(); indice++) {
            if (lanchas.get(indice).getIdLancha().equals(idLancha)) {
                return indice;
            }
        }
        return -1;
    }
}
