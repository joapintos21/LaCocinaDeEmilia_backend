-- Agregar columna reset_token a la tabla Usuario
ALTER TABLE gestion_usuarios.Usuario 
ADD COLUMN reset_token VARCHAR(255) UNIQUE;