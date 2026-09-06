package com.nimmda.application.port.security;

import com.nimmda.domain.user.User;

import java.util.Optional;

public interface AccessTokenIssuer {

    IssuedAccessToken issue(User user);

    Optional<AccessTokenPrincipal> parse(String token);
}
