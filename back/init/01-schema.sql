-- Esquema inicial PostgreSQL para Nautiflow.
-- Los archivos binarios permanecen en Cloudflare R2; aqui se guardan sus metadatos.


CREATE TABLE lanchero (
    id_lanchero BIGSERIAL PRIMARY KEY,
    nombre_completo VARCHAR(201) NOT NULL,
    whatsapp VARCHAR(20) NOT NULL,
    correo VARCHAR(100) NOT NULL UNIQUE,
    estado_solicitud VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
    fecha_registro TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_decision TIMESTAMP WITH TIME ZONE,
    motivo_rechazo VARCHAR(500),
    estado_viaje VARCHAR(20) NOT NULL DEFAULT 'ACTIVO',
    password_hash VARCHAR(100) NOT NULL,
    CONSTRAINT ck_lanchero_estado_solicitud
        CHECK (estado_solicitud IN ('PENDIENTE', 'APROBADO', 'RECHAZADO')),
    CONSTRAINT ck_lanchero_estado_viaje
        CHECK (estado_viaje IN ('ACTIVO', 'INACTIVO', 'EN_VIAJE'))
);

CREATE TABLE documento (
    id_documento BIGSERIAL PRIMARY KEY,
    id_lanchero BIGINT NOT NULL,
    tipo_documento VARCHAR(50) NOT NULL,
    clave_archivo VARCHAR(255) NOT NULL,
    tipo_contenido VARCHAR(255) NOT NULL,
    fecha TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_documento_lanchero
        FOREIGN KEY (id_lanchero) REFERENCES lanchero (id_lanchero) ON DELETE CASCADE,
    CONSTRAINT ck_documento_tipo
        CHECK (tipo_documento IN ('LICENCIA', 'SEGURO', 'ANTECEDENTES', 'FOTO_LANCHA'))
);
CREATE INDEX idx_documento_id_lanchero ON documento (id_lanchero);

CREATE TABLE lancha (
    id_lancha BIGSERIAL PRIMARY KEY,
    id_lanchero BIGINT NOT NULL UNIQUE,
    nombre VARCHAR(100) NOT NULL,
    capacidad INTEGER NOT NULL,
    CONSTRAINT fk_lancha_lanchero
        FOREIGN KEY (id_lanchero) REFERENCES lanchero (id_lanchero) ON DELETE CASCADE,
    CONSTRAINT ck_lancha_capacidad CHECK (capacidad > 0)
);

CREATE TABLE zona (
    id_zona BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE lancha_zona (
    id_lancha BIGINT NOT NULL,
    id_zona BIGINT NOT NULL,
    CONSTRAINT pk_lancha_zona PRIMARY KEY (id_lancha, id_zona),
    CONSTRAINT fk_lancha_zona_lancha
        FOREIGN KEY (id_lancha) REFERENCES lancha (id_lancha) ON DELETE CASCADE,
    CONSTRAINT fk_lancha_zona_zona
        FOREIGN KEY (id_zona) REFERENCES zona (id_zona) ON DELETE CASCADE
);
CREATE INDEX idx_lancha_zona_id_zona ON lancha_zona (id_zona);

CREATE TABLE viaje (
    id_viaje BIGSERIAL PRIMARY KEY,
    id_lancha BIGINT NOT NULL,
    id_zona_origen BIGINT NOT NULL,
    id_zona_destino BIGINT NOT NULL,
    fecha_hora_inicio TIMESTAMP WITH TIME ZONE NOT NULL,
    fecha_hora_fin TIMESTAMP WITH TIME ZONE,
    cantidad_pasajeros INTEGER NOT NULL,
    turista_nombre_completo VARCHAR(201) NOT NULL,
    turista_whatsapp VARCHAR(20) NOT NULL,
    token_resena VARCHAR(64) UNIQUE,
    CONSTRAINT fk_viaje_lancha
        FOREIGN KEY (id_lancha) REFERENCES lancha (id_lancha),
    CONSTRAINT fk_viaje_zona_origen
        FOREIGN KEY (id_zona_origen) REFERENCES zona (id_zona),
    CONSTRAINT fk_viaje_zona_destino
        FOREIGN KEY (id_zona_destino) REFERENCES zona (id_zona),
    CONSTRAINT ck_viaje_cantidad_pasajeros CHECK (cantidad_pasajeros > 0),
    CONSTRAINT ck_viaje_fechas CHECK (fecha_hora_fin IS NULL OR fecha_hora_fin >= fecha_hora_inicio)
);
CREATE INDEX idx_viaje_id_lancha ON viaje (id_lancha);
CREATE INDEX idx_viaje_id_zona_origen ON viaje (id_zona_origen);
CREATE INDEX idx_viaje_id_zona_destino ON viaje (id_zona_destino);

CREATE TABLE resena (
    id_resena BIGSERIAL PRIMARY KEY,
    id_viaje BIGINT NOT NULL UNIQUE,
    estrellas INTEGER NOT NULL,
    comentario VARCHAR(500),
    fecha TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_resena_viaje
        FOREIGN KEY (id_viaje) REFERENCES viaje (id_viaje) ON DELETE CASCADE,
    CONSTRAINT ck_resena_estrellas CHECK (estrellas BETWEEN 1 AND 5)
);

CREATE TABLE trayecto_retorno_vacio (
    id_tramo BIGSERIAL PRIMARY KEY,
    id_viaje BIGINT NOT NULL,
    id_zona_origen BIGINT NOT NULL,
    id_zona_destino BIGINT NOT NULL,
    fecha_hora_retorno TIMESTAMP WITH TIME ZONE NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVO',
    CONSTRAINT fk_retorno_viaje
        FOREIGN KEY (id_viaje) REFERENCES viaje (id_viaje) ON DELETE CASCADE,
    CONSTRAINT fk_retorno_zona_origen
        FOREIGN KEY (id_zona_origen) REFERENCES zona (id_zona),
    CONSTRAINT fk_retorno_zona_destino
        FOREIGN KEY (id_zona_destino) REFERENCES zona (id_zona),
    CONSTRAINT ck_retorno_estado CHECK (estado IN ('ACTIVO', 'CERRADO'))
);
CREATE INDEX idx_retorno_id_viaje ON trayecto_retorno_vacio (id_viaje);
