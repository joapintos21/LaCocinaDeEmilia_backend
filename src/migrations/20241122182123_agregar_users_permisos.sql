CREATE TABLE gestion_usuarios.Permisos (
    id SERIAL PRIMARY KEY,
    endpoint VARCHAR(255) NOT NULL,          -- Nombre del recurso o endpoint
    tipo VARCHAR(10) NOT NULL,               -- 'LECTURA' o 'ESCRITURA'
    rol_id INT REFERENCES gestion_usuarios.Rol(id) ON DELETE CASCADE
);

INSERT INTO gestion_usuarios.Permisos (endpoint, tipo, rol_id)
VALUES ('permisos', 'ESCRITURA', 1),
('productos', 'ESCRITURA', 1),
('categorias', 'ESCRITURA', 1),
('usuarios', 'ESCRITURA', 1),
('packs', 'ESCRITURA', 1),
('descuentos', 'ESCRITURA', 1),
('pedidos', 'ESCRITURA', 1),
('adicionales', 'ESCRITURA', 1),
('dashboard', 'ESCRITURA', 1);
