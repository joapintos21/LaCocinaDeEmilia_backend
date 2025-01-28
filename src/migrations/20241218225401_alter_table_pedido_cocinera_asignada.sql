-- Eliminar la clave foránea existente
ALTER TABLE aplicacion.Pedido
DROP CONSTRAINT IF EXISTS pedido_cocinera_asignada_fkey;

-- Modificar la columna cocinera_asignada para apuntar a gestion_usuarios.Usuario(id)
ALTER TABLE aplicacion.Pedido
ADD CONSTRAINT pedido_cocinera_asignada_fkey FOREIGN KEY (cocinera_asignada)
REFERENCES gestion_usuarios.Usuario(id) ON DELETE SET NULL;