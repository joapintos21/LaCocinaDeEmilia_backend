-- Agregar la columna cocinera_asignada como FK en la tabla Pedido
ALTER TABLE aplicacion.Pedido
ADD COLUMN cocinera_asignada INT REFERENCES gestion_usuarios.Cocinero(id) ON DELETE SET NULL;