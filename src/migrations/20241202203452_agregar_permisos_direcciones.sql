INSERT INTO gestion_usuarios.permisos(endpoint, tipo, rol_id)
	VALUES ('direcciones', 'ESCRITURA', 4);

ALTER TABLE aplicacion.direccion ADD COLUMN direccion character varying(255);

