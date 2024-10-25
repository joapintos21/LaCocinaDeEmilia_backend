CREATE TABLE gestion_usuarios.Rol (
    id SERIAL PRIMARY KEY,
    nombre VARCHAR(50) UNIQUE NOT NULL
);

ALTER TABLE gestion_usuarios.Usuario
ADD COLUMN rol_id INT REFERENCES gestion_usuarios.Rol(id);

-- Inserta roles comunes en la tabla Rol
INSERT INTO gestion_usuarios.Rol (nombre) VALUES 
    ('Administrador'),
    ('Cocinero'),
    ('Cadete'),
    ('Cliente'),
    ('Nutricionista');

CREATE INDEX idx_usuario_email ON gestion_usuarios.Usuario(email);
CREATE INDEX idx_usuario_telefono ON gestion_usuarios.Usuario(telefono);

ALTER TABLE aplicacion.Direccion 
ALTER COLUMN latitud TYPE DECIMAL(9,6) USING latitud::DECIMAL,
ALTER COLUMN longitud TYPE DECIMAL(9,6) USING longitud::DECIMAL;