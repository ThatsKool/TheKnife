/*
 * Utility per il rilevamento approssimativo della posizione tramite IP pubblico.
 */
package dev.theknife.app.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Interroga il servizio ip-api.com per ottenere latitudine e longitudine.
 */
public final class IpLocationDetector {

    private static final String API_URL = "http://ip-api.com/json/?fields=lat,lon,status";
    private static final int TIMEOUT_MS = 5000;

    private IpLocationDetector() {
    }

    public record Coordinates(double latitude, double longitude) {
    }

    /**
     * Rileva le coordinate geografiche associate all'IP pubblico del client.
     *
     * @return coordinate rilevate
     * @throws IOException in caso di errore di rete o risposta non valida
     */
    public static Coordinates detectFromPublicIp() throws IOException {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(API_URL);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(TIMEOUT_MS);
            connection.setReadTimeout(TIMEOUT_MS);

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new IOException("HTTP Error: " + connection.getResponseCode());
            }

            StringBuilder response = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
            }

            String json = response.toString();
            double lat = JsonUtils.parseDouble(json, "lat");
            double lon = JsonUtils.parseDouble(json, "lon");

            if (lat == 0.0 && lon == 0.0) {
                throw new IOException("Coordinate non presenti nella risposta");
            }

            GeoValidator.validateCoordinates(lat, lon);
            return new Coordinates(lat, lon);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}
