package app.controllers;

import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.preference.PreferenceBackUrlsRequest;
import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceItemRequest;
import com.mercadopago.client.preference.PreferenceRequest;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.preference.Preference;

import app.models.Direccion;
import app.models.Envio;
import app.models.Pack;
import app.models.Pago;
import app.models.Pedido;
import app.models.PedidoPack;
import app.models.PedidoProducto;
import app.models.Producto;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.javalite.activeweb.AppController;
import org.javalite.activeweb.annotations.POST;
import org.json.JSONArray;
import org.json.JSONObject;

public class PagosController extends AppController {

	@POST
	public void registrarPago() {
		try {
			JSONObject body = new JSONObject(getRequestString());

			int pedidoId = body.getInt("pedido_id");
			int metodoPagoId = body.getInt("metodo_pago_id");
			String estadoPago = body.getString("estado_pago");
			BigDecimal monto = body.getBigDecimal("monto");
			String referencia = body.optString("referencia", null); // Campo opcional
			String observaciones = body.optString("observaciones", null);

			Pago pago = new Pago();
			pago.set("pedido_id", pedidoId);
			pago.set("metodo_pago_id", metodoPagoId);
			pago.set("estado_pago", estadoPago);
			pago.set("monto", monto);
			pago.set("referencia", referencia);
			pago.set("observaciones", observaciones);

			if (pago.saveIt()) {
				respond("{\"message\": \"Pago registrado exitosamente.\"}")
						.contentType("application/json")
						.status(200);
			} else {
				respond("{\"error\": \"No se pudo registrar el pago.\"}")
						.contentType("application/json")
						.status(400);
			}
		} catch (Exception e) {
			respond("{\"error\": \"" + e.getMessage() + "\"}")
					.contentType("application/json")
					.status(500);
		}
	}

	@POST
	public void crearPreferencia() {
		try {
			int userIdFromToken = (Integer) appContext().get("userId");
			JSONObject body = new JSONObject(getRequestString());
			JSONObject urls = body.getJSONObject("backUrls");
			Integer direccionId = body.getInt("direccion_id");
			int pedidoId = body.getInt("pedido_id");
			String fechaEnvio = body.getString("fecha_envio");
			// Agrega credenciales
			MercadoPagoConfig
					.setAccessToken("APP_USR-411942404565714-112820-46da2a5cc4a2a44f087cb876ffa9eb84-2125634042");

			// Recupera los productos y packs asociados al pedido
			Pedido pedido = Pedido.findById(pedidoId);
			if (pedido == null) {
				respond("{\"error\": \"Pedido no encontrado.\"}")
						.contentType("application/json")
						.status(404);
				return;
			}
			Direccion d = Direccion.findFirst("id = ?", direccionId);

			if (d == null) {
				respond("{\"error\": \"Direccion no encontrada.\"}")
						.contentType("application/json")
						.status(404);
				return;
			}
			// obtengo el envio y guardo la fecha de envio
			Envio e = Envio.findFirst("pedido_id = ?", pedidoId);
			LocalDate fechaEnvioDate = LocalDate.parse(fechaEnvio);
			e.set("fechaEnvio", Date.valueOf(fechaEnvioDate));
			e.saveIt();

			double envio = d.calcularCostoEnvio();
			BigDecimal costoEnvio = envio > 0 ? BigDecimal.valueOf(envio) : BigDecimal.ZERO;

			List<PedidoProducto> itemsProductos = PedidoProducto.where("pedido_id = ?", pedidoId);
			List<PedidoPack> itemsPacks = PedidoPack.where("pedido_id = ?", pedidoId);
			BigDecimal subtotal = BigDecimal.ZERO;

			// Calcular subtotal, descuentos, y totales
			JSONArray itemsArray = new JSONArray();
			for (PedidoProducto item : itemsProductos) {
				Producto p = Producto.findFirst("id = ?", item.get("producto_id"));
				BigDecimal precioConDescuento = p.getPrecioTotalConDescuento();
				subtotal = subtotal.add(precioConDescuento.multiply(BigDecimal.valueOf(item.getInteger("cantidad"))));

				itemsArray.put(new JSONObject()
						.put("title", p.getString("descripcion"))
						.put("quantity", item.getInteger("cantidad"))
						.put("unitPrice", precioConDescuento));
			}

			for (PedidoPack itemPack : itemsPacks) {
				Pack p = Pack.findFirst("id = ?", itemPack.get("pack_id"));
				BigDecimal precioConDescuento = p.getPrecioTotalConDescuento();
				subtotal = subtotal
						.add(precioConDescuento.multiply(BigDecimal.valueOf(itemPack.getInteger("cantidad"))));

				itemsArray.put(new JSONObject()
						.put("title", "Pack: " + p.getString("nombre"))
						.put("quantity", itemPack.getInteger("cantidad"))
						.put("unitPrice", precioConDescuento));
			}

			// Agregar costos de envío y comisiones
			BigDecimal porcentajeComision = BigDecimal.valueOf(0.0599);
			subtotal = subtotal.add(costoEnvio);
			BigDecimal comision = subtotal.multiply(porcentajeComision);
			// Agregar comisiones y costos de envío como items adicionales
			itemsArray.put(new JSONObject()
					.put("title", "Costo de envío")
					.put("quantity", 1)
					.put("unitPrice", costoEnvio));

			itemsArray.put(new JSONObject()
					.put("title", "Comisión por método de pago")
					.put("quantity", 1)
					.put("unitPrice", comision));
			int counter = 0;
			String itemId = "item-0";
			List<PreferenceItemRequest> itemList = new ArrayList<>();
			for (int i = 0; i < itemsArray.length(); i++) {
				JSONObject jsonObject = itemsArray.getJSONObject(i);
				PreferenceItemRequest item = PreferenceItemRequest.builder()
						.id(itemId)
						.title(jsonObject.getString("title"))
						.description(jsonObject.optString("description", "Sin descripción"))
						.pictureUrl("http://picture.com/PS5")
						.categoryId("games")
						.quantity(jsonObject.getInt("quantity"))
						.currencyId("UYU")
						.unitPrice(jsonObject.getBigDecimal("unitPrice"))
						.build();
				itemList.add(item);
				counter++;
				itemId = "item-" + counter;
			}
			PreferenceBackUrlsRequest backUrls = PreferenceBackUrlsRequest.builder()
					.success(urls.getString("success"))
					.pending(urls.getString("pending"))
					.failure(urls.getString("failure"))
					.build();

			// Crea la preferencia
			PreferenceRequest request = PreferenceRequest.builder()
					.items(itemList)
					.backUrls(backUrls)
					.externalReference(Integer.toString(userIdFromToken)) // Número de pedido interno
					.notificationUrl(
							"https://eddb-2800-a4-29dc-bb00-7d24-90af-803a-e96c.ngrok-free.app/hooks/handleWebhook")
					.autoReturn("approved") // Retorno automático al éxito
					.build();

			// Guarda la preferencia y obtén el ID
			PreferenceClient client = new PreferenceClient();
			Preference preference = client.create(request);
			String preferenceId = preference.getId();
			respond("{\"preference_id\": \"" + preferenceId + "\"}")
					.contentType("application/json")
					.status(200);
		} catch (MPException e) {
			respond("{\"error\": \"" + e.getMessage() + "\"}")
					.contentType("application/json")
					.status(500);
		} catch (MPApiException e) {
			respond("{\"error\": \"" + e.getMessage() + "\"}")
					.contentType("application/json")
					.status(500);
		}
	}

}