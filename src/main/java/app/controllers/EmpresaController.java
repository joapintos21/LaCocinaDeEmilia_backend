package app.controllers;

import org.javalite.activeweb.AppController;
import org.javalite.activeweb.annotations.GET;
import org.javalite.activeweb.annotations.PUT;

import app.models.Empresa;

public class EmpresaController extends AppController {

    @GET
    public void index() {
        Empresa empresa = Empresa.findById(1); // Buscar la empresa con ID 1
        if (empresa != null) {
            respond(empresa.toJson().toString()).contentType("application/json");
        } else {
            respond("{\"error\": \"Empresa no encontrada\"}").contentType("application/json").status(404);
        }
    }

    @PUT
    public void update(Empresa empresaRequest) {
        try {
            int id = 1; //Debido a que solo se tiene que tener un registro de empresa a la vez

            Empresa empresa = Empresa.findById(id);

            if (empresa == null) {
                respond("{\"error\": \"Empresa no encontrada. Intente crearla primero.\"}")
                        .contentType("application/json").status(404);
                return;
            }

            String nombre = empresaRequest.getString("nombre");
            String direccion = empresaRequest.getString("direccion");
            String descripcionTransferencia = empresaRequest.getString("descripcion_transferencia");
            String telefono = empresaRequest.getString("telefono");
            String correo = empresaRequest.getString("correo");

            if (nombre != null && !nombre.isEmpty()) {
                empresa.set("nombre", nombre);
            }
            if (direccion != null && !direccion.isEmpty()) {
                empresa.set("direccion", direccion);
            }
            if (descripcionTransferencia != null && !descripcionTransferencia.isEmpty()) {
                empresa.set("descripcion_transferencia", descripcionTransferencia);
            }
            if (telefono != null && !telefono.isEmpty()) {
                empresa.set("telefono", telefono);
            }
            if (correo != null && !correo.isEmpty()) {
                empresa.set("correo", correo);
            }

            if (empresa.save()) {
                respond(empresa.toJson().toString())
                        .contentType("application/json").status(200);
            } else {
                respond("{\"error\": " + empresa.errors().toJSON() + "}")
                        .contentType("application/json")
                        .status(400);
            }
        } catch (Exception e) {
            respond("{\"error\": \"Error interno del servidor: " + e.getMessage() + "\"}")
                    .contentType("application/json").status(500);
        }
    }
}
