package com.nimmda.infrastructure.listing;

import com.nimmda.domain.geo.GeoCoordinates;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import java.util.List;

final class ListingGeoQuery {

    private static final double EARTH_KM = 6371.0;

    private ListingGeoQuery() {
    }

    static Expression<Double> distanceKm(
            CriteriaBuilder cb,
            Root<ListingJpaEntity> root,
            GeoCoordinates origin
    ) {
        Expression<Double> latRad = cb.function("radians", Double.class, root.get("latitude"));
        Expression<Double> lngRad = cb.function("radians", Double.class, root.get("longitude"));
        double originLat = Math.toRadians(origin.latitude());
        double originLng = Math.toRadians(origin.longitude());
        Expression<Double> sinProduct = cb.prod(
                cb.literal(Math.sin(originLat)),
                cb.function("sin", Double.class, latRad)
        );
        Expression<Double> cosProduct = cb.prod(
                cb.prod(
                        cb.literal(Math.cos(originLat)),
                        cb.function("cos", Double.class, latRad)
                ),
                cb.function("cos", Double.class, cb.diff(lngRad, cb.literal(originLng)))
        );
        Expression<Double> cosine = cb.function(
                "least",
                Double.class,
                cb.literal(1.0d),
                cb.function("greatest", Double.class, cb.literal(-1.0d), cb.sum(sinProduct, cosProduct))
        );
        return cb.prod(cb.literal(EARTH_KM), cb.function("acos", Double.class, cosine));
    }

    static void addBoundingBox(
            List<Predicate> predicates,
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
