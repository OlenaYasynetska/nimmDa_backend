package com.nimmda.web.listing;

import com.nimmda.application.listing.ListingNotFoundException;
import com.nimmda.domain.listing.Listing;
import com.nimmda.domain.listing.ListingId;
import com.nimmda.domain.listing.ListingRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.time.Duration;
import java.util.Base64;
import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/api/listings/{id}/photos")
public class ListingPhotoController {

    private static final CacheControl CACHE = CacheControl.maxAge(Duration.ofDays(7)).cachePublic();

    private final ListingRepository listingRepository;
    private final String frontendBaseUrl;

    public ListingPhotoController(
            ListingRepository listingRepository,
            @Value("${app.frontend.base-url:https://www.nimmda.org}") String frontendBaseUrl
    ) {
        this.listingRepository = listingRepository;
        this.frontendBaseUrl = stripTrailingSlash(frontendBaseUrl);
    }

    @GetMapping("/{index}")
    public ResponseEntity<?> photo(@PathVariable String id, @PathVariable int index) {
        Listing listing = listingRepository.findById(new ListingId(id))
                .orElseThrow(() -> new ListingNotFoundException(id));
        List<String> photos = listing.images();
        if (index < 0 || index >= photos.size()) {
            throw new ListingNotFoundException(id);
        }
        String value = photos.get(index);
        if (value.startsWith("data:")) {
            Decoded decoded = decode(value);
            return ResponseEntity.ok()
                    .cacheControl(CACHE)
                    .contentType(MediaType.parseMediaType(decoded.mediaType()))
                    .body(decoded.bytes());
        }
        return ResponseEntity.status(302)
                .cacheControl(CACHE)
                .location(URI.create(absolute(value)))
                .build();
    }

    private String absolute(String value) {
        if (value.startsWith("http://") || value.startsWith("https://")) {
            return value;
        }
        return frontendBaseUrl + (value.startsWith("/") ? value : "/" + value);
    }

    private static Decoded decode(String dataUrl) {
        int comma = dataUrl.indexOf(',');
        if (comma < 0) {
            throw new ListingNotFoundException("photo");
        }
        String meta = dataUrl.substring(5, comma);
        String mediaType = "image/jpeg";
        int semicolon = meta.indexOf(';');
        String declared = (semicolon < 0 ? meta : meta.substring(0, semicolon)).trim().toLowerCase(Locale.ROOT);
        if (declared.equals("image/jpeg") || declared.equals("image/png") || declared.equals("image/webp") || declared.equals("image/gif")) {
            mediaType = declared;
        }
        try {
            return new Decoded(mediaType, Base64.getDecoder().decode(dataUrl.substring(comma + 1)));
        } catch (IllegalArgumentException exception) {
            throw new ListingNotFoundException("photo");
        }
    }

    private static String stripTrailingSlash(String value) {
        String trimmed = value == null ? "" : value.trim();
        while (trimmed.endsWith("/")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        return trimmed.isBlank() ? "https://www.nimmda.org" : trimmed;
    }

    private record Decoded(String mediaType, byte[] bytes) {
    }
}
