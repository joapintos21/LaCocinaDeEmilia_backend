INSERT INTO gestion_usuarios.permisos(endpoint, tipo, rol_id)
	VALUES ('usuarios', 'ESCRITURA', 1);

INSERT INTO gestion_usuarios.permisos(endpoint, tipo, rol_id)
	VALUES ('usuarios', 'ESCRITURA', 2);

INSERT INTO gestion_usuarios.permisos(endpoint, tipo, rol_id)
	VALUES ('usuarios', 'ESCRITURA', 3);

INSERT INTO gestion_usuarios.permisos(endpoint, tipo, rol_id)
	VALUES ('usuarios', 'ESCRITURA', 4);

INSERT INTO gestion_usuarios.permisos(endpoint, tipo, rol_id)
	VALUES ('reporte', 'ESCRITURA', 1);

INSERT INTO gestion_usuarios.permisos(endpoint, tipo, rol_id)
	VALUES ('direcciones', 'ESCRITURA', 1);

INSERT INTO gestion_usuarios.permisos(endpoint, tipo, rol_id)
	VALUES ('pedidos', 'ESCRITURA', 2);

ALTER TABLE aplicacion.producto ADD COLUMN titulo VARCHAR(255);

