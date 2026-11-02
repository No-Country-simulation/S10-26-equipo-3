package com.nautiflow.config;

import com.nautiflow.service.JwtTokenService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nautiflow.exception.handler.ApiSecurityErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtTokenService jwtTokenService,
            ObjectMapper objectMapper
    )
            throws Exception {
        ApiSecurityErrorHandler securityErrorHandler = new ApiSecurityErrorHandler(objectMapper);

        return http
                .csrf(csrf -> csrf.disable())
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(securityErrorHandler)
                        .accessDeniedHandler(securityErrorHandler)
                )

                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(
                                HttpMethod.POST,
                                "/lancheros/registrar",
                                "/login"
                        ).permitAll()
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
                        new JwtAuthenticationFilter(jwtTokenService, securityErrorHandler),
                        UsernamePasswordAuthenticationFilter.class
                )

                .build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    UserDetailsService userDetailsService() {
        return username -> {
            throw new UsernameNotFoundException("Usuario no encontrado.");
        };
    }
}