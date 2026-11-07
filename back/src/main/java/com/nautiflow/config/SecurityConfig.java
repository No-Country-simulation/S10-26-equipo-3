package com.nautiflow.config;

import com.nautiflow.service.JwtTokenService;
import com.nautiflow.service.JwtTokenRevocationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nautiflow.exception.handler.ApiSecurityErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.List;

/**
 * Configura Spring Security: reglas de acceso por ruta, CORS, manejo uniforme de errores y el filtro de autenticación JWT.
 */
@Configuration
public class SecurityConfig {

    /**
     * Define las reglas de autorización, CORS, manejo de errores y el filtro JWT de la API.
     */
    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtTokenService jwtTokenService,
            JwtTokenRevocationService revocationService,
            ObjectMapper objectMapper,
            CorsConfigurationSource corsConfigurationSource
    )
            throws Exception {
        ApiSecurityErrorHandler securityErrorHandler = new ApiSecurityErrorHandler(objectMapper);

        return http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .logout(logout -> logout.disable())
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(securityErrorHandler)
                        .accessDeniedHandler(securityErrorHandler)
                )

                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(
                                HttpMethod.POST,
                                "/lancheros/registrar",
                                "/login"
                        ).permitAll()
                        .requestMatchers(HttpMethod.POST, "/logout").authenticated()
                        .requestMatchers(HttpMethod.GET, "/lanchas/destacadas").permitAll()
                        .requestMatchers(HttpMethod.GET, "/lancheros/*/resenas").permitAll()
                        .requestMatchers(HttpMethod.GET, "/zonas").permitAll()
                        .requestMatchers(HttpMethod.GET, "/resena/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/resena/**").permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")

                        .requestMatchers(
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/openapi.yaml"
                        ).permitAll()

                        .anyRequest().authenticated()
                )
                .addFilterBefore(
                        new JwtAuthenticationFilter(jwtTokenService, revocationService, securityErrorHandler),
                        UsernamePasswordAuthenticationFilter.class
                )

                .build();
    }

    /**
     * Configura CORS para los orígenes y métodos usados por el frontend.
     */
    @Bean
    JwtTokenRevocationService jwtTokenRevocationService() {
        return new JwtTokenRevocationService();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of(
                "Authorization",
                "Content-Type",
                "Accept",
                "X-Requested-With"
        ));
        configuration.setAllowCredentials(false);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * Expone el codificador BCrypt usado para almacenar contraseñas.
     */
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Define un servicio de usuarios que rechaza la autenticación por formulario; la API usa JWT.
     */
    @Bean
    UserDetailsService userDetailsService() {
        return username -> {
            throw new UsernameNotFoundException("Usuario no encontrado.");
        };
    }
}