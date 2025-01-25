package app.controllers;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.javalite.activeweb.AppController;
import org.javalite.activeweb.annotations.GET;
import org.json.JSONArray;
import org.json.JSONObject;

import app.models.Pedido;
import app.models.Usuario;

public class DashboardController extends AppController {

    @GET
    public void index() {
        int cantUsuarios = Usuario.findAll().size();

        // Obtener el mes y año actuales
        LocalDate now = LocalDate.now();
        int currentMonth = now.getMonthValue();
        int currentYear = now.getYear();

        // Obtener todos los pedidos
        List<Pedido> pedidos = Pedido.findAll();

        // ---- Pedidos por mes ----
        int[] pedidosPorMes = new int[12]; // Inicializar arreglo para los 12 meses
        Map<Integer, Long> pedidosAgrupadosPorMes = pedidos.stream()
                .filter(p -> p.get("fechaCreacion") != null) // Filtrar nulos
                .collect(Collectors.groupingBy(
                        p -> {
                            LocalDate fechaCreacion = LocalDate.parse(p.getString("fechaCreacion"), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                            return fechaCreacion.getMonthValue(); // Obtener mes como entero (1-12)
                        },
                        Collectors.counting()
                ));

        pedidosAgrupadosPorMes.forEach((mes, cantidad) -> pedidosPorMes[mes - 1] = cantidad.intValue());

        // Total de pedidos acumulados por mes
        int totalPedidosPorMes = 0;
        for (int pedidosMes : pedidosPorMes) {
            totalPedidosPorMes += pedidosMes;
        }

        // ---- Pedidos por semana del mes actual ----
        int[] pedidosPorSemana = new int[5]; // Inicializar arreglo para 5 semanas del mes
        List<Pedido> pedidosDelMesActual = pedidos.stream()
                .filter(p -> {
                    if (p.get("fechaCreacion") != null) {
                        LocalDate fechaCreacion = LocalDate.parse(p.getString("fechaCreacion"), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                        return fechaCreacion.getYear() == currentYear && fechaCreacion.getMonthValue() == currentMonth;
                    }
                    return false;
                })
                .collect(Collectors.toList());

        Map<Integer, Long> pedidosAgrupadosPorSemana = pedidosDelMesActual.stream()
                .collect(Collectors.groupingBy(
                        p -> {
                            LocalDate fechaCreacion = LocalDate.parse(p.getString("fechaCreacion"), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                            return fechaCreacion.getDayOfMonth() / 7; // Semana del mes (0-4)
                        },
                        Collectors.counting()
                ));

        pedidosAgrupadosPorSemana.forEach((semana, cantidad) -> pedidosPorSemana[semana] = cantidad.intValue());

        // Total de pedidos acumulados por semana
        int totalPedidosPorSemana = 0;
        for (int pedidosSemana : pedidosPorSemana) {
            totalPedidosPorSemana += pedidosSemana;
        }

        // ---- Usuarios por mes ----
        int[] usuariosPorMes = new int[12]; // Inicializar arreglo para los 12 meses
        Map<Integer, Long> usuariosAgrupadosPorMes = Usuario.findAll().stream()
                .filter(u -> u.get("fecha_creacion") != null) // Filtrar nulos
                .collect(Collectors.groupingBy(
                        u -> {
                            LocalDate fechaCreacion = LocalDate.parse(u.getString("fecha_creacion"), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                            return fechaCreacion.getMonthValue(); // Obtener mes como entero (1-12)
                        },
                        Collectors.counting()
                ));

        usuariosAgrupadosPorMes.forEach((mes, cantidad) -> usuariosPorMes[mes - 1] = cantidad.intValue());

        // Total de usuarios acumulados por mes
        int totalUsuariosPorMes = 0;
        for (int usuariosMes : usuariosPorMes) {
            totalUsuariosPorMes += usuariosMes;
        }

        // ---- Crear JSON para los datos ----
        JSONObject datos = new JSONObject();

        // Datos de pedidos por mes
        JSONArray dataPedidosPorMes = new JSONArray();
        for (int pedidosMes : pedidosPorMes) {
            dataPedidosPorMes.put(pedidosMes);
        }
        datos.put("dataPedidosPorMes", dataPedidosPorMes);
        datos.put("totalPedidosPorMes", totalPedidosPorMes);  // Total acumulado por mes

        // Datos de pedidos por semana
        JSONArray dataPedidosPorSemana = new JSONArray();
        for (int pedidosSemana : pedidosPorSemana) {
            dataPedidosPorSemana.put(pedidosSemana);
        }
        datos.put("dataPedidosPorSemana", dataPedidosPorSemana);
        datos.put("totalPedidosPorSemana", totalPedidosPorSemana);  // Total acumulado por semana

        // Datos de usuarios por mes
        JSONArray dataUsuariosPorMes = new JSONArray();
        for (int usuariosMes : usuariosPorMes) {
            dataUsuariosPorMes.put(usuariosMes);
        }
        datos.put("dataUsuariosPorMes", dataUsuariosPorMes);
         // Agregar cantidad de usuarios
        datos.put("cantUsuarios", cantUsuarios);

        // Responder con el JSON
        respond(datos.toString()).contentType("application/json");
    }

}
