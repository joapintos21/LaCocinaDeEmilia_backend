-- Agregar la columna cadete_asignado como FK en la tabla Pedido
ALTER TABLE aplicacion.Pedido
ADD COLUMN cadete_asignado INT REFERENCES gestion_usuarios.Usuario(id) ON DELETE SET NULL;