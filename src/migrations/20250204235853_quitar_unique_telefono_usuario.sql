-- Drop the unique constraint on the telefono column
ALTER TABLE gestion_usuarios.Usuario DROP CONSTRAINT IF EXISTS usuario_telefono_key;

-- Drop the index on the telefono column
DROP INDEX IF EXISTS gestion_usuarios.idx_usuario_telefono;
