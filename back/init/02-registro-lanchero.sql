-- Ejecutar sobre una base existente que ya tenga init/01-schema.sql.
-- Los lancheros anteriores no tienen contraseña; los nuevos registros sí guardan hash.
ALTER TABLE lanchero ADD COLUMN IF NOT EXISTS password_hash VARCHAR(100);


