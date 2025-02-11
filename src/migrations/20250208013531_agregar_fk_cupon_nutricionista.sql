-- Agregar la columna cocinera_asignada como FK en la tabla Pedido
ALTER TABLE gestion_usuarios.cuponnutricionista
ADD COLUMN nutricionista_id INT REFERENCES gestion_usuarios.nutricionista(id) ON DELETE CASCADE;