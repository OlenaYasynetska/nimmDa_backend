package com.nimmda.infrastructure.business;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface SpringDataBusinessProfileJpaRepository extends JpaRepository<BusinessProfileJpaEntity, String> {

    List<BusinessProfileJpaEntity> findByActiveUntilAfterOrderByCompanyNameAsc(Instant now);

    @Query("""
            select p from BusinessProfileJpaEntity p
            where p.activeUntil > :now
              and lower(p.location) = :location
            order by p.companyName asc
            """)
    List<BusinessProfileJpaEntity> findActiveInLocation(@Param("now") Instant now, @Param("location") String location);
}
