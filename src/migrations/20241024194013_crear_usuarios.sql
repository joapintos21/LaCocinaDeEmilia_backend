-- SCHEMA: aplicacion

-- DROP SCHEMA IF EXISTS aplicacion ;

CREATE SCHEMA IF NOT EXISTS aplicacion
    AUTHORIZATION postgres;

-- SCHEMA: auditoria

-- DROP SCHEMA IF EXISTS auditoria ;

CREATE SCHEMA IF NOT EXISTS auditoria
    AUTHORIZATION postgres;

-- SCHEMA: gestion_usuarios

-- DROP SCHEMA IF EXISTS gestion_usuarios ;

CREATE SCHEMA IF NOT EXISTS gestion_usuarios
    AUTHORIZATION postgres;

-- Tabla Usuario
CREATE TABLE gestion_usuarios.Usuario (
    id SERIAL PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    nombre VARCHAR(255) NOT NULL,
    telefono VARCHAR(20) UNIQUE NOT NULL
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

CREATE TABLE gestion_usuarios.Nutricionista (
    id SERIAL PRIMARY KEY,
    saldo NUMERIC(10,2),
    cliente_id INT REFERENCES gestion_usuarios.Cliente(id) ON DELETE CASCADE
);

-- Tabla Direccion
CREATE TABLE aplicacion.Direccion (
    id SERIAL PRIMARY KEY,
    cliente_id INT REFERENCES gestion_usuarios.Cliente(id) ON DELETE CASCADE,
    titulo VARCHAR(255) NOT NULL,
    predeterminado BOOLEAN NOT NULL,
    latitud VARCHAR(50),
    longitud VARCHAR(50)
);
