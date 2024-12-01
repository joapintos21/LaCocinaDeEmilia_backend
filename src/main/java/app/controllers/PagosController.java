package app.controllers;

import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.preference.PreferenceBackUrlsRequest;
import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceItemRequest;
import com.mercadopago.client.preference.PreferenceRequest;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.preference.Preference;

import app.models.Pago;

import java.math.BigDecimal;
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
			JSONObject body = new JSONObject(getRequestString());
			JSONArray itemsArray = body.getJSONArray("items");
			JSONObject urls = body.getJSONObject("backUrls");
			Integer userId = body.getInt("userId");
			// Agrega credenciales
			MercadoPagoConfig
					.setAccessToken("APP_USR-411942404565714-112820-46da2a5cc4a2a44f087cb876ffa9eb84-2125634042");

			List<PreferenceItemRequest> itemList = new ArrayList<>();
			for (int i = 0; i < itemsArray.length(); i++) {
				JSONObject jsonObject = itemsArray.getJSONObject(i);
				PreferenceItemRequest item = PreferenceItemRequest.builder()
						.id("1234")
						.title(jsonObject.getString("title"))
						.description(jsonObject.optString("description", "Sin descripción"))
						.pictureUrl("http://picture.com/PS5")
						.categoryId("games")
						.quantity(jsonObject.getInt("quantity"))
						.currencyId("BRL")
						.unitPrice(jsonObject.getBigDecimal("unitPrice"))
						.build();
				itemList.add(item);
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
					.externalReference(Integer.toString(userId)) // Número de pedido interno
					.notificationUrl(
							"https://a177-2800-a4-2994-df00-4c76-c6d-9158-9582.ngrok-free.app/hooks/handleWebhook")
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