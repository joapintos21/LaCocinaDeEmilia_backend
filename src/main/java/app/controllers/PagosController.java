package app.controllers;

import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceItemRequest;
import com.mercadopago.client.preference.PreferenceRequest;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.preference.Preference;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.javalite.activeweb.AppController;
import org.javalite.activeweb.annotations.POST;
import org.json.JSONArray;
import org.json.JSONObject;

public class PagosController extends AppController {

    @POST
    public void crearPreferencia() {
        try {
            // Agrega credenciales
            MercadoPagoConfig.setAccessToken("TEST-7255651511582069-112507-5225cd18822ac4c73e606697d0af7430-175019083");
            PreferenceItemRequest itemRequest = PreferenceItemRequest.builder()
                    .id("1234")
                    .title("Games")
                    .description("PS5")
                    .pictureUrl("http://picture.com/PS5")
                    .categoryId("games")
                    .quantity(2)
                    .currencyId("BRL")
                    .unitPrice(new BigDecimal("4000"))
                    .build();
            List<PreferenceItemRequest> items = new ArrayList<>();
            items.add(itemRequest);
            PreferenceRequest preferenceRequest = PreferenceRequest.builder()
                    .items(items).build();
            PreferenceClient client = new PreferenceClient();
            Preference preference = client.create(preferenceRequest);

            // Inicializar Mercado Pago con tu Access Token
            // MercadoPago.SDK.setAccessToken("TEST-7255651511582069-112507-5225cd18822ac4c73e606697d0af7430-175019083");

            // JSONObject body = new JSONObject(getRequestString());
            // // Crear los detalles de los ítems del carrito
            // JSONArray itemsArray = body.getJSONArray("items");

            // List<Item> items = new ArrayList<>();
            // for (int i = 0; i < itemsArray.length(); i++) {
            // JSONObject itemJson = itemsArray.getJSONObject(i);
            // Item item = new Item()
            // .setTitle(itemJson.getString("title"))
            // .setQuantity(itemJson.getInt("quantity"))
            // .setUnitPrice(itemJson.getBigDecimal("unitPrice").floatValue());
            // items.add(item);
            // }

            // // Crear la preferencia
            // Preference preference = new Preference();
            // preference.setItems(new ArrayList<>(items));

            // // Configurar URLs de callback
            // preference.setBackUrls(
            // new BackUrls()
            // .setSuccess("https://tuapp.com/pago/success")
            // .setPending("https://tuapp.com/pago/pending")
            // .setFailure("https://tuapp.com/pago/failure"));
            // preference.setAutoReturn(Preference.AutoReturn.approved);

            // // Guardar la preferencia
            // preference.save();

            // // Retornar la URL de pago
            respond("{\"preference_id\": \"" + preference.getId() + "\"}")
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