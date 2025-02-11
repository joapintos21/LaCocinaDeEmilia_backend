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
