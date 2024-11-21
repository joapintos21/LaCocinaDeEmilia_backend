package app.controllers;

import app.models.DescuentoPack;
import app.models.Pack;
import app.models.PackProducto;
import app.models.PackRequest;
import app.models.Producto;
import org.javalite.activeweb.AppController;
import org.javalite.activeweb.annotations.DELETE;
import org.javalite.activeweb.annotations.GET;
import org.javalite.activeweb.annotations.POST;
import org.javalite.activeweb.annotations.PUT;
import org.json.JSONArray;
import org.json.JSONObject;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.stream.Collectors;

public class PacksController extends AppController {
    private ObjectMapper mapper = new ObjectMapper();

    // Crear un pack con sus productos
    @POST
    public void create() {
        try {
            // Deserializar el JSON recibido en un objeto PackRequest
            PackRequest packRequest = mapper.readValue(getRequestString(), PackRequest.class);

            // Crear el Pack a partir de los datos deserializados
            Pack pack = new Pack();
            pack.set("nombre", packRequest.getNombre());
            pack.set("descripcion", packRequest.getDescripcion());
            pack.set("precio", packRequest.getPrecio());

            if (pack.saveIt()) {
                // Asociar productos al Pack
                List<PackRequest.ProductoRequest> productos = packRequest.getProductos();
                if (productos != null && !productos.isEmpty()) {
                    for (PackRequest.ProductoRequest productoRequest : productos) {
                        Producto producto = Producto.findById(productoRequest.getProductoId());
                        if (producto != null) {
                            // Crear la relación en PackProducto
                            new PackProducto()
                                    .set("pack_id", pack.getId())
                                    .set("producto_id", producto.getId())
                                    .set("cantidad", productoRequest.getCantidad())
                                    .saveIt();
                        }
                    }
                }

                respond(pack.toJson().toString()).contentType("application/json").status(201);
            } else {
                respond(pack.errors().toJSON()).contentType("application/json").status(400);
            }
        } catch (Exception e) {
            String errorJson = "{\"error\": \"" + e.getMessage() + "\"}";
            respond(errorJson).contentType("application/json").status(500);
        }
    }

    // Actualizar un pack
    @PUT
    public void update() {
        try {
            int id = Integer.parseInt(param("id")); // Obtener el ID del pack a actualizar
            Pack pack = Pack.findById(id);

            if (pack == null) {
                respond("{\"error\": \"Pack no encontrado.\"}").contentType("application/json").status(404);
                return;
            }

            // Deserializar el JSON recibido en un objeto PackRequest
            PackRequest packRequest = mapper.readValue(getRequestString(), PackRequest.class);

            // Actualizar los datos del pack
            pack.set("nombre", packRequest.getNombre());
            pack.set("descripcion", packRequest.getDescripcion());
            pack.set("precio", packRequest.getPrecio());

            if (pack.saveIt()) {
                // Obtener los productos actualmente asociados al pack
                List<PackProducto> productosActuales = PackProducto.where("pack_id = ?", pack.getId());

                // Crear una lista de IDs de productos nuevos recibidos
                List<Integer> nuevosProductoIds = packRequest.getProductos()
                        .stream()
                        .map(PackRequest.ProductoRequest::getProductoId)
                        .collect(Collectors.toList());

                // Eliminar productos que ya no están en la lista
                for (PackProducto packProducto : productosActuales) {
                    if (!nuevosProductoIds.contains(packProducto.get("producto_id"))) {
                        packProducto.delete();
                    }
                }

                // Asociar o actualizar productos
                for (PackRequest.ProductoRequest productoRequest : packRequest.getProductos()) {
                    Producto producto = Producto.findById(productoRequest.getProductoId());
                    if (producto != null) {
                        PackProducto packProducto = PackProducto.findFirst(
                                "pack_id = ? AND producto_id = ?",
                                pack.getId(),
                                producto.getId());

                        if (packProducto == null) {
                            // Si no existe la relación, se crea una nueva
                            new PackProducto()
                                    .set("pack_id", pack.getId())
                                    .set("producto_id", producto.getId())
                                    .set("cantidad", productoRequest.getCantidad())
                                    .saveIt();
                        } else {
                            // Si ya existe, actualizamos la cantidad
                            packProducto.set("cantidad", productoRequest.getCantidad());
                            packProducto.saveIt();
                        }
                    }
                }

                respond(pack.toJson().toString()).contentType("application/json").status(200);
            } else {
                respond(pack.errors().toJSON()).contentType("application/json").status(400);
            }
        } catch (Exception e) {
            String errorJson = "{\"error\": \"" + e.getMessage() + "\"}";
            respond(errorJson).contentType("application/json").status(500);
        }
    }

    // Obtener todos los packs
    @GET
    public void index() {
        List<Pack> packs = Pack.findAll();
        JSONArray jsonArray = new JSONArray();
        packs.forEach(pack -> jsonArray.put(pack.toJson()));
        respond(jsonArray.toString()).contentType("application/json");
    }

    // Obtener un pack por ID
    @GET
    public void show() {
        int id = Integer.parseInt(param("id"));
        Pack pack = Pack.findById(id);

        if (pack != null) {
            respond(pack.toJson().toString()).contentType("application/json").status(200);
        } else {
            respond("Pack no encontrado").contentType("application/json").status(404);
        }
    }

    // Eliminar un pack
    @DELETE
    public void delete() {
        int id = Integer.parseInt(param("id"));
        Pack pack = Pack.findById(id);
        if (pack != null) {
            pack.delete();
            respond("Pack eliminado correctamente").contentType("application/json").status(200);
        } else {
            respond("Pack no encontrado").contentType("application/json").status(404);
        }
    }

    @POST
    public void crearDescuento() {
        try {
            // Parsear el cuerpo de la solicitud
            JSONObject body = new JSONObject(getRequestString());
            int packId = body.getInt("pack_id"); // ID del pack al que se le asignará el descuento
            int descuentoId = body.getInt("descuento_id"); // ID del descuento a asignar

            // Crear la relación entre el pack y el descuento
            DescuentoPack descuentoPack = new DescuentoPack();
            descuentoPack.set("pack_id", packId);
            descuentoPack.set("descuento_id", descuentoId);

            // Guardar la relación en la base de datos
            if (descuentoPack.saveIt()) {
                respond(descuentoPack.toJson().toString())
                        .contentType("application/json")
                        .status(201); // Creado
            } else {
                respond(descuentoPack.errors().toJSON())
                        .contentType("application/json")
                        .status(400); // Error en la solicitud
            }
        } catch (Exception e) {
            // Manejar excepciones y devolver un error genérico
            respond("{\"error\": \"" + e.getMessage() + "\"}")
                    .contentType("application/json")
                    .status(500); // Error interno del servidor
        }
    }
}