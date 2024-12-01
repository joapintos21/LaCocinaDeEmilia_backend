CREATE TABLE aplicacion.Pago (
    id SERIAL PRIMARY KEY,
    pedido_id INT NOT NULL REFERENCES aplicacion.Pedido(id) ON DELETE CASCADE, -- Relación con Pedido
    metodo_pago_id INT NOT NULL REFERENCES aplicacion.MetodoPago(id) ON DELETE SET NULL, -- Relación con Método de Pago
    estado_pago VARCHAR(50) NOT NULL CHECK (estado_pago IN ('APROBADO', 'PENDIENTE', 'RECHAZADO')), -- Restricción para validar el estado del pago
    referencia VARCHAR(255), -- Referencia o ID proporcionado por el proveedor de pago (e.g., MercadoPago)
    monto NUMERIC(10,2) NOT NULL, -- Monto pagado
    fecha_pago TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- Fecha y hora del pago
    observaciones TEXT -- Comentarios adicionales, opcional
);