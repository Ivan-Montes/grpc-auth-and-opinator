package dev.ime.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity.CsrfSpec;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {
	  
    @Bean
    @Profile("!prod")
    SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
    	
        return http
            .authorizeExchange(exchanges -> exchanges
                .pathMatchers("/api/**").permitAll()
                .pathMatchers("/actuator/**").permitAll()
                .anyExchange().authenticated()
            )
            .csrf(CsrfSpec::disable)
            .cors(Customizer.withDefaults())
            .oauth2ResourceServer( resourceServer -> resourceServer.jwt(Customizer.withDefaults()))
            .build();        
    }

    @Bean
    @Profile("prod")
    SecurityWebFilterChain productionSecurityFilterChain(ServerHttpSecurity http) {
    	
        return http
            .authorizeExchange(exchanges -> exchanges
                .pathMatchers("/api/**").permitAll()
                .pathMatchers("/actuator/**").permitAll()
                .anyExchange().authenticated()
            )
            .csrf(CsrfSpec::disable)
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
            .build();        
    }
	 
}
