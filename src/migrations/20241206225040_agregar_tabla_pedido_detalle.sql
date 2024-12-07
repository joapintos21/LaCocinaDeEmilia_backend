CREATE TABLE aplicacion.PedidoDetalle (
    id SERIAL PRIMARY KEY,
    pedido_id INT NOT NULL REFERENCES aplicacion.Pedido(id) ON DELETE CASCADE,
    descripcion VARCHAR(255) NOT NULL,       -- Descripción del ítem
    cantidad INT DEFAULT 1 CHECK (cantidad > 0), -- Cantidad de ítems (mínimo 1)
    precio_unitario NUMERIC(10,2) NOT NULL, -- Precio unitario
    subtotal NUMERIC(10,2) NOT NULL CHECK (subtotal >= 0) -- Subtotal calculado (cantidad * precio_unitario)
);