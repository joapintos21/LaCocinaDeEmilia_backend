package app.models;

import java.util.List;

public class PackRequest {
    private String nombre;
    private String descripcion;
    private Double precio;
    private List<ProductoRequest> productos;

    // Getters y Setters
    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Double getPrecio() {
        return precio;
    }

    public void setPrecio(Double precio) {
        this.precio = precio;
    }

    public List<ProductoRequest> getProductos() {
        return productos;
    }

    public void setProductos(List<ProductoRequest> productos) {
        this.productos = productos;
    }

    public static class ProductoRequest {
        private Integer productoId;
        private Integer cantidad;

        // Getters y Setters
        public Integer getProductoId() {
            return productoId;
        }

        public void setProductoId(Integer productoId) {
            this.productoId = productoId;
        }

        public Integer getCantidad() {
            return cantidad;
        }

        public void setCantidad(Integer cantidad) {
            this.cantidad = cantidad;
        }
    }
}