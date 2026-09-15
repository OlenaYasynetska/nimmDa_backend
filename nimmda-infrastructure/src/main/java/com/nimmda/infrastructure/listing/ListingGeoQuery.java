package com.nimmda.infrastructure.listing;

import com.nimmda.domain.geo.GeoCoordinates;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;

final class ListingGeoQuery {

    private ListingGeoQuery() {
    }

    static Expression<Double> distanceKm(
            CriteriaBuilder cb,
            Root<ListingJpaEntity> root,
            GeoCoordinates origin
    ) {
        Expression<Double> meters = cb.function(
                "ST_Distance_Sphere",
                Double.class,
                cb.function("POINT", Object.class, root.get("longitude"), root.get("latitude")),
                cb.function("POINT", Object.class, cb.literal(origin.longitude()), cb.literal(origin.latitude()))
        );
        return cb.quot(meters, 1000d).as(Double.class);
    }

    static void addBoundingBox(
            java.util.List<jakarta.persistence.criteria.Predicate> predicates,
            CriteriaBuilder cb,
            Path<ListingJpaEntity> root,
            GeoCoordinates origin,
            int radiusKm
    ) {
        double latDelta = radiusKm / 111.0;
        double cosLat = Math.cos(Math.toRadians(origin.latitude()));
        double lngDelta = radiusKm / (111.0 * Math.max(cosLat, 0.01));
        predicates.add(cb.between(root.get("latitude"), origin.latitude() - latDelta, origin.latitude() + latDelta));
        predicates.add(cb.between(root.get("longitude"), origin.longitude() - lngDelta, origin.longitude() + lngDelta));
    }
}
