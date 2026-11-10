package com.nautiflow;

import com.nautiflow.repository.LanchaRepository;
import com.nautiflow.repository.LancheroRepository;
import com.nautiflow.repository.DocumentoRepository;
import com.nautiflow.repository.RegistroEmailOutboxRepository;
import com.nautiflow.repository.ResenaRepository;
import com.nautiflow.repository.TrayectoRetornoVacioRepository;
import com.nautiflow.repository.ViajeRepository;
import com.nautiflow.repository.ZonaRepository;
import com.nautiflow.model.EstadoSolicitud;
import com.nautiflow.model.EstadoOperativoLanchero;
import com.nautiflow.model.EstadoTramoVacio;
import com.nautiflow.model.Documento;
import com.nautiflow.model.Lanchero;
import com.nautiflow.model.Lancha;
import com.nautiflow.model.Resena;
import com.nautiflow.model.TrayectoRetornoVacio;
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
    private TrayectoRetornoVacioRepository trayectoRetornoVacioRepository;

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
                false,
                EstadoSolicitud.APROBADO,
                EstadoOperativoLanchero.ACTIVO,
                fechaHoraConsulta
        ));
        assertNotNull(lanchaRepository.buscarDisponibles(
                4,
                1L,
                2L,
                false,
                EstadoSolicitud.APROBADO,
                EstadoOperativoLanchero.ACTIVO,
                fechaHoraConsulta
        ));
    }

    @Test
    @Transactional
    void lanchaDisponiblePuedeAgendarseEnZonasRegistradasAunqueHayaFinalizadoEnOtraZona() {
        Zona origenSolicitado = crearZona("origen-solicitado");
        Zona destinoSolicitado = crearZona("destino-solicitado");
        Zona origenAnterior = crearZona("origen-anterior");
        Zona destinoAnterior = crearZona("destino-anterior");
        Lancha lancha = crearLancha("reubicacion", EstadoSolicitud.APROBADO, origenSolicitado, destinoSolicitado);
        lancha.getLanchero().setEstadoViaje(EstadoOperativoLanchero.ACTIVO);
        lancheroRepository.saveAndFlush(lancha.getLanchero());

        Viaje viajeAnterior = new Viaje();
        viajeAnterior.setLancha(lancha);
        viajeAnterior.setZonaOrigen(origenAnterior);
        viajeAnterior.setZonaDestino(destinoAnterior);
        viajeAnterior.setFechaHoraInicio(OffsetDateTime.parse("2030-01-10T08:00:00Z"));
        viajeAnterior.setFechaHoraFin(OffsetDateTime.parse("2030-01-10T09:00:00Z"));
        viajeAnterior.setCantidadPasajeros(1);
        viajeAnterior.setTuristaNombreCompleto("Pasajero de prueba");
        viajeAnterior.setTuristaWhatsapp("+570000000000");
        viajeAnterior = viajeRepository.saveAndFlush(viajeAnterior);

        var disponibles = lanchaRepository.buscarDisponibles(
                null,
                origenSolicitado.getIdZona(),
                destinoSolicitado.getIdZona(),
                false,
                EstadoSolicitud.APROBADO,
                EstadoOperativoLanchero.ACTIVO,
                OffsetDateTime.parse("2030-01-10T11:00:00Z")
        );

        assertTrue(disponibles.stream().anyMatch(disponible ->
                disponible.getIdLancha().equals(lancha.getIdLancha())
        ));
    }

    @Test
    @Transactional
    void lanchaEnViajeNoEstaDisponibleYUnaVezFinalizadoPuedeAgendarseEnZonasRegistradas() {
        Zona zonaRegistradaOrigen = crearZona("zona-registrada-origen");
        Zona zonaRegistradaDestino = crearZona("zona-registrada-destino");
        Lancha lancha = crearLancha(
                "viaje-en-curso",
                EstadoSolicitud.APROBADO,
                zonaRegistradaOrigen,
                zonaRegistradaDestino
        );
        lancha.getLanchero().setEstadoViaje(EstadoOperativoLanchero.ACTIVO);
        lancheroRepository.saveAndFlush(lancha.getLanchero());

        Viaje viajeEnCurso = new Viaje();
        viajeEnCurso.setLancha(lancha);
        viajeEnCurso.setZonaOrigen(zonaRegistradaOrigen);
        viajeEnCurso.setZonaDestino(zonaRegistradaDestino);
        viajeEnCurso.setFechaHoraInicio(OffsetDateTime.parse("2030-01-10T08:00:00Z"));
        viajeEnCurso.setCantidadPasajeros(1);
        viajeEnCurso.setTuristaNombreCompleto("Pasajero de prueba");
        viajeEnCurso.setTuristaWhatsapp("+570000000000");
        viajeEnCurso = viajeRepository.saveAndFlush(viajeEnCurso);

        var duranteElViaje = lanchaRepository.buscarDisponibles(
                null,
                zonaRegistradaOrigen.getIdZona(),
                zonaRegistradaDestino.getIdZona(),
                false,
                EstadoSolicitud.APROBADO,
                EstadoOperativoLanchero.ACTIVO,
                OffsetDateTime.parse("2030-01-10T09:00:00Z")
        );
        assertTrue(duranteElViaje.stream().noneMatch(disponible ->
                disponible.getIdLancha().equals(lancha.getIdLancha())
        ));

        viajeEnCurso.setFechaHoraFin(OffsetDateTime.parse("2030-01-10T10:00:00Z"));
        viajeRepository.saveAndFlush(viajeEnCurso);
        var despuesDeFinalizar = lanchaRepository.buscarDisponibles(
                null,
                zonaRegistradaOrigen.getIdZona(),
                zonaRegistradaDestino.getIdZona(),
                false,
                EstadoSolicitud.APROBADO,
                EstadoOperativoLanchero.ACTIVO,
                OffsetDateTime.parse("2030-01-10T11:00:00Z")
        );
        assertTrue(despuesDeFinalizar.stream().anyMatch(disponible ->
                disponible.getIdLancha().equals(lancha.getIdLancha())
        ));
    }

    @Test
    @Transactional
    void noIncluyeLanchaCuandoLaRutaSolicitadaTieneUnaZonaNoRegistrada() {
        Zona zonaRegistrada = crearZona("zona-registrada");
        Zona zonaNoRegistrada = crearZona("zona-no-registrada");
        Lancha lancha = crearLancha("sin-zona", EstadoSolicitud.APROBADO, zonaRegistrada, crearZona("otra-zona"));
        lancha.getLanchero().setEstadoViaje(EstadoOperativoLanchero.ACTIVO);
        lancheroRepository.saveAndFlush(lancha.getLanchero());

        var disponibles = lanchaRepository.buscarDisponibles(
                null,
                zonaRegistrada.getIdZona(),
                zonaNoRegistrada.getIdZona(),
                false,
                EstadoSolicitud.APROBADO,
                EstadoOperativoLanchero.ACTIVO,
                OffsetDateTime.parse("2030-01-10T11:00:00Z")
        );

        assertTrue(disponibles.stream().noneMatch(disponible ->
                disponible.getIdLancha().equals(lancha.getIdLancha())
        ));
    }

    @Test
    @Transactional
    void deltaComoOrigenIncluyeLanchasAunqueNoTenganEsaZonaRegistrada() {
        Zona delta = crearZona("Delta");
        Zona destinoRegistrado = crearZona("destino-registrado");
        Lancha lancha = crearLancha(
                "delta-comun",
                EstadoSolicitud.APROBADO,
                crearZona("origen-registrado"),
                destinoRegistrado
        );
        lancha.getLanchero().setEstadoViaje(EstadoOperativoLanchero.ACTIVO);
        lancheroRepository.saveAndFlush(lancha.getLanchero());

        var disponibles = lanchaRepository.buscarDisponibles(
                null,
                delta.getIdZona(),
                destinoRegistrado.getIdZona(),
                true,
                EstadoSolicitud.APROBADO,
                EstadoOperativoLanchero.ACTIVO,
                OffsetDateTime.parse("2030-01-10T11:00:00Z")
        );

        assertTrue(disponibles.stream().anyMatch(disponible ->
                disponible.getIdLancha().equals(lancha.getIdLancha())
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

    @Test
    @Transactional
    void returnQueryFiltersByDateAndStateAndLoadsRelatedRoute() {
        long viajesAbiertosAntes = viajeRepository.countByFechaHoraFinIsNull();
        Zona origen = crearZona("retorno-origen");
        Zona destino = crearZona("retorno-destino");
        Lancha lancha = crearLancha("retorno-query", EstadoSolicitud.APROBADO, origen, destino);
        Viaje viaje = new Viaje();
        viaje.setLancha(lancha);
        viaje.setZonaOrigen(origen);
        viaje.setZonaDestino(destino);
        viaje.setFechaHoraInicio(OffsetDateTime.parse("2026-11-09T12:00:00-03:00"));
        viaje.setCantidadPasajeros(1);
        viaje.setTuristaNombreCompleto("Pasajero de prueba");
        viaje.setTuristaWhatsapp("+570000000000");
        viaje = viajeRepository.saveAndFlush(viaje);
        lancha.getLanchero().setEstadoViaje(EstadoOperativoLanchero.EN_RETORNO);
        lancheroRepository.saveAndFlush(lancha.getLanchero());

        TrayectoRetornoVacio activo = crearRetorno(
                viaje,
                destino,
                origen,
                OffsetDateTime.parse("2099-11-09T15:00:00-03:00"),
                EstadoTramoVacio.ACTIVO
        );
        crearRetorno(
                viaje,
                destino,
                origen,
                OffsetDateTime.parse("2099-11-10T15:00:00-03:00"),
                EstadoTramoVacio.INACTIVO
        );

        var resultados = trayectoRetornoVacioRepository.buscar(
                OffsetDateTime.parse("2099-11-09T00:00:00-03:00"),
                OffsetDateTime.parse("2099-11-10T00:00:00-03:00"),
                EstadoTramoVacio.ACTIVO
        );

        assertEquals(1, resultados.size());
        assertEquals(activo.getIdTramo(), resultados.getFirst().getIdTramo());
        assertEquals(destino.getNombre(), resultados.getFirst().getZonaOrigen().getNombre());
        assertEquals("Lancha retorno-query", resultados.getFirst().getViaje().getLancha().getNombre());
        assertEquals(viajesAbiertosAntes + 1, viajeRepository.countByFechaHoraFinIsNull());
        assertEquals(viajesAbiertosAntes + 1,
                viajeRepository.findByFechaHoraFinIsNull(PageRequest.of(0, 10)).getTotalElements());
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

    private TrayectoRetornoVacio crearRetorno(
            Viaje viaje,
            Zona zonaOrigen,
            Zona zonaDestino,
            OffsetDateTime fechaHoraRetorno,
            EstadoTramoVacio estado
    ) {
        TrayectoRetornoVacio retorno = new TrayectoRetornoVacio();
        retorno.setViaje(viaje);
        retorno.setZonaOrigen(zonaOrigen);
        retorno.setZonaDestino(zonaDestino);
        retorno.setFechaHoraRetorno(fechaHoraRetorno);
        retorno.setEstado(estado);
        retorno.setFechaCreacion(OffsetDateTime.now(ZoneOffset.UTC));
        return trayectoRetornoVacioRepository.saveAndFlush(retorno);
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
