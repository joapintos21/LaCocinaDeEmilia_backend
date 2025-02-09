-- Agregar la columna cocinera_asignada como FK en la tabla Pedido
ALTER TABLE aplicacion.Pedido
ADD COLUMN cupon_id INT REFERENCES gestion_usuarios.cuponnutricionista(id) ON DELETE SET NULL;