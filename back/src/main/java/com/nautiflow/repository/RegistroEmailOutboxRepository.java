package com.nautiflow.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

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
            RETURNING outbox.id_outbox, outbox.id_lanchero, outbox.correo, outbox.intentos
            """;

    private final JdbcTemplate jdbcTemplate;

    public RegistroEmailOutboxRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public void encolar(Long idLanchero, String correo) {
        jdbcTemplate.update("""
                INSERT INTO registro_email_outbox (id_lanchero, correo)
                VALUES (?, ?)
                ON CONFLICT (id_lanchero) DO NOTHING
                """, idLanchero, correo);
    }

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
                        resultSet.getInt("intentos")
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

    @Transactional
    public void marcarEnviada(long id) {
        jdbcTemplate.update("""
                UPDATE registro_email_outbox
                SET estado = 'ENVIADO',
                    enviada_en = CURRENT_TIMESTAMP,
                    lease_hasta = NULL,
                    ultimo_error = NULL,
                    correo = NULL
                WHERE id_outbox = ?
                """, id);
    }

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
