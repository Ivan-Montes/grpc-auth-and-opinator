package dev.ime.application.utils;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Component;

import dev.ime.common.constants.GlobalConstants;
import reactor.core.publisher.Mono;

@Component
public class JwtUtil {

    private final JwtDecoder jwtDecoder;

	public JwtUtil(JwtDecoder jwtDecoder) {
		super();
		this.jwtDecoder = jwtDecoder;
	}
    
    public Mono<String> getJwtTokenFromContext() {
        return Mono.deferContextual(ctx -> {
            String jwtToken = ctx.getOrDefault(GlobalConstants.JWT_TOKEN, null);
            if (jwtToken == null) {
                return Mono.error(new SecurityException(GlobalConstants.MSG_EMPTYTOKEN));
            }
    		return getEmailFromJwtReactive(jwtToken);
        });
    }

    private Mono<String> getEmailFromJwtReactive(String token) {
        return Mono.fromCallable(() -> {
            Jwt jwt = jwtDecoder.decode(token);
            String email = jwt.getClaimAsString(GlobalConstants.JWT_USER);
            if (email == null) {
                throw new SecurityException(GlobalConstants.MSG_EMPTYEMAIL);
            }
            return email;
        });
    }
    
}
