package com.tpi.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtGrantedAuthoritiesConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityFilterChain(ServerHttpSecurity http) {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeExchange(exchanges -> exchanges
                .pathMatchers("/actuator/**").permitAll()
                .anyExchange().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
            );

        return http.build();
    }

    /**
     * Convierte los roles de Keycloak a autoridades Spring Security en contexto reactivo.
     */
    @Bean
    public ReactiveJwtAuthenticationConverter jwtAuthenticationConverter() {
        // Custom converter: Keycloak places realm roles under claim `realm_access.roles` (nested map).
        // JwtGrantedAuthoritiesConverter does not support nested claim paths, so we provide a custom converter
        // that extracts the list and maps them to SimpleGrantedAuthority with prefix ROLE_.
        ReactiveJwtAuthenticationConverter jwtAuthenticationConverter = new ReactiveJwtAuthenticationConverter();

        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwt -> {
            java.util.Collection<org.springframework.security.core.GrantedAuthority> authorities = new java.util.ArrayList<>();

            Object realmAccess = jwt.getClaims().get("realm_access");
            if (realmAccess instanceof java.util.Map) {
                Object rolesObj = ((java.util.Map<?, ?>) realmAccess).get("roles");
                if (rolesObj instanceof java.util.Collection) {
                    for (Object r : (java.util.Collection<?>) rolesObj) {
                        if (r != null) {
                            String role = r.toString();
                            authorities.add(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + role));
                        }
                    }
                }
            }

            // Also include scope/as authorities if present (optional)
            Object scopeObj = jwt.getClaims().get("scope");
            if (scopeObj instanceof String) {
                String[] scopes = scopeObj.toString().split(" ");
                for (String s : scopes) {
                    authorities.add(new org.springframework.security.core.authority.SimpleGrantedAuthority(s));
                }
            }

            return reactor.core.publisher.Flux.fromIterable(authorities);
        });

        return jwtAuthenticationConverter;
    }
}
