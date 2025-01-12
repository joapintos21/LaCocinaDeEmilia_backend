-- Crear esquemas
CREATE SCHEMA IF NOT EXISTS aplicacion AUTHORIZATION postgres;
CREATE SCHEMA IF NOT EXISTS gestion_usuarios AUTHORIZATION postgres;

-- Enumeraciones
CREATE TYPE EstadoPedidoEnum AS ENUM ('EN_PROGRESO', 'PENDIENTE_CONFIRMACION_PAGO', 'LISTO_PARA_ENVIAR', 'ENTREGADO', 'CANCELADO');
CREATE TYPE MetodoPagoEnum AS ENUM ('TRANSFERENCIA', 'MERCADO_PAGO', 'EFECTIVO');
CREATE TYPE TipoDescuentoEnum AS ENUM ('PORCENTAJE', 'IMPORTE');

-- Tabla Rol
CREATE TABLE gestion_usuarios.Rol (
    id SERIAL PRIMARY KEY,
    nombre VARCHAR(50) UNIQUE NOT NULL
);

-- Insertar roles en la tabla Rol
INSERT INTO gestion_usuarios.Rol (nombre) VALUES 
    ('Administrador'),
    ('Cocinero'),
    ('Cadete'),
    ('Cliente'),
    ('Nutricionista');

-- Tabla Usuario
CREATE TABLE gestion_usuarios.Usuario (
    id SERIAL PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    nombre VARCHAR(255) NOT NULL,
    telefono VARCHAR(20) UNIQUE NOT NULL,
    rol_id INT REFERENCES gestion_usuarios.Rol(id),
    status VARCHAR(10) DEFAULT 'pendiente' CHECK (status IN ('activo', 'pendiente', 'inactivo'))
);

-- Tabla Administrador
CREATE TABLE gestion_usuarios.Administrador (
    id SERIAL PRIMARY KEY,
    usuario_id INT REFERENCES gestion_usuarios.Usuario(id) ON DELETE CASCADE
);

-- Tabla Cocinero
CREATE TABLE gestion_usuarios.Cocinero (
    id SERIAL PRIMARY KEY,
    usuario_id INT REFERENCES gestion_usuarios.Usuario(id) ON DELETE CASCADE
);

-- Tabla Cadete
CREATE TABLE gestion_usuarios.Cadete (
    id SERIAL PRIMARY KEY,
    usuario_id INT REFERENCES gestion_usuarios.Usuario(id) ON DELETE CASCADE
);

-- Tabla Cliente
CREATE TABLE gestion_usuarios.Cliente (
    id SERIAL PRIMARY KEY,
    usuario_id INT REFERENCES gestion_usuarios.Usuario(id) ON DELETE CASCADE
);

-- Tabla Nutricionista (herencia de Cliente)
CREATE TABLE gestion_usuarios.Nutricionista (
    id SERIAL PRIMARY KEY,
    saldo NUMERIC(10,2) DEFAULT 0,
    cliente_id INT REFERENCES gestion_usuarios.Cliente(id) ON DELETE CASCADE
);

-- Tabla CuponNutricionista
CREATE TABLE gestion_usuarios.CuponNutricionista (
    id SERIAL PRIMARY KEY,
    codigo VARCHAR(50) UNIQUE NOT NULL,
    porcentaje NUMERIC(5,2) CHECK (porcentaje >= 0 AND porcentaje <= 100)
);

-- Tabla Direccion (relación de muchos a uno con Cliente)
CREATE TABLE aplicacion.Direccion (
    id SERIAL PRIMARY KEY,
    cliente_id INT REFERENCES gestion_usuarios.Cliente(id) ON DELETE CASCADE,
    titulo VARCHAR(255) NOT NULL,
    predeterminado BOOLEAN NOT NULL,
    latitud DECIMAL(9,6),
    longitud DECIMAL(9,6)
);

-- Tabla Pedido
CREATE TABLE aplicacion.Pedido (
    id SERIAL PRIMARY KEY,
    cliente_id INT REFERENCES gestion_usuarios.Cliente(id) ON DELETE SET NULL,
    estado EstadoPedidoEnum NOT NULL DEFAULT 'EN_PROGRESO',
    fechaCreacion DATE DEFAULT CURRENT_DATE,
    notas TEXT,
    metodo_pago MetodoPagoEnum,
    total NUMERIC(10,2)
);

-- Tabla Pedido_Historico para almacenar el historial de cambios de estado de los pedidos
CREATE TABLE aplicacion.Pedido_Historico (
    id SERIAL PRIMARY KEY,
    pedido_id INT REFERENCES aplicacion.Pedido(id) ON DELETE CASCADE,
    estado_anterior EstadoPedidoEnum,
    estado_nuevo EstadoPedidoEnum NOT NULL,
    fecha_cambio TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    usuario_id INT REFERENCES gestion_usuarios.Usuario(id) ON DELETE SET NULL,
    observaciones TEXT
);

-- Tabla MetodoPago
CREATE TABLE aplicacion.MetodoPago (
    id SERIAL PRIMARY KEY,
    nombre MetodoPagoEnum NOT NULL,
    comision NUMERIC(5,2) DEFAULT 0 CHECK (comision >= 0)
);

-- Tabla Envio
CREATE TABLE aplicacion.Envio (
    id SERIAL PRIMARY KEY,
    pedido_id INT REFERENCES aplicacion.Pedido(id) ON DELETE CASCADE,
    direccion_id INT REFERENCES aplicacion.Direccion(id) ON DELETE SET NULL,
    fechaEnvio DATE,
    costo NUMERIC(10,2),
    notasCadete TEXT,
    entregado BOOLEAN DEFAULT FALSE
);

-- Tabla Categoria
CREATE TABLE aplicacion.Categoria (
    id SERIAL PRIMARY KEY,
    titulo VARCHAR(255) NOT NULL,
    descripcion TEXT
);

-- Tabla Producto
CREATE TABLE aplicacion.Producto (
    id SERIAL PRIMARY KEY,
    descripcion TEXT NOT NULL,
    imagen TEXT,
    precio NUMERIC(10,2) NOT NULL CHECK (precio >= 0),
    categoria_id INT REFERENCES aplicacion.Categoria(id),
    visible BOOLEAN DEFAULT TRUE
);

-- Tabla EtiquetaProducto
CREATE TABLE aplicacion.EtiquetaProducto (
    id SERIAL PRIMARY KEY,
    titulo VARCHAR(50) NOT NULL
);

-- Relación de muchos a muchos entre Producto y EtiquetaProducto
CREATE TABLE aplicacion.Producto_Etiqueta (
    producto_id INT REFERENCES aplicacion.Producto(id) ON DELETE CASCADE,
    etiqueta_id INT REFERENCES aplicacion.EtiquetaProducto(id) ON DELETE CASCADE,
    PRIMARY KEY (producto_id, etiqueta_id)
);

-- Tabla Descuento
CREATE TABLE aplicacion.Descuento (
    id SERIAL PRIMARY KEY,
    fechainicio DATE NOT NULL,
    fechaFin DATE,
    tipo TipoDescuentoEnum NOT NULL,
    valor NUMERIC(5,2) CHECK (valor >= 0)
);

-- Tabla Pack
CREATE TABLE aplicacion.Pack (
    id SERIAL PRIMARY KEY,
    precio NUMERIC(10,2) NOT NULL
);

-- Relación de muchos a muchos entre Pack y Producto
CREATE TABLE aplicacion.Pack_Producto (
    pack_id INT REFERENCES aplicacion.Pack(id) ON DELETE CASCADE,
    producto_id INT REFERENCES aplicacion.Producto(id) ON DELETE CASCADE,
    PRIMARY KEY (pack_id, producto_id)
);

-- Relación de muchos a muchos entre Pack y Pedido
CREATE TABLE aplicacion.Pedido_Pack (
    pack_id INT REFERENCES aplicacion.Pack(id) ON DELETE CASCADE,
    pedido_id INT REFERENCES aplicacion.Pedido(id) ON DELETE CASCADE,
    cantidad INT NOT NULL CHECK (cantidad > 0),
    PRIMARY KEY (pack_id, pedido_id)
);

-- Tabla Pedido_Producto: relación de productos en un pedido, con cantidad
CREATE TABLE aplicacion.Pedido_Producto (
    pedido_id INT REFERENCES aplicacion.Pedido(id) ON DELETE CASCADE,
    producto_id INT REFERENCES aplicacion.Producto(id) ON DELETE CASCADE,
    cantidad INT NOT NULL CHECK (cantidad > 0),
    PRIMARY KEY (pedido_id, producto_id)
);

-- Tabla Adicional
CREATE TABLE aplicacion.Adicional (
    id SERIAL PRIMARY KEY,
    descripcion TEXT,
    costoAdicional NUMERIC(10,2),
    visible BOOLEAN DEFAULT TRUE
);

-- Crear tabla ProductoAdicional para relacionar productos y adicionales
CREATE TABLE aplicacion.Producto_Adicional (
    producto_id INT REFERENCES aplicacion.Producto(id) ON DELETE CASCADE,
    adicional_id INT REFERENCES aplicacion.Adicional(id) ON DELETE CASCADE,
    PRIMARY KEY (producto_id, adicional_id)
);

-- Tabla Pedido_Producto_Adicional: adicionales específicos para productos en un pedido
CREATE TABLE aplicacion.Pedido_Producto_Adicional (
    pedido_id INT REFERENCES aplicacion.Pedido(id) ON DELETE CASCADE,
    producto_id INT REFERENCES aplicacion.Producto(id) ON DELETE CASCADE,
    adicional_id INT REFERENCES aplicacion.Adicional(id) ON DELETE CASCADE,
    PRIMARY KEY (pedido_id, producto_id, adicional_id)
);

-- Índices y restricciones adicionales
CREATE INDEX idx_usuario_email ON gestion_usuarios.Usuario(email);
CREATE INDEX idx_usuario_telefono ON gestion_usuarios.Usuario(telefono);
CREATE UNIQUE INDEX idx_cliente_unico_pedido_en_progreso
ON aplicacion.Pedido (cliente_id)
WHERE estado = 'EN_PROGRESO';

-- Valores de configuración de reglas de negocio
INSERT INTO aplicacion.MetodoPago (nombre, comision) VALUES
    ('TRANSFERENCIA', 0),
    ('MERCADO_PAGO', 1.5),  -- Ejemplo de comisión de 1.5%
    ('EFECTIVO', 0);