UPDATE aplicacion.producto SET imagen = NULL;

ALTER TABLE aplicacion.producto 
ALTER COLUMN imagen TYPE TEXT;