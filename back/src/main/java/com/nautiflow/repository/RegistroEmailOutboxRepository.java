package com.nautiflow.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio JDBC del outbox de correos de registro. Implementa el reclamo atómico, el lease, los reintentos y el cierre de tareas.
 */
@Repository
public class RegistroEmailOutboxRepository {
    private static final String RECLAMAR_SQL = """
            WITH siguiente AS (
                SELECT id_outbox
                FROM registro_email_outbox
                WHERE (
                    (estado = 'PENDIENTE' AND disponible_desde <= CURRENT_TIMESTAMP)
                    OR (estado = 'PROCESANDO' AND lease_hasta <= CURRENT_TIMESTAMP)
                )
                AND intentos < ?
                ORDER BY disponible_desde, id_outbox
                FOR UPDATE SKIP LOCKED
                LIMIT 1
            )
            UPDATE registro_email_outbox AS outbox
            SET estado = 'PROCESANDO',
                intentos = outbox.intentos + 1,
                lease_hasta = CURRENT_TIMESTAMP + (? * INTERVAL '1 second')
            FROM siguiente
            WHERE outbox.id_outbox = siguiente.id_outbox
            RETURNING outbox.id_outbox, outbox.id_lanchero, outbox.correo, outbox.intentos,
                      outbox.payload_json, outbox.idempotency_key
            """;

    private final JdbcTemplate jdbcTemplate;

    /**
     * Crea el repositorio JDBC usado para el outbox de correos.
     */
    public RegistroEmailOutboxRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Registra un correo pendiente para un lanchero; si ya existe, no hace nada.
     */
    @Transactional
    public void encolar(Long idLanchero, String correo) {
        jdbcTemplate.update("""
                INSERT INTO registro_email_outbox (id_lanchero, correo)
                VALUES (?, ?)
                ON CONFLICT (id_lanchero) DO NOTHING
                """, idLanchero, correo);
    }

    /**
     * Reclama atómicamente la siguiente tarea disponible y le asigna un lease temporal.
     */
    @Transactional
    public Optional<RegistroEmailOutboxTask> reclamarSiguiente(int maximoIntentos, long segundosLease) {
        jdbcTemplate.update("""
                UPDATE registro_email_outbox
                SET estado = 'FALLIDO',
                    lease_hasta = NULL,
                    ultimo_error = COALESCE(ultimo_error, 'Lease expirado tras agotar intentos')
                WHERE estado = 'PROCESANDO'
                  AND lease_hasta <= CURRENT_TIMESTAMP
                  AND intentos >= ?
                """, maximoIntentos);
        List<RegistroEmailOutboxTask> tareas = jdbcTemplate.query(
                RECLAMAR_SQL,
                (resultSet, rowNumber) -> new RegistroEmailOutboxTask(
                        resultSet.getLong("id_outbox"),
                        resultSet.getLong("id_lanchero"),
                        resultSet.getString("correo"),
                        resultSet.getInt("intentos"),
                        resultSet.getString("payload_json"),
                        resultSet.getString("idempotency_key")
                ),
                maximoIntentos,
                segundosLease
        );
        if (tareas.isEmpty()) {
            return Optional.empty();
        }

        RegistroEmailOutboxTask tarea = tareas.getFirst();
        return Optional.of(tarea);
    }

    /**
     * Marca la tarea como enviada y borra los datos personales del payload.
     */
    @Transactional
    public void marcarEnviada(long id) {
        jdbcTemplate.update("""
                UPDATE registro_email_outbox
                SET estado = 'ENVIADO',
                    enviada_en = CURRENT_TIMESTAMP,
                    lease_hasta = NULL,
                    ultimo_error = NULL,
                    correo = NULL,
                    payload_json = NULL
                WHERE id_outbox = ?
                """, id);
    }

    /**
     * Guarda el payload y la clave de idempotencia si aún no estaban persistidos.
     */
    @Transactional
    public RegistroEmailOutboxPayload asegurarPayloadYClave(
            long id,
            int intentos,
            String payloadJson,
            String idempotencyKey
    ) {
        return jdbcTemplate.queryForObject("""
                UPDATE registro_email_outbox
                SET payload_json = COALESCE(payload_json, ?),
                    idempotency_key = COALESCE(idempotency_key, ?)
                WHERE id_outbox = ? AND intentos = ? AND estado = 'PROCESANDO'
                RETURNING payload_json, idempotency_key
                """, (resultSet, rowNumber) -> new RegistroEmailOutboxPayload(
                resultSet.getString("payload_json"),
                resultSet.getString("idempotency_key")
        ), payloadJson, idempotencyKey, id, intentos);
    }

    /**
     * Reprograma la tarea con una espera o la marca como fallida al agotar los intentos.
     */
    @Transactional
    public void programarReintento(
            long id,
            int intentos,
            int maximoIntentos,
            long demoraSegundos,
            String tipoError
    ) {
        jdbcTemplate.update("""
                UPDATE registro_email_outbox
                SET estado = CASE WHEN intentos >= ? THEN 'FALLIDO' ELSE 'PENDIENTE' END,
                    disponible_desde = CURRENT_TIMESTAMP + (? * INTERVAL '1 second'),
                    lease_hasta = NULL,
                    ultimo_error = ?
                WHERE id_outbox = ? AND intentos = ?
                """, maximoIntentos, demoraSegundos, tipoError, id, intentos);
    }

    /**
     * Marca la tarea como fallida por un error permanente.
     */
    @Transactional
    public void marcarFallida(long id, int intentos, String tipoError) {
        jdbcTemplate.update("""
                UPDATE registro_email_outbox
                SET estado = 'FALLIDO',
                    lease_hasta = NULL,
                    ultimo_error = ?
                WHERE id_outbox = ? AND intentos = ?
                """, tipoError, id, intentos);
    }
}
