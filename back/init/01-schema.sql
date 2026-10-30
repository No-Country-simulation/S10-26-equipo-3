-- 1. Tabla de Lancheros
CREATE TABLE lanchero (
    id_lanchero BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    apellido VARCHAR(100) NOT NULL,
    documento_identidad VARCHAR(50) UNIQUE NOT NULL,
    telefono VARCHAR(20) NOT NULL,
    correo VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE', -- PENDIENTE, VERIFICADO, RECHAZADO
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 2. Tabla de Documentos
CREATE TABLE documento (
    id_documento BIGSERIAL PRIMARY KEY,
    id_lanchero BIGINT NOT NULL,
    tipo_documento VARCHAR(50) NOT NULL, -- LICENCIA, SEGURO, ANTECEDENTES
    ruta_archivo VARCHAR(255) NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE', -- PENDIENTE, APROBADO, RECHAZADO
    fecha TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_documento_lanchero FOREIGN KEY (id_lanchero) REFERENCES lanchero(id_lanchero) ON DELETE CASCADE
);

-- 3. Tabla de Lanchas
CREATE TABLE lancha (
    id_lancha BIGSERIAL PRIMARY KEY,
    id_lanchero BIGINT NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    capacidad INT NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE', -- PENDIENTE, VERIFICADO, RECHAZADO
    descripcion TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_lancha_lanchero FOREIGN KEY (id_lanchero) REFERENCES lanchero(id_lanchero) ON DELETE CASCADE
);

-- 4. Tabla de Zonas
CREATE TABLE zona (
    id_zona BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL UNIQUE,
    descripcion TEXT
);

-- 5. Tabla de Viajes
CREATE TABLE viaje (
    id_viaje BIGSERIAL PRIMARY KEY,
    id_lancha BIGINT, -- Puede ser NULL antes de ser asignado
    id_zona_origen BIGINT NOT NULL,
    id_zona_destino BIGINT NOT NULL,
    fecha_hora TIMESTAMP WITH TIME ZONE NOT NULL,
    cantidad_pasajeros INT NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'PROGRAMADO', -- PROGRAMADO, COMPLETADO, CANCELADO
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_viaje_lancha FOREIGN KEY (id_lancha) REFERENCES lancha(id_lancha) ON DELETE SET NULL,
    CONSTRAINT fk_viaje_zona_origen FOREIGN KEY (id_zona_origen) REFERENCES zona(id_zona),
    CONSTRAINT fk_viaje_zona_destino FOREIGN KEY (id_zona_destino) REFERENCES zona(id_zona)
);

-- 6. Tabla de Reseñas
CREATE TABLE resena (
    id_resena BIGSERIAL PRIMARY KEY,
    id_viaje BIGINT UNIQUE NOT NULL,
    calificacion INT CHECK (calificacion >= 1 AND calificacion <= 5),
    comentario TEXT,
    token VARCHAR(64) UNIQUE NOT NULL, -- Token único para URL de reseña post-viaje
    fecha TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_resena_viaje FOREIGN KEY (id_viaje) REFERENCES viaje(id_viaje) ON DELETE CASCADE
);

-- 7. Tabla de Trayectos / Retornos Vacíos
CREATE TABLE trayecto_retorno_vacio (
    id_tramo BIGSERIAL PRIMARY KEY,
    id_lancha BIGINT NOT NULL,
    id_zona_origen BIGINT NOT NULL,
    id_zona_destino BIGINT NOT NULL,
    fecha_hora_retorno TIMESTAMP WITH TIME ZONE NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'DISPONIBLE', -- DISPONIBLE, RESERVADO, COMPLETADO
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_retorno_lancha FOREIGN KEY (id_lancha) REFERENCES lancha(id_lancha) ON DELETE CASCADE,
    CONSTRAINT fk_retorno_zona_origen FOREIGN KEY (id_zona_origen) REFERENCES zona(id_zona),
    CONSTRAINT fk_retorno_zona_destino FOREIGN KEY (id_zona_destino) REFERENCES zona(id_zona)
);
