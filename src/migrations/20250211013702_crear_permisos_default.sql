INSERT INTO gestion_usuarios.permisos(endpoint, tipo, rol_id)
VALUES
  ('permisos',    'ESCRITURA', 1),
  ('productos',   'ESCRITURA', 1),
  ('categorias',  'ESCRITURA', 1),
  ('usuarios',    'ESCRITURA', 1),
  ('descuentos',  'ESCRITURA', 1),
  ('pedidos',     'ESCRITURA', 1),
  ('gastos',      'ESCRITURA', 1),
  ('dashboard',   'ESCRITURA', 1),
  ('adicionales', 'ESCRITURA', 1),
  ('reporte',     'ESCRITURA', 1),
  ('direcciones', 'ESCRITURA', 1),
  ('empresa',     'ESCRITURA', 1),
  ('pagos',      'ESCRITURA', 1),
  ('cupones',     'ESCRITURA', 1);

-- Para rol_id = 2
INSERT INTO gestion_usuarios.permisos(endpoint, tipo, rol_id)
VALUES
  ('usuarios', 'ESCRITURA', 2),
  ('pedidos',  'ESCRITURA', 2);

-- Para rol_id = 3
INSERT INTO gestion_usuarios.permisos(endpoint, tipo, rol_id)
VALUES
  ('usuarios', 'ESCRITURA', 3),
  ('pedidos',  'ESCRITURA', 3);

-- Para rol_id = 4
INSERT INTO gestion_usuarios.permisos(endpoint, tipo, rol_id)
VALUES
  ('carrito',    'ESCRITURA', 4),
  ('pedidos',    'ESCRITURA', 4),
  ('empresa', 'LECTURA', 4),
  ('pagos',      'ESCRITURA', 4),
  ('direcciones','ESCRITURA', 4),
  ('cupones', 'LECTURA', 4),
  ('usuarios',   'ESCRITURA', 4);

-- Para rol_id = 5
INSERT INTO gestion_usuarios.permisos(endpoint, tipo, rol_id)
VALUES 
    ('usuarios', 'ESCRITURA', 5),
    ('empresa', 'LECTURA', 5),
    ('carrito', 'ESCRITURA', 5),
    ('pedidos',    'ESCRITURA', 5),
    ('pagos', 'ESCRITURA', 5),
    ('direcciones', 'ESCRITURA', 5);