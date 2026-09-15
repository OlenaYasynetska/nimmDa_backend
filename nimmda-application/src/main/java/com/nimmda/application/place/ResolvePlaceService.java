package com.nimmda.application.place;

import com.nimmda.application.port.geo.GeocodedPlace;
import com.nimmda.application.port.geo.Geocoder;
import com.nimmda.domain.place.Place;
import com.nimmda.domain.place.PlaceId;
import com.nimmda.domain.place.PlaceNames;
import com.nimmda.domain.place.PlaceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class ResolvePlaceService {

    static final String HOME_REGION = "Oberösterreich";
    private static final String HOME_COUNTRY = "AT";

    private final PlaceRepository placeRepository;
    private final Geocoder geocoder;

    public ResolvePlaceService(PlaceRepository placeRepository, Geocoder geocoder) {
        this.placeRepository = placeRepository;
        this.geocoder = geocoder;
    }

    @Transactional
    public Place requireForListing(String raw) {
        String query = requireQuery(raw);
        List<Place> local = findLocal(query);
        if (local.size() == 1) {
            return local.getFirst();
        }
        if (local.size() > 1) {
            throw new PlaceNotResolvedException(toSuggestions(local));
        }
        return persistBest(query, geocode(query), true);
    }

    @Transactional
    public Optional<Place> findOrigin(String raw) {
        if (raw == null || raw.isBlank()) {
            return Optional.empty();
        }
        String query = raw.trim();
        List<Place> local = findLocal(query);
        if (local.size() == 1) {
            return Optional.of(local.getFirst());
        }
        if (local.size() > 1) {
            return Optional.of(preferHomeRegion(local));
        }
        try {
            return Optional.of(persistBest(query, geocode(query), false));
        } catch (PlaceNotResolvedException ex) {
            return Optional.empty();
        }
    }

    @Transactional(readOnly = true)
    public List<Place> suggest(String raw, int limit) {
        if (raw == null || raw.isBlank()) {
            return List.of();
        }
        int safeLimit = Math.min(Math.max(limit, 1), 20);
        return placeRepository.suggest(raw.trim(), safeLimit);
    }

    private List<Place> findLocal(String query) {
        if (PlaceNames.looksLikePostalCode(query)) {
            List<Place> byPostal = placeRepository.findByPostalCode(query.trim());
            if (!byPostal.isEmpty()) {
                return byPostal;
            }
        }
        return placeRepository.findByNormalizedName(PlaceNames.normalize(query));
    }

    private List<GeocodedPlace> geocode(String query) {
        return geocoder.search(query);
    }

    private Place persistBest(String query, List<GeocodedPlace> remote, boolean failIfAmbiguous) {
        if (remote == null || remote.isEmpty()) {
            throw new PlaceNotResolvedException(List.of());
        }
        String normalizedQuery = PlaceNames.normalize(query);
        List<GeocodedPlace> exact = remote.stream()
                .filter(item -> PlaceNames.normalize(item.name()).equals(normalizedQuery))
                .toList();
        List<GeocodedPlace> pool = exact.isEmpty() ? remote : exact;
        if (pool.size() > 1 && failIfAmbiguous) {
            throw new PlaceNotResolvedException(pool.stream().map(GeocodedPlace::displayName).toList());
        }
        GeocodedPlace chosen = preferHomeRegion(pool);
        Place place = toPlace(chosen);
        List<Place> existingByName = placeRepository.findByNormalizedName(place.nameNormalized());
        if (!existingByName.isEmpty()) {
            return preferHomeRegion(existingByName);
        }
        return placeRepository.findDuplicate(place).orElseGet(() -> placeRepository.save(place));
    }

    private static Place toPlace(GeocodedPlace remote) {
        return new Place(
                PlaceId.newId(),
                remote.name(),
                remote.region() == null ? "" : remote.region(),
                blankToCountry(remote.country()),
                remote.postalCode(),
                remote.coordinates()
        );
    }

    private static String blankToCountry(String country) {
        if (country == null || country.isBlank()) {
            return HOME_COUNTRY;
        }
        return country.trim().toUpperCase(Locale.ROOT);
    }

    private static Place preferHomeRegion(List<Place> places) {
        return places.stream()
                .min(Comparator
                        .comparing((Place place) -> HOME_REGION.equalsIgnoreCase(place.region()) ? 0 : 1)
                        .thenComparing(Place::name, String.CASE_INSENSITIVE_ORDER))
                .orElseThrow();
    }

    private static GeocodedPlace preferHomeRegion(List<GeocodedPlace> places) {
        return places.stream()
                .min(Comparator
                        .comparing((GeocodedPlace place) -> HOME_REGION.equalsIgnoreCase(nullToEmpty(place.region())) ? 0 : 1)
                        .thenComparing(GeocodedPlace::name, String.CASE_INSENSITIVE_ORDER))
                .orElseThrow();
    }

    private static List<String> toSuggestions(List<Place> places) {
        return places.stream().map(Place::displayName).toList();
    }

    private static String requireQuery(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("location must not be blank");
        }
        return raw.trim();
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
