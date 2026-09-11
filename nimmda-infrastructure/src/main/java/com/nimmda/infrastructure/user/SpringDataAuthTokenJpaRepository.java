package com.nimmda.infrastructure.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SpringDataAuthTokenJpaRepository extends JpaRepository<AuthTokenJpaEntity, String> {

    Optional<AuthTokenJpaEntity> findByTokenAndTypeAndConsumedAtIsNull(String token, AuthTokenJpaType type);

    Optional<AuthTokenJpaEntity> findByTokenAndType(String token, AuthTokenJpaType type);

    @Modifying
    @Query("delete from AuthTokenJpaEntity t where t.userId = :userId and t.type = :type and t.consumedAt is null")
    void deleteOpenTokens(@Param("userId") String userId, @Param("type") AuthTokenJpaType type);
}
