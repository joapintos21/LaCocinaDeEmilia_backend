package app.models;

public class CarritoRequest {
    private int userId;
    private int productoId;
    private int cantidad;
    private int adicionalId;

    // Getters y Setters
    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getProductoId() {
        return productoId;
    }

    public void setAdicionalId(int adicionalId) {
        this.adicionalId = adicionalId;
    }

    public int getAdicionalId() {
        return adicionalId;
    }

    public void setProductoId(int productoId) {
        this.productoId = productoId;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }
}