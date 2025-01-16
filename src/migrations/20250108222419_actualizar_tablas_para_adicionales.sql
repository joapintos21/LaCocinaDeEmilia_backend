-- Agregar una columna `id` con tipo SERIAL
ALTER TABLE aplicacion.Pedido_Producto ADD COLUMN id SERIAL;
-- Eliminar la clave primaria compuesta actual
ALTER TABLE aplicacion.Pedido_Producto DROP CONSTRAINT Pedido_Producto_pkey;

-- Establecer la nueva columna `id` como clave primaria
ALTER TABLE aplicacion.Pedido_Producto ADD CONSTRAINT pk_pedido_producto PRIMARY KEY (id);

-- Modificar la estructura para referirse al id de Pedido_Producto
ALTER TABLE aplicacion.Pedido_Producto_Adicional DROP CONSTRAINT Pedido_Producto_Adicional_pkey;

-- Agregar la columna pedido_producto_id y configurar su referencia
ALTER TABLE aplicacion.Pedido_Producto_Adicional ADD COLUMN pedido_producto_id INT;

-- Configurar la relación con Pedido_Producto
ALTER TABLE aplicacion.Pedido_Producto_Adicional ADD CONSTRAINT fk_pedido_producto_adicional
    FOREIGN KEY (pedido_producto_id) REFERENCES aplicacion.Pedido_Producto(id) ON DELETE CASCADE;

-- Eliminar las columnas antiguas relacionadas con producto_id y pedido_id
ALTER TABLE aplicacion.Pedido_Producto_Adicional DROP COLUMN pedido_id;
ALTER TABLE aplicacion.Pedido_Producto_Adicional DROP COLUMN producto_id;

-- Establecer la nueva clave primaria
ALTER TABLE aplicacion.Pedido_Producto_Adicional ADD CONSTRAINT pk_pedido_producto_adicional
    PRIMARY KEY (pedido_producto_id, adicional_id);