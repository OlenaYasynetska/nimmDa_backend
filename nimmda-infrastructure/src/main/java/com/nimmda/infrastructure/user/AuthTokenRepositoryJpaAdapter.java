package com.nimmda.infrastructure.user;

import com.nimmda.domain.shared.UserId;
import com.nimmda.domain.user.AuthToken;
import com.nimmda.domain.user.AuthTokenRepository;
import com.nimmda.domain.user.AuthTokenType;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public class AuthTokenRepositoryJpaAdapter implements AuthTokenRepository {

    private final SpringDataAuthTokenJpaRepository jpaRepository;

    public AuthTokenRepositoryJpaAdapter(SpringDataAuthTokenJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public AuthToken save(AuthToken token) {
        return toDomain(jpaRepository.save(toEntity(token)));
    }

    @Override
    public Optional<AuthToken> findUsableByToken(String token, AuthTokenType type) {
        return jpaRepository
                .findByTokenAndTypeAndConsumedAtIsNull(token, AuthTokenJpaType.valueOf(type.name()))
                .map(this::toDomain);
    }

    @Override
    @Transactional
    public void deleteOpenTokens(String userId, AuthTokenType type) {
        jpaRepository.deleteOpenTokens(userId, AuthTokenJpaType.valueOf(type.name()));
    }

    private AuthTokenJpaEntity toEntity(AuthToken token) {
        AuthTokenJpaEntity entity = new AuthTokenJpaEntity();
        entity.setId(token.id());
        entity.setUserId(token.userId().value());
        entity.setToken(token.token());
        entity.setType(AuthTokenJpaType.valueOf(token.type().name()));
        entity.setExpiresAt(token.expiresAt());
        entity.setConsumedAt(token.consumedAt());
        entity.setCreatedAt(token.createdAt());
        return entity;
    }

    private AuthToken toDomain(AuthTokenJpaEntity entity) {
        return AuthToken.rehydrate(
                entity.getId(),
                new UserId(entity.getUserId()),
                entity.getToken(),
                AuthTokenType.valueOf(entity.getType().name()),
                entity.getExpiresAt(),
                entity.getConsumedAt(),
                entity.getCreatedAt()
        );
    }
}
