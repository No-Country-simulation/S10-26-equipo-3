CREATE TABLE IF NOT EXISTS registro_email_outbox (
    id_outbox BIGSERIAL PRIMARY KEY,
    id_lanchero BIGINT NOT NULL UNIQUE,
    correo VARCHAR(100),
    estado VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
    intentos INTEGER NOT NULL DEFAULT 0,
    disponible_desde TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    lease_hasta TIMESTAMP WITH TIME ZONE,
    creada_en TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    enviada_en TIMESTAMP WITH TIME ZONE,
    ultimo_error VARCHAR(100),
    CONSTRAINT fk_registro_email_outbox_lanchero
        FOREIGN KEY (id_lanchero) REFERENCES lanchero (id_lanchero) ON DELETE CASCADE,
    CONSTRAINT ck_registro_email_outbox_estado
        CHECK (estado IN ('PENDIENTE', 'PROCESANDO', 'ENVIADO', 'FALLIDO')),
    CONSTRAINT ck_registro_email_outbox_intentos
        CHECK (intentos >= 0),
    CONSTRAINT ck_registro_email_outbox_correo
        CHECK ((estado = 'ENVIADO' AND correo IS NULL) OR (estado <> 'ENVIADO' AND correo IS NOT NULL))
);

ALTER TABLE registro_email_outbox
    ADD COLUMN IF NOT EXISTS correo VARCHAR(100);

UPDATE registro_email_outbox AS outbox
SET correo = lanchero.correo
FROM lanchero
WHERE outbox.id_lanchero = lanchero.id_lanchero
  AND outbox.estado <> 'ENVIADO'
  AND outbox.correo IS NULL;

UPDATE registro_email_outbox
SET correo = NULL
WHERE estado = 'ENVIADO' AND correo IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_registro_email_outbox_pendiente
    ON registro_email_outbox (disponible_desde, id_outbox)
    WHERE estado IN ('PENDIENTE', 'PROCESANDO');
