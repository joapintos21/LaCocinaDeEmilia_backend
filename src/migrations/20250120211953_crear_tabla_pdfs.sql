CREATE TABLE aplicacion.productos_pdfs (
    id SERIAL PRIMARY KEY,
    producto_id INT NOT NULL,
    pdf_url TEXT NOT NULL,
    FOREIGN KEY (producto_id) REFERENCES aplicacion.Producto(id) ON DELETE CASCADE
);