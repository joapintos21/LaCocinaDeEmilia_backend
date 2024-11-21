package app.controllers;

import app.models.Descuento;
import app.models.TipoDescuento;
import org.javalite.activeweb.AppController;
import org.javalite.activeweb.annotations.GET;
import org.javalite.activeweb.annotations.POST;
import org.javalite.activeweb.annotations.PUT;
import org.javalite.activeweb.annotations.DELETE;
import org.json.JSONArray;
import java.sql.Date;
import java.util.List;

public class DescuentosController extends AppController {

    // Crear un nuevo descuento
    @POST
    public void create(Descuento descuento) {
        try {
            Descuento desc = new Descuento();
            // Obtener el nombre del tipo de descuento del parámetro
            Integer tipoNombre = descuento.getInteger("tipo_id");
            TipoDescuento tipoDescuento = TipoDescuento.findFirst("id = ?", tipoNombre);

            if (tipoDescuento == null) {
                respond("{\"error\": \"Tipo de descuento inválido\"}").contentType("application/json").status(400);
                return;
            }
            desc.set("tipo_id", tipoDescuento.getId());
            desc.set("valor", descuento.get("valor"));
            // Convertir las fechas a java.sql.Date
            String fechaInicio = descuento.getString("fechainicio");
            String fechaFin = descuento.getString("fechafin");

            desc.set("fechainicio", Date.valueOf(fechaInicio)); // Convertir a java.sql.Date

            if (fechaFin != null && !fechaFin.isEmpty()) {
                desc.set("fechafin", Date.valueOf(fechaFin)); // Convertir a java.sql.Date
            } else {
                desc.set("fechafin", null);
            }
            desc.set("descripcion", descuento.get("descripcion"));

            if (desc.save()) {
                respond(desc.toJson().toString()).contentType("application/json").status(200);
            } else {
                respond(desc.errors().toJSON()).contentType("application/json").status(400);
            }
        } catch (IllegalArgumentException e) {
            respond("{\"error\": \"Tipo de descuento inválido. Solo se aceptan 'PORCENTAJE' o 'IMPORTE'.\"}")
                    .contentType("application/json").status(400);
        } catch (Exception e) {
            respond("{\"error\": \"" + e.getMessage() + "\"}").contentType("application/json").status(500);
        }
    }

    // Obtener todos los descuentos
    @GET
    public void index() {
        List<Descuento> descuentos = Descuento.findAll();
        JSONArray jsonArray = new JSONArray();
        descuentos.forEach(descuento -> jsonArray.put(descuento.toJson()));
        respond(jsonArray.toString()).contentType("application/json");
    }

    // Obtener un descuento por ID
    @GET
    public void show() {
        int id = Integer.parseInt(param("id"));
        Descuento descuento = Descuento.findById(id);

        if (descuento != null) {
            respond(descuento.toJson().toString()).contentType("application/json").status(200);
        } else {
            respond("{\"error\": \"Descuento no encontrado\"}").contentType("application/json").status(404);
        }
    }

    // Actualizar un descuento
    @PUT
    public void update(Descuento newDescuento) {
        int id = Integer.parseInt(param("id"));
        Descuento descuento = Descuento.findById(id);

        if (descuento != null) {
            try {
                // Obtener el tipo de descuento
                Integer tipoId = newDescuento.getInteger("tipo_id");
                TipoDescuento tipoDescuento = TipoDescuento.findFirst("id = ?", tipoId);

                if (tipoDescuento == null) {
                    respond("{\"error\": \"Tipo de descuento inválido\"}").contentType("application/json").status(400);
                    return;
                }

                // Actualizar los campos
                descuento.set("tipo_id", tipoDescuento.getId());
                descuento.set("valor", newDescuento.get("valor"));

                // Convertir las fechas a java.sql.Date
                String fechaInicio = newDescuento.getString("fechainicio");
                String fechaFin = newDescuento.getString("fechafin");

                descuento.set("fechainicio", Date.valueOf(fechaInicio)); // Convertir a java.sql.Date

                if (fechaFin != null && !fechaFin.isEmpty()) {
                    descuento.set("fechafin", Date.valueOf(fechaFin)); // Convertir a java.sql.Date
                } else {
                    descuento.set("fechafin", null);
                }

                descuento.set("descripcion", newDescuento.get("descripcion"));

                // Guardar el descuento actualizado
                if (descuento.saveIt()) {
                    respond(descuento.toJson().toString()).contentType("application/json").status(200);
                } else {
                    respond(descuento.errors().toJSON()).contentType("application/json").status(400);
                }
            } catch (IllegalArgumentException e) {
                respond("{\"error\": \"Fechas inválidas o tipo de descuento no válido.\"}")
                        .contentType("application/json").status(400);
            } catch (Exception e) {
                String errorJson = "{\"error\": \"" + e.getMessage() + "\"}";
                respond(errorJson).contentType("application/json").status(500);
            }
        } else {
            respond("{\"error\": \"Descuento no encontrado\"}").contentType("application/json").status(404);
        }
    }

    // Eliminar un descuento
    @DELETE
    public void delete() {
        int id = Integer.parseInt(param("id"));
        Descuento descuento = Descuento.findById(id);

        if (descuento != null) {
            descuento.delete();
            respond("{\"message\": \"Descuento eliminado correctamente\"}").status(200);
        } else {
            respond("{\"error\": \"Descuento no encontrado\"}").contentType("application/json").status(404);
        }
    }
}