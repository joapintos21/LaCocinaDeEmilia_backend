ALTER TABLE gestion_usuarios.Usuario
ADD COLUMN status VARCHAR(10) DEFAULT 'pendiente',
ADD CONSTRAINT status_check
CHECK (status IN ('activo', 'pendiente', 'inactivo'));