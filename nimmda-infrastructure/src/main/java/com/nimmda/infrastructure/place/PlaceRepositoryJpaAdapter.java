package com.nimmda.infrastructure.place;

import com.nimmda.domain.geo.GeoCoordinates;
import com.nimmda.domain.place.Place;
import com.nimmda.domain.place.PlaceId;
import com.nimmda.domain.place.PlaceNames;
import com.nimmda.domain.place.PlaceRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class PlaceRepositoryJpaAdapter implements PlaceRepository {

    private final SpringDataPlaceJpaRepository jpaRepository;

    public PlaceRepositoryJpaAdapter(SpringDataPlaceJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<Place> findById(PlaceId id) {
        return jpaRepository.findById(id.value()).map(this::toDomain);
    }

    @Override
    public List<Place> findByNormalizedName(String nameNormalized) {
        return jpaRepository.findByNameNormalizedOrderByRegionAsc(nameNormalized).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<Place> findByPostalCode(String postalCode) {
        return jpaRepository.findByPostalCodeOrderByNameAsc(postalCode).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<Place> suggest(String query, int limit) {
        String normalized = PlaceNames.normalize(query).replace("%", "").replace("_", "");
        if (normalized.isEmpty()) {
            return List.of();
        }
        return jpaRepository.suggest(normalized).stream()
                .limit(Math.max(limit, 1))
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Optional<Place> findDuplicate(Place place) {
        return jpaRepository.findByNameNormalizedAndRegionAndCountry(
                        place.nameNormalized(),
                        place.region(),
                        place.country()
                )
                .map(this::toDomain);
    }

    @Override
    public Place save(Place place) {
        return toDomain(jpaRepository.save(toEntity(place)));
    }

    private PlaceJpaEntity toEntity(Place place) {
        PlaceJpaEntity entity = new PlaceJpaEntity();
        entity.setId(place.id().value());
        entity.setName(place.name());
        entity.setNameNormalized(place.nameNormalized());
        entity.setRegion(place.region());
        entity.setCountry(place.country());
        entity.setPostalCode(place.postalCode());
        entity.setLatitude(place.coordinates().latitude());
        entity.setLongitude(place.coordinates().longitude());
        return entity;
    }

    private Place toDomain(PlaceJpaEntity entity) {
        return new Place(
                new PlaceId(entity.getId()),
                entity.getName(),
                entity.getRegion(),
                entity.getCountry(),
                entity.getPostalCode(),
                new GeoCoordinates(entity.getLatitude(), entity.getLongitude())
        );
    }
}
