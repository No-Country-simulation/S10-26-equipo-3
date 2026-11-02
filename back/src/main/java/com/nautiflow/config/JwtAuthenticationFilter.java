package com.nautiflow.config;

import com.nautiflow.service.JwtTokenService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.nautiflow.exception.handler.ApiSecurityErrorHandler;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.cors.CorsUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtTokenService jwtTokenService;
    private final ApiSecurityErrorHandler securityErrorHandler;

    public JwtAuthenticationFilter(
            JwtTokenService jwtTokenService,
            ApiSecurityErrorHandler securityErrorHandler
    ) {
        this.jwtTokenService = jwtTokenService;
        this.securityErrorHandler = securityErrorHandler;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return CorsUtils.isPreFlightRequest(request);
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String authorization = request.getHeader("Authorization");
        if (authorization != null) {
            if (!authorization.regionMatches(true, 0, "Bearer ", 0, 7)) {
                rechazarToken(request, response, new BadCredentialsException("Esquema de autenticación inválido."));
                return;
            }
            try {
                Claims claims = jwtTokenService.validarToken(authorization.substring(7));
                String subject = claims.getSubject();
                String role = claims.get("role", String.class);
                if (subject == null || subject.isBlank()) {
                    rechazarToken(request, response, new BadCredentialsException("El token JWT no es válido."));
                    return;
                }
                var authorities = role == null || role.isBlank()
                        ? List.<SimpleGrantedAuthority>of()
                        : List.of(new SimpleGrantedAuthority("ROLE_" + role));
                var authentication = new UsernamePasswordAuthenticationToken(
                        subject,
                        null,
                        authorities
                );
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (JwtException | IllegalArgumentException exception) {
                rechazarToken(request, response, new BadCredentialsException("El token JWT no es válido.", exception));
                return;
            }
        }
        filterChain.doFilter(request, response);
    }

    private void rechazarToken(
            HttpServletRequest request,
            HttpServletResponse response,
            BadCredentialsException exception
    ) throws IOException, ServletException {
        SecurityContextHolder.clearContext();
        securityErrorHandler.commence(request, response, exception);
    }
}
