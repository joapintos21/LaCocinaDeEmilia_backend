WITH new_admin AS (
  INSERT INTO gestion_usuarios.usuario(
	email, password, nombre, telefono, rol_id, status, reset_token, fecha_creacion)
	VALUES ('admin@lacocinadeemilia.com', '$2a$10$knzOatsfgv9cVqVUOEFXwOv5V8kYgvRgj2nFahiRN9bW/7sS1amSe', 'Admin', '091234567', 1, 'activo', NULL, CURRENT_DATE)

  RETURNING id
)
INSERT INTO gestion_usuarios.administrador(usuario_id)
SELECT id FROM new_admin;

WITH new_cocinera AS (
 INSERT INTO gestion_usuarios.usuario(
	email, password, nombre, telefono, rol_id, status, reset_token, fecha_creacion)
	VALUES ('cocinera@lacocinadeemilia.com', '$2a$10$knzOatsfgv9cVqVUOEFXwOv5V8kYgvRgj2nFahiRN9bW/7sS1amSe', 'Cocinera', '091234567', 2, 'activo', NULL, CURRENT_DATE)

  RETURNING id
)
INSERT INTO gestion_usuarios.cocinero(usuario_id)
SELECT id FROM new_cocinera;

WITH new_cadete AS (
  INSERT INTO gestion_usuarios.usuario(
	email, password, nombre, telefono, rol_id, status, reset_token, fecha_creacion)
	VALUES ('cadete@lacocinadeemilia.com', '$2a$10$knzOatsfgv9cVqVUOEFXwOv5V8kYgvRgj2nFahiRN9bW/7sS1amSe', 'Cadete', '091234567', 3, 'activo', NULL, CURRENT_DATE)

  RETURNING id
)
INSERT INTO gestion_usuarios.cadete(usuario_id)
SELECT id FROM new_cadete;

WITH new_cliente AS (
  INSERT INTO gestion_usuarios.usuario(
	email, password, nombre, telefono, rol_id, status, reset_token, fecha_creacion)
	VALUES ('cliente@lacocinadeemilia.com', '$2a$10$knzOatsfgv9cVqVUOEFXwOv5V8kYgvRgj2nFahiRN9bW/7sS1amSe', 'Cliente', '091234567', 5, 'activo', NULL, CURRENT_DATE)

  RETURNING id
)
INSERT INTO gestion_usuarios.cliente(usuario_id)
SELECT id FROM new_cliente;

WITH new_cliente_nutri AS (
  INSERT INTO gestion_usuarios.usuario(
    email, password, nombre, telefono, rol_id, status, reset_token, fecha_creacion)
  VALUES ('nutricionista@lacocinadeemilia.com', '$2a$10$knzOatsfgv9cVqVUOEFXwOv5V8kYgvRgj2nFahiRN9bW/7sS1amSe', 'Nutri', '091234567', 5, 'activo', NULL, CURRENT_DATE)
  RETURNING id
), new_nutri AS (
  INSERT INTO gestion_usuarios.cliente(usuario_id)
  SELECT id FROM new_cliente_nutri
  RETURNING id
)
INSERT INTO gestion_usuarios.nutricionista(cliente_id)
SELECT id FROM new_nutri;

WITH new_usuario AS (
  INSERT INTO gestion_usuarios.usuario(
      email, password, nombre, telefono, fecha_creacion, rol_id, status
  )
  VALUES (
      'generico@cliente.com', 
      '', 
      'Cliente generico', 
      '091234567', 
      CURRENT_DATE, 
      4, 
      'activo'
  )
  RETURNING id
)
INSERT INTO gestion_usuarios.cliente(usuario_id)
SELECT id FROM new_usuario;