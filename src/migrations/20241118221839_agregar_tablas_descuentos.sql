-- Eliminar la columna porcentaje si ya no es necesaria
ALTER TABLE gestion_usuarios.CuponNutricionista
DROP COLUMN porcentaje;

-- Agregar la columna descuento_id con la relación a aplicacion.Descuento
ALTER TABLE gestion_usuarios.CuponNutricionista
ADD COLUMN descuento_id INT REFERENCES aplicacion.Descuento(id) ON DELETE CASCADE;

-- Modificar la columna fechaFin para que tenga NULL como valor por defecto
ALTER TABLE aplicacion.Descuento
ALTER COLUMN fechaFin DROP DEFAULT,
ALTER COLUMN fechaFin SET DEFAULT NULL;

-- Modificar la columna valor para que sea NOT NULL
ALTER TABLE aplicacion.Descuento
ALTER COLUMN valor SET NOT NULL;

CREATE TABLE aplicacion.Descuento_Producto (
    id SERIAL PRIMARY KEY,
    producto_id INT REFERENCES aplicacion.Producto(id) ON DELETE CASCADE,
    descuento_id INT REFERENCES aplicacion.Descuento(id) ON DELETE CASCADE
);

CREATE TABLE aplicacion.Descuento_Pack (
    id SERIAL PRIMARY KEY,
    pack_id INT REFERENCES aplicacion.Pack(id) ON DELETE CASCADE,
    descuento_id INT REFERENCES aplicacion.Descuento(id) ON DELETE CASCADE
);

CREATE TABLE aplicacion.Descuento_Categoria (
    id SERIAL PRIMARY KEY,
    categoria_id INT REFERENCES aplicacion.Categoria(id) ON DELETE CASCADE,
    descuento_id INT REFERENCES aplicacion.Descuento(id) ON DELETE CASCADE
);