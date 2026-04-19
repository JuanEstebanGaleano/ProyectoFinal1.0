package ziploc.ui.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import java.util.Map;

public class ApiClient {

    private static final String BASE = "http://localhost:8080/api";
    private static final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5)).build();
    private static final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    // ── GET ──────────────────────────────────────────────────────────────────
    public static String get(String path) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(BASE + path))
                    .GET().build();
            return http.send(req, HttpResponse.BodyHandlers.ofString()).body();
        } catch (Exception e) {
            return "{\"exito\":false,\"mensaje\":\"" + e.getMessage() + "\"}";
        }
    }

    // ── POST con body JSON ───────────────────────────────────────────────────
    public static String post(String path, Map<String, Object> body) {
        try {
            String json = mapper.writeValueAsString(body);
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(BASE + path))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json)).build();
            return http.send(req, HttpResponse.BodyHandlers.ofString()).body();
        } catch (Exception e) {
            return "{\"exito\":false,\"mensaje\":\"" + e.getMessage() + "\"}";
        }
    }

    // ── POST sin body ────────────────────────────────────────────────────────
    public static String post(String path) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(BASE + path))
                    .POST(HttpRequest.BodyPublishers.noBody()).build();
            return http.send(req, HttpResponse.BodyHandlers.ofString()).body();
        } catch (Exception e) {
            return "{\"exito\":false,\"mensaje\":\"" + e.getMessage() + "\"}";
        }
    }

    // ── DELETE ───────────────────────────────────────────────────────────────
    public static String delete(String path) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(BASE + path))
                    .DELETE().build();
            return http.send(req, HttpResponse.BodyHandlers.ofString()).body();
        } catch (Exception e) {
            return "{\"exito\":false,\"mensaje\":\"" + e.getMessage() + "\"}";
        }
    }

    // ── Parsear respuesta ────────────────────────────────────────────────────
    public static com.fasterxml.jackson.databind.JsonNode parse(String json) {
        try { return mapper.readTree(json); }
        catch (Exception e) { return mapper.createObjectNode(); }
    }
}