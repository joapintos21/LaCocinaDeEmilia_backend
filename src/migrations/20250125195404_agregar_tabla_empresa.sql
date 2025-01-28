DROP TABLE IF EXISTS aplicacion.empresa;

CREATE TABLE aplicacion.empresa
	(
	id          SERIAL NOT NULL,
	nombre      VARCHAR (255) NOT NULL,
	direccion      VARCHAR (255) NULL,
	descripcion_transferencia  TEXT NULL,
	telefono  VARCHAR (255) NULL,
	correo VARCHAR (255) NULL,
	CONSTRAINT empresa_pkey PRIMARY KEY (id)
	)
	WITH (OIDS = FALSE);
	
INSERT INTO aplicacion.empresa (nombre, direccion, descripcion_transferencia, telefono, correo)
VALUES ('La cocina de Emilia', '', '', '', '');

INSERT INTO gestion_usuarios.permisos
	(
	endpoint
	, tipo
	, rol_id
	)
VALUES
	(
	'empresa'
	, 'LECTURA'
	, 4
	);
