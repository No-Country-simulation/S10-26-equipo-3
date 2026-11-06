-- El registro de lancheros no crea cuentas ni requiere credenciales.
ALTER TABLE lanchero DROP COLUMN IF EXISTS password_hash;
