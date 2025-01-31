CREATE OR REPLACE VIEW aplicacion.vista_pedidos_detalles
 AS
 SELECT p.id,
    u_cliente.nombre AS nombre_cliente,
    u_cliente.telefono,
    d.direccion,
    d.barrio,
    p.fechacreacion AS fecha_creacion,
    m.nombre AS metodo_de_pago,
    pago.estado_pago,
    p.total,
    ep.nombre AS estado_pedido,
    u_cocinera.nombre AS nombre_cocinera,
    u_cadete.nombre AS nombre_cadete,
    p.fecha_entrega,
    COALESCE(( SELECT string_agg((pd.descripcion::text || ' x'::text) || pd.cantidad, '
'::text) AS string_agg
           FROM aplicacion.pedidodetalle pd
          WHERE pd.pedido_id = p.id), 'Sin detalles'::text) AS detalles_pedido
   FROM aplicacion.pedido p
   	 LEFT JOIN gestion_usuarios.cliente cliente ON cliente.id = p.cliente_id
     LEFT JOIN gestion_usuarios.usuario u_cocinera ON u_cocinera.id = p.cocinera_asignada
     LEFT JOIN gestion_usuarios.usuario u_cadete ON u_cadete.id = p.cadete_asignado
     LEFT JOIN gestion_usuarios.usuario u_cliente ON u_cliente.id = cliente.usuario_id
     LEFT JOIN aplicacion.estadopedido ep ON ep.id = p.estado
     LEFT JOIN aplicacion.envio e ON e.pedido_id = p.id
     LEFT JOIN aplicacion.direccion d ON d.id = e.direccion_id
     LEFT JOIN aplicacion.pago ON pago.pedido_id = p.id
     LEFT JOIN aplicacion.metodopago m ON m.id = pago.metodo_pago_id
  ORDER BY p.fechacreacion DESC, p.fecha_entrega DESC;