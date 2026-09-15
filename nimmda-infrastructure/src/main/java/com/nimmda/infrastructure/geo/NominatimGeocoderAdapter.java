package com.nimmda.infrastructure.geo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimmda.application.port.geo.GeocodedPlace;
import com.nimmda.application.port.geo.Geocoder;
import com.nimmda.domain.geo.GeoCoordinates;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Component
public class NominatimGeocoderAdapter implements Geocoder {

    private static final Logger log = LoggerFactory.getLogger(NominatimGeocoderAdapter.class);
    private static final long MIN_INTERVAL_MS = 1100;
    private static final TypeReference<List<NominatimHit>> HITS = new TypeReference<>() {
    };

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
    private final boolean enabled;
    private final String searchUrl;
    private final String userAgent;
    private final Object rateLock = new Object();
    private long lastRequestAt;

    public NominatimGeocoderAdapter(
            ObjectMapper objectMapper,
            @Value("${app.geocoder.enabled:true}") boolean enabled,
            @Value("${app.geocoder.url:https://nominatim.openstreetmap.org/search}") String searchUrl,
            @Value("${app.geocoder.user-agent:NimmDa/1.0 (https://nimmda.org)}") String userAgent
    ) {
        this.objectMapper = objectMapper;
        this.enabled = enabled;
        this.searchUrl = searchUrl;
        this.userAgent = userAgent == null || userAgent.isBlank()
                ? "NimmDa/1.0 (https://nimmda.org)"
                : userAgent.trim();
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }

    @Override
    public List<GeocodedPlace> search(String query) {
        if (!enabled || query == null || query.isBlank()) {
            return List.of();
        }
        String q = query.trim();
        if (!q.toLowerCase(Locale.ROOT).contains("austria") && !q.toLowerCase(Locale.GERMAN).contains("österreich")) {
            q = q + ", Austria";
        }
        try {
            String url = searchUrl
                    + "?format=jsonv2&addressdetails=1&limit=5&countrycodes=at&accept-language=de"
                    + "&q=" + URLEncoder.encode(q, StandardCharsets.UTF_8);
            HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                    .timeout(Duration.ofSeconds(8))
                    .header("User-Agent", userAgent)
                    .header("Accept", "application/json")
                    .GET()
                    .build();
            throttle();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                log.warn("Nominatim returned HTTP {}", response.statusCode());
                return List.of();
            }
            List<NominatimHit> hits = objectMapper.readValue(response.body(), HITS);
            List<GeocodedPlace> places = new ArrayList<>();
            for (NominatimHit hit : hits) {
                GeocodedPlace place = toPlace(hit);
                if (place != null) {
                    places.add(place);
                }
            }
            return List.copyOf(places);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            log.warn("Nominatim search interrupted");
            return List.of();
        } catch (Exception ex) {
            log.warn("Nominatim search failed for '{}': {}", query, ex.getMessage());
            return List.of();
        }
    }

    private void throttle() throws InterruptedException {
        synchronized (rateLock) {
            long wait = lastRequestAt + MIN_INTERVAL_MS - System.currentTimeMillis();
            if (wait > 0) {
                Thread.sleep(wait);
            }
            lastRequestAt = System.currentTimeMillis();
        }
    }

    private static GeocodedPlace toPlace(NominatimHit hit) {
        if (hit == null || hit.lat == null || hit.lon == null) {
            return null;
        }
        NominatimAddress address = hit.address == null ? new NominatimAddress(null, null, null, null, null, null, null, null) : hit.address;
        String name = firstNonBlank(
                address.city,
                address.town,
                address.village,
                address.municipality,
                address.hamlet,
                hit.name
        );
        if (name == null || name.isBlank()) {
            return null;
        }
        try {
            return new GeocodedPlace(
                    name.trim(),
                    blankToNull(address.state),
                    countryOf(address.countryCode),
                    blankToNull(address.postcode),
                    new GeoCoordinates(Double.parseDouble(hit.lat), Double.parseDouble(hit.lon)),
                    hit.displayName == null || hit.displayName.isBlank() ? name.trim() : hit.displayName.trim()
            );
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private static String countryOf(String countryCode) {
        if (countryCode == null || countryCode.isBlank()) {
            return "AT";
        }
        return countryCode.trim().toUpperCase(Locale.ROOT);
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record NominatimHit(
            String name,
            @JsonProperty("display_name") String displayName,
            String lat,
            String lon,
            NominatimAddress address
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record NominatimAddress(
            String city,
            String town,
            String village,
            String municipality,
            String hamlet,
            String state,
            String postcode,
            @JsonProperty("country_code") String countryCode
    ) {
    }
}
