package app.controllers;

import java.math.BigDecimal;

import org.javalite.activeweb.AppController;
import org.javalite.activeweb.annotations.POST;

import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.resources.payment.Payment;

import app.models.Cliente;
import app.models.Pago;
import app.models.Pedido;

public class HooksController extends AppController {
    @POST
    public void handleWebhook() {
        String paymentId = param("id");
        String topic = param("topic");
        if ("payment".equals(topic)) {
            // Procesa el estado del pago (aprobado, pendiente, rechazado)
            processPayment(Long.parseLong(paymentId));
        }
        respond("OK").status(200);
    }

    private void processPayment(Long paymentId) {
        PaymentClient paymentClient = new PaymentClient();
        try {
            Payment payment = paymentClient.get(paymentId);

            String status = payment.getStatus(); // Estado del pago
            String externalReference = payment.getExternalReference(); // ID del usuario asociado
            BigDecimal transactionAmount = payment.getTransactionAmount(); // Monto total del pago
            Integer userId = Integer.parseInt(externalReference);
            System.out.println("Estado del pago: " + status);
            System.out.println("Usuario asociado (external_reference): " + userId);

            // Buscar el cliente en la tabla Cliente usando el usuario_id
            Cliente cliente = Cliente.findFirst("usuario_id = ?", userId);
            if (cliente == null) {
                System.err.println("No se encontró un cliente para el usuario con ID: " + userId);
                return;
            }

            Long clienteId = cliente.getLong("id");

            // Buscar el pedido activo del cliente
            Pedido pedido = Pedido.findFirst("cliente_id = ? AND estado = ?", clienteId, 1); // Estado 1: EN_PROGRESO
            if (pedido == null) {
                System.err.println("No se encontró un pedido EN_PROGRESO para el cliente con ID: " + clienteId);
                return;
            }

            // Actualizar el estado del pedido en base al estado del pago
            if ("approved".equals(status)) {
                // Si el pago fue aprobado, mover el pedido a "LISTO_PARA_PREPARAR" (estado 2)
                pedido.set("estado", 2);
                System.out.println("Pago aprobado. Pedido marcado como LISTO_PARA_PREPARAR.");
                status = "APROBADO";
            } else if ("in_process".equals(status) || "rejected".equals(status)) {
                // Si el pago está pendiente o rechazado, mantener el pedido en "EN_PROGRESO"
                // (estado 1)
                if ("in_process".equals(status)) {
                    status = "PENDIENTE";
                } else {
                    status = "RECHAZADO";
                }
                pedido.set("estado", 1);
                System.out.println("Pago " + status + ". Pedido permanece en EN_PROGRESO.");
            }

            // Guardar los cambios en el pedido
            pedido.saveIt();

            // Opcional: Registrar detalles del pago en una tabla de pagos
            Pago pago = new Pago();
            pago.set("pedido_id", pedido.getId());
            pago.set("metodo_pago_id", 2);
            pago.set("referencia", paymentId);
            pago.set("monto", transactionAmount);
            pago.set("estado_pago", status);
            pago.saveIt();

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error al procesar el pago: " + e.getMessage());
        }
    }
}
