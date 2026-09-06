package com.nimmda.domain.user;

import java.util.Optional;

public interface AuthTokenRepository {

    AuthToken save(AuthToken token);

    Optional<AuthToken> findUsableByToken(String token, AuthTokenType type);

    void deleteOpenTokens(String userId, AuthTokenType type);
}
