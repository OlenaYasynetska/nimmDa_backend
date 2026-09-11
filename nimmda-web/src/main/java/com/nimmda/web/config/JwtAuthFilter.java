package com.nimmda.web.config;

import com.nimmda.application.port.security.AccessTokenIssuer;
import com.nimmda.application.port.security.AccessTokenPrincipal;
import com.nimmda.domain.shared.UserId;
import com.nimmda.domain.user.CodedAdmin;
import com.nimmda.domain.user.User;
import com.nimmda.domain.user.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final AccessTokenIssuer accessTokenIssuer;
    private final UserRepository userRepository;

    public JwtAuthFilter(AccessTokenIssuer accessTokenIssuer, UserRepository userRepository) {
        this.accessTokenIssuer = accessTokenIssuer;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            accessTokenIssuer.parse(header.substring(7)).ifPresent(principal -> {
                if (!isActiveAccount(principal)) {
                    return;
                }
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                principal.userId(),
                                null,
                                List.of(new SimpleGrantedAuthority("ROLE_" + principal.role()))
                        );
                SecurityContextHolder.getContext().setAuthentication(authentication);
            });
        }
        filterChain.doFilter(request, response);
    }

    private boolean isActiveAccount(AccessTokenPrincipal principal) {
        if (CodedAdmin.isId(principal.userId())) {
            return true;
        }
        return userRepository
                .findById(new UserId(principal.userId()))
                .filter(User::emailVerified)
                .isPresent();
    }
}
