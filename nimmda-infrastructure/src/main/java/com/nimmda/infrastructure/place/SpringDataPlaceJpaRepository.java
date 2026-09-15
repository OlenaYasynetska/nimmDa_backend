package com.nimmda.infrastructure.place;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SpringDataPlaceJpaRepository extends JpaRepository<PlaceJpaEntity, String> {

    List<PlaceJpaEntity> findByNameNormalizedOrderByRegionAsc(String nameNormalized);

    List<PlaceJpaEntity> findByPostalCodeOrderByNameAsc(String postalCode);

    Optional<PlaceJpaEntity> findByNameNormalizedAndRegionAndCountry(
            String nameNormalized,
            String region,
            String country
    );

    @Query("""
            SELECT p FROM PlaceJpaEntity p
            WHERE p.nameNormalized LIKE CONCAT('%', :query, '%')
               OR LOWER(p.postalCode) LIKE CONCAT('%', :query, '%')
            ORDER BY
              CASE WHEN p.region = 'Oberösterreich' THEN 0 ELSE 1 END,
              p.name
            """)
    List<PlaceJpaEntity> suggest(@Param("query") String query);
}
