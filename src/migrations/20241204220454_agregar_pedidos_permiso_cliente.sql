UPDATE gestion_usuarios.permisos
	SET endpoint='pedidos', tipo='ESCRITURA', rol_id=4
	WHERE endpoint='pedidos' and tipo='LECTURA' and rol_id=4;