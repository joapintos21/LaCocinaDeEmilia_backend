CREATE TABLE aplicacion.TipoDescuento (
    id SERIAL PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL UNIQUE -- Nombre como 'PORCENTAJE', 'IMPORTE', etc.
);

ALTER TABLE aplicacion.Descuento
    DROP COLUMN tipo,
    ADD COLUMN tipo_id INT REFERENCES aplicacion.TipoDescuento(id) ON DELETE CASCADE;

-- Insertar valores iniciales en la tabla TipoDescuento
INSERT INTO aplicacion.TipoDescuento (nombre) VALUES
    ('PORCENTAJE'),
    ('IMPORTE');

CREATE TABLE aplicacion.EstadoPedido (
    id SERIAL PRIMARY KEY,
    nombre VARCHAR(50) UNIQUE NOT NULL
);

-- Insertar valores iniciales
INSERT INTO aplicacion.EstadoPedido (nombre) VALUES
    ('EN_PROGRESO'),
    ('PENDIENTE_CONFIRMACION_PAGO'),
    ('LISTO_PARA_ENVIAR'),
    ('ENTREGADO'),
    ('CANCELADO');

ALTER TABLE aplicacion.MetodoPago
    ALTER COLUMN nombre TYPE VARCHAR(50),
    ADD CONSTRAINT unique_nombre UNIQUE(nombre);

-- Paso 1: Agregar las columnas temporales
ALTER TABLE aplicacion.Pedido ADD COLUMN estado_id INT REFERENCES aplicacion.EstadoPedido(id);
ALTER TABLE aplicacion.Pedido ADD COLUMN metodo_pago_id INT REFERENCES aplicacion.MetodoPago(id);

-- Paso 2: Migrar datos de los enums a las nuevas columnas
UPDATE aplicacion.Pedido p
SET estado_id = ep.id
FROM aplicacion.EstadoPedido ep
WHERE p.estado::TEXT = ep.nombre;

UPDATE aplicacion.Pedido p
SET metodo_pago_id = mp.id
FROM aplicacion.MetodoPago mp
WHERE p.metodo_pago::TEXT = mp.nombre;

-- Paso 3: Eliminar las columnas antiguas
ALTER TABLE aplicacion.Pedido DROP COLUMN estado;
ALTER TABLE aplicacion.Pedido DROP COLUMN metodo_pago;

-- Paso 4: Renombrar las nuevas columnas
ALTER TABLE aplicacion.Pedido RENAME COLUMN estado_id TO estado;
ALTER TABLE aplicacion.Pedido RENAME COLUMN metodo_pago_id TO metodo_pago;

-- Paso 1: Agregar las columnas temporales
ALTER TABLE aplicacion.Pedido_Historico ADD COLUMN estado_anterior_id INT REFERENCES aplicacion.EstadoPedido(id);
ALTER TABLE aplicacion.Pedido_Historico ADD COLUMN estado_nuevo_id INT REFERENCES aplicacion.EstadoPedido(id);

-- Paso 2: Migrar datos de los enums a las nuevas columnas
UPDATE aplicacion.Pedido_Historico ph
SET estado_anterior_id = ep.id
FROM aplicacion.EstadoPedido ep
WHERE ph.estado_anterior::TEXT = ep.nombre;

UPDATE aplicacion.Pedido_Historico ph
SET estado_nuevo_id = ep.id
FROM aplicacion.EstadoPedido ep
WHERE ph.estado_nuevo::TEXT = ep.nombre;

-- Paso 3: Eliminar las columnas antiguas
ALTER TABLE aplicacion.Pedido_Historico DROP COLUMN estado_anterior;
ALTER TABLE aplicacion.Pedido_Historico DROP COLUMN estado_nuevo;

-- Paso 4: Renombrar las nuevas columnas
ALTER TABLE aplicacion.Pedido_Historico RENAME COLUMN estado_anterior_id TO estado_anterior;
ALTER TABLE aplicacion.Pedido_Historico RENAME COLUMN estado_nuevo_id TO estado_nuevo;

DROP TYPE IF EXISTS EstadoPedidoEnum CASCADE;
DROP TYPE IF EXISTS MetodoPagoEnum CASCADE;
DROP TYPE IF EXISTS TipoDescuentoEnum CASCADE;