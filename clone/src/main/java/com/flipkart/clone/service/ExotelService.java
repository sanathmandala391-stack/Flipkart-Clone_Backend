package com.flipkart.clone.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service
@Slf4j
public class ExotelService {

    @Value("${exotel.account-sid}")
    private String accountSid;

    @Value("${exotel.api-key}")
    private String apiKey;

    @Value("${exotel.api-token}")
    private String apiToken;

    @Value("${exotel.caller-id}")
    private String callerId;

    @Value("${exotel.subdomain:api.exotel.com}")
    private String subdomain;

    // Your Exotel App ID from App Bazaar
    private static final String APP_ID = "1248728";

    private final HttpClient httpClient = HttpClient.newHttpClient();

    // ── Make a call using your Exotel app ─────────────────────
    public void makeCall(String toPhone, String message) {
        try {
            String formattedPhone = formatPhone(toPhone);
            if (formattedPhone == null || formattedPhone.isBlank()) {
                log.warn("⚠️ Invalid phone number — skipping call");
                return;
            }

            // Exotel Say URL — passes message as URL param
            String sayUrl =
                    "http://my.exotel.com/" + accountSid +
                            "/exoml/start_voice/" + APP_ID +
                            "?message=" + encode(message);

            String body =
                    "From="     + encode(callerId)       + "&" +
                            "To="       + encode(formattedPhone) + "&" +
                            "Url="      + encode(sayUrl)          + "&" +
                            "CallType=trans";

            String auth = Base64.getEncoder().encodeToString(
                    (apiKey + ":" + apiToken)
                            .getBytes(StandardCharsets.UTF_8)
            );

            String url = String.format(
                    "https://%s/v1/Accounts/%s/Calls/connect.json",
                    subdomain, accountSid
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "Basic " + auth)
                    .header("Content-Type",
                            "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response =
                    httpClient.send(request,
                            HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200 ||
                    response.statusCode() == 201) {
                log.info("✅ Call placed to {}", formattedPhone);
            } else {
                log.error("❌ Call failed [{}}]: {}",
                        response.statusCode(), response.body());
            }

        } catch (Exception e) {
            log.error("❌ Exotel error: {}", e.getMessage());
        }
    }

    // ── Order Placed ───────────────────────────────────────────
    public void callOrderPlaced(String phone,
                                String customerName,
                                Long orderId,
                                String amount) {
        String msg =
                "Namaskaram " + customerName + " garu. " +
                        "Meeru order chesina order number " + orderId +
                        " vijayavanthamga place ayindi. " +
                        "Mottham " + amount + " rupayalu. " +
                        "Meeru order tvaralona deliver cheyabadutundi. " +
                        "Dhanyavadamulu.";
        makeCall(phone, msg);
    }

    // ── Order Shipped ──────────────────────────────────────────
    public void callOrderShipped(String phone,
                                 String customerName,
                                 Long orderId) {
        String msg =
                "Namaskaram " + customerName + " garu. " +
                        "Meeru order number " + orderId +
                        " ship cheyabadindi. " +
                        "Tvaralona meeru daggariki cherukoontundi. " +
                        "Dhanyavadamulu.";
        makeCall(phone, msg);
    }

    // ── Out for Delivery ───────────────────────────────────────
    public void callOutForDelivery(String phone,
                                   String customerName,
                                   Long orderId) {
        String msg =
                "Namaskaram " + customerName + " garu. " +
                        "Meeru order number " + orderId +
                        " delivery ki bayaluderidhi. " +
                        "Ee roju meeru ki deliver avutundi. " +
                        "Dayachesi andubatulo undandi. " +
                        "Dhanyavadamulu.";
        makeCall(phone, msg);
    }

    // ── Order Delivered ────────────────────────────────────────
    public void callOrderDelivered(String phone,
                                   String customerName,
                                   Long orderId) {
        String msg =
                "Namaskaram " + customerName + " garu. " +
                        "Meeru order number " + orderId +
                        " vijayavanthamga deliver ayindi. " +
                        "Meeru maa vad shopping chesinduku dhanyavadamulu. " +
                        "Dayachesi review ivvandi.";
        makeCall(phone, msg);
    }

    // ── Order Cancelled ────────────────────────────────────────
    public void callOrderCancelled(String phone,
                                   String customerName,
                                   Long orderId) {
        String msg =
                "Namaskaram " + customerName + " garu. " +
                        "Meeru order number " + orderId +
                        " cancel cheyabadindi. " +
                        "Meeru amount 5 nundi 7 rojulalo refund avutundi. " +
                        "Dhanyavadamulu.";
        makeCall(phone, msg);
    }

    // ── Helpers ───────────────────────────────────────────────

    // Convert any phone format to 0XXXXXXXXXX
    private String formatPhone(String phone) {
        if (phone == null) return null;
        phone = phone.replaceAll("[^0-9]", "");
        if (phone.startsWith("91") && phone.length() == 12) {
            phone = "0" + phone.substring(2);
        } else if (phone.length() == 10) {
            phone = "0" + phone;
        }
        return phone;
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}