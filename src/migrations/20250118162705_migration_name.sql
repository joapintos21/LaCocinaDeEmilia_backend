CREATE TABLE aplicacion.Gastos (
    id SERIAL PRIMARY KEY,              
    descripcion VARCHAR(255) NOT NULL,  
    monto NUMERIC(10, 2) NOT NULL,      
    fecha DATE NOT NULL,                
    fecha_carga TIMESTAMP DEFAULT NOW()
);

INSERT INTO gestion_usuarios.permisos (endpoint, tipo, rol_id) 
VALUES ('gastos', 'ESCRITURA', 1);