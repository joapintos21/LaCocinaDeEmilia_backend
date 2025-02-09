package app.controllers;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javalite.activeweb.AppController;
import org.javalite.activeweb.annotations.POST;
import org.json.JSONArray;
import org.json.JSONObject;

import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.preference.PreferenceBackUrlsRequest;
import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceItemRequest;
import com.mercadopago.client.preference.PreferenceRequest;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.preference.Preference;

import app.models.Adicional;
import app.models.CuponNutricionista;
import app.models.Descuento;
import app.models.Direccion;
import app.models.Pack;
import app.models.Pago;
import app.models.Pedido;
import app.models.PedidoPack;
import app.models.PedidoProducto;
import app.models.PedidoProductoAdicional;
import app.models.Producto;

public class PagosController extends AppController {

	private static final Logger logger = LogManager.getLogger(PagosController.class);

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
				logger.info("Pago registrado exitosamente: " + pago.toString());
				respond("{\"message\": \"Pago registrado exitosamente.\"}")
						.contentType("application/json")
						.status(200);
			} else {
				logger.error("No se pudo registrar el pago: " + pago.toString());
				respond("{\"error\": \"No se pudo registrar el pago.\"}")
						.contentType("application/json")
						.status(400);
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
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
			String deliveryMethod = body.getString("delivery_method");
			int pedidoId = body.getInt("pedido_id");
			int cuponId = body.optInt("cupon", 0);
			// Agrega credenciales
			MercadoPagoConfig
					.setAccessToken("APP_USR-411942404565714-112820-46da2a5cc4a2a44f087cb876ffa9eb84-2125634042");

			// Recupera los productos y packs asociados al pedido
			Pedido pedido = Pedido.findById(pedidoId);
			if (pedido == null) {
				logger.error("Pedido no encontrado. ON crearPreferencia");
				respond("{\"error\": \"Pedido no encontrado.\"}")
						.contentType("application/json")
						.status(404);
				return;
			}
			Direccion direccion = null;
			double envio = 0;
			if (deliveryMethod.equals("delivery")) {
				direccion = Direccion.findById(direccionId);
				if (direccion == null) {
					logger.error("Direccion no encontrada. ON crearPreferencia");
					respond("{\"error\": \"Direccion no encontrada.\"}")
							.contentType("application/json")
							.status(404);
					return;
				} else {
					envio = direccion.calcularCostoEnvio();
				}
			}

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

				// Verificar si hay un adicional asociado al PedidoProducto
				List<PedidoProductoAdicional> pedidoAdicionales = PedidoProductoAdicional.where(
						"pedido_producto_id = ?", item.getId());
				for (PedidoProductoAdicional pedidoAdicional : pedidoAdicionales) {
					Adicional adicional = Adicional.findFirst("id = ?", pedidoAdicional.get("adicional_id"));
					BigDecimal costoAdicional = adicional.getBigDecimal("costoadicional");
					subtotal = subtotal.add(costoAdicional.multiply(BigDecimal.valueOf(item.getInteger("cantidad"))));

					itemsArray.put(new JSONObject()
							.put("title", "Adicional: " + adicional.getString("descripcion"))
							.put("quantity", item.getInteger("cantidad"))
							.put("unitPrice", costoAdicional));
				}
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
			subtotal = subtotal.add(comision);
			// Agregar comisiones y costos de envío como items adicionales
			if (cuponId > 0) {
				CuponNutricionista cupon = CuponNutricionista.findById(cuponId);
				if (cupon != null) {
					Descuento desc = Descuento.findFirst("id = ?", cupon.get("descuento_id"));
					BigDecimal importeDescuento = subtotal.multiply(desc.getBigDecimal("valor"));
					subtotal = subtotal.subtract(importeDescuento);
					itemsArray.put(new JSONObject()
							.put("title", "Cupon: " + cupon.getString(("codigo")))
							.put("quantity", 1)
							.put("unitPrice", importeDescuento.negate()));
				}
			}

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
			logger.info("{\"preference_id\": \"" + preferenceId + "\"}");
			respond("{\"preference_id\": \"" + preferenceId + "\"}")
					.contentType("application/json")
					.status(200);
		} catch (MPException e) {
			logger.error(e.getMessage());
			respond("{\"error\": \"" + e.getMessage() + "\"}")
					.contentType("application/json")
					.status(500);
		} catch (MPApiException e) {
			logger.error(e.getMessage());
			respond("{\"error\": \"" + e.getMessage() + "\"}")
					.contentType("application/json")
					.status(500);
		}
	}

}