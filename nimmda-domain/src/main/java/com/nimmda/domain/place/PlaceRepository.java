package com.nimmda.domain.place;

import java.util.List;
import java.util.Optional;

public interface PlaceRepository {

    Optional<Place> findById(PlaceId id);

    List<Place> findByNormalizedName(String nameNormalized);

    List<Place> findByPostalCode(String postalCode);

    List<Place> suggest(String query, int limit);

    Optional<Place> findDuplicate(Place place);

    Place save(Place place);
}
