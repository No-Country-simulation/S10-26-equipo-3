-- Actualiza esquemas ya inicializados antes de que Hibernate valide las entidades.
ALTER TABLE lanchero
    DROP CONSTRAINT IF EXISTS ck_lanchero_estado_viaje;
ALTER TABLE lanchero
    ADD CONSTRAINT ck_lanchero_estado_viaje
        CHECK (estado_viaje IN ('ACTIVO', 'INACTIVO', 'EN_VIAJE', 'EN_RETORNO'));

ALTER TABLE trayecto_retorno_vacio
    ADD COLUMN IF NOT EXISTS id_zona_origen BIGINT;
ALTER TABLE trayecto_retorno_vacio
    ADD COLUMN IF NOT EXISTS id_zona_destino BIGINT;

UPDATE trayecto_retorno_vacio tramo
SET id_zona_origen = viaje.id_zona_destino,
    id_zona_destino = viaje.id_zona_origen
FROM viaje
WHERE tramo.id_viaje = viaje.id_viaje
  AND (tramo.id_zona_origen IS NULL OR tramo.id_zona_destino IS NULL);

ALTER TABLE trayecto_retorno_vacio
    ALTER COLUMN id_zona_origen SET NOT NULL,
    ALTER COLUMN id_zona_destino SET NOT NULL;

ALTER TABLE trayecto_retorno_vacio
    DROP CONSTRAINT IF EXISTS fk_retorno_zona_origen;
ALTER TABLE trayecto_retorno_vacio
    ADD CONSTRAINT fk_retorno_zona_origen
        FOREIGN KEY (id_zona_origen) REFERENCES zona (id_zona);

ALTER TABLE trayecto_retorno_vacio
    DROP CONSTRAINT IF EXISTS fk_retorno_zona_destino;
ALTER TABLE trayecto_retorno_vacio
    ADD CONSTRAINT fk_retorno_zona_destino
        FOREIGN KEY (id_zona_destino) REFERENCES zona (id_zona);

ALTER TABLE trayecto_retorno_vacio
    ADD COLUMN IF NOT EXISTS turista_nombre_completo VARCHAR(201);
ALTER TABLE trayecto_retorno_vacio
    ADD COLUMN IF NOT EXISTS turista_whatsapp VARCHAR(20);
ALTER TABLE trayecto_retorno_vacio
    ADD COLUMN IF NOT EXISTS fecha_creacion TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP;

UPDATE trayecto_retorno_vacio
SET fecha_creacion = COALESCE(fecha_creacion, CURRENT_TIMESTAMP);

ALTER TABLE trayecto_retorno_vacio
    ALTER COLUMN fecha_creacion SET DEFAULT CURRENT_TIMESTAMP,
    ALTER COLUMN fecha_creacion SET NOT NULL;

ALTER TABLE trayecto_retorno_vacio
    DROP CONSTRAINT IF EXISTS ck_retorno_estado;
UPDATE trayecto_retorno_vacio SET estado = 'INACTIVO' WHERE estado = 'CERRADO';
ALTER TABLE trayecto_retorno_vacio
    ADD CONSTRAINT ck_retorno_estado CHECK (estado IN ('ACTIVO', 'INACTIVO'));

WITH retornos_duplicados AS (
    SELECT id_tramo,
           ROW_NUMBER() OVER (PARTITION BY id_viaje ORDER BY id_tramo DESC) AS numero
    FROM trayecto_retorno_vacio
    WHERE estado = 'ACTIVO'
)
UPDATE trayecto_retorno_vacio tramo
SET estado = 'INACTIVO'
FROM retornos_duplicados duplicado
WHERE tramo.id_tramo = duplicado.id_tramo
  AND duplicado.numero > 1;

CREATE UNIQUE INDEX IF NOT EXISTS uq_retorno_viaje_activo
    ON trayecto_retorno_vacio (id_viaje)
    WHERE estado = 'ACTIVO';
