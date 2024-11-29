package app.controllers;

import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceItemRequest;
import com.mercadopago.client.preference.PreferencePayerRequest;
import com.mercadopago.client.preference.PreferenceRequest;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.preference.Preference;
import com.mercadopago.resources.preference.PreferencePayer;

import java.math.BigDecimal;
import java.util.Arrays;

import org.javalite.activeweb.AppController;
import org.javalite.activeweb.annotations.POST;

public class PagosController extends AppController {

    @POST
    public void crearPreferencia() {
        try {
            // Agrega credenciales
            MercadoPagoConfig
                    .setAccessToken("APP_USR-411942404565714-112820-46da2a5cc4a2a44f087cb876ffa9eb84-2125634042");

            // Crea un ítem en la preferencia
            PreferenceItemRequest item = PreferenceItemRequest.builder()
                    .title("Nombre del producto")
                    .quantity(1)
                    .unitPrice(new BigDecimal("100.0"))
                    .build();

            // Crea la preferencia
            PreferenceRequest request = PreferenceRequest.builder()
                    .items(Arrays.asList(item))
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