package dev.ime.common.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.vote.UnanimousBased;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationProvider;

import dev.proto.AuthGrpcServiceGrpc;
import io.grpc.ServerInterceptor;
import net.devh.boot.grpc.server.security.authentication.BearerAuthenticationReader;
import net.devh.boot.grpc.server.security.authentication.GrpcAuthenticationReader;
import net.devh.boot.grpc.server.security.check.AccessPredicate;
import net.devh.boot.grpc.server.security.check.AccessPredicateVoter;
import net.devh.boot.grpc.server.security.check.GrpcSecurityMetadataSource;
import net.devh.boot.grpc.server.security.check.ManualGrpcSecurityMetadataSource;
import net.devh.boot.grpc.server.security.interceptors.DefaultAuthenticatingServerInterceptor;
import net.devh.boot.grpc.server.serverfactory.GrpcServerConfigurer;

@Configuration
public class GrpcConfig {
	
	/**
	 * 
	 * @param jwtDecoder
	 * @return
	 */
	@Bean
	JwtAuthenticationProvider jwtAuthenticationProvider(JwtDecoder jwtDecoder) {
	    return new JwtAuthenticationProvider(jwtDecoder);
	}
	/**
	 * handle authenticating the JWT tokens
	 * @param properties
	 * @return JwtAuthenticationProvider
	 */
	@Bean
    DaoAuthenticationProvider daoAuthenticationProvider(UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
	    DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
	    provider.setUserDetailsService(userDetailsService);
	    provider.setPasswordEncoder(passwordEncoder);
	    return provider;
    }
	/**
	 * responsible for processing authentication based on JWT
	 * @param authenticationConfiguration
	 * @param jwtAuthenticationProvider
	 * @param daoAuthenticationProvider
	 * @return
	 */
	@Bean
	AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration, JwtAuthenticationProvider jwtAuthenticationProvider, DaoAuthenticationProvider daoAuthenticationProvider) {
		return new ProviderManager(jwtAuthenticationProvider, daoAuthenticationProvider);
	}

	
	
	/**
	 * extracts the token from the gRPC requests
	 * @return GrpcAuthenticationReader
	 */
	@Bean
	GrpcAuthenticationReader grpcAuthenticationReader() {
	    return new BearerAuthenticationReader(BearerTokenAuthenticationToken::new);
	}	
	/**
	 * intercepts the gRPC requests to authenticate the user.
	 * @param authenticationManager
	 * @param authenticationReader
	 * @return ServerInterceptor
	 */
	@Bean
	ServerInterceptor grpcAuthInterceptor(AuthenticationManager authenticationManager, GrpcAuthenticationReader authenticationReader) {
	    return new DefaultAuthenticatingServerInterceptor(authenticationManager, authenticationReader);
	}	
	/**
	 * configures the gRPC server to use the authentication interceptor.
	 * @param grpcAuthInterceptor
	 * @return GrpcServerConfigurer
	 */
	@Bean
	GrpcServerConfigurer grpcServerConfigurer(ServerInterceptor grpcAuthInterceptor) {
	    return serverBuilder -> serverBuilder.intercept(grpcAuthInterceptor);
	}

	

	/**
	 * defines the security rules for gRPC methods
	 * @return GrpcSecurityMetadataSource
	 */
	@Bean
	GrpcSecurityMetadataSource grpcSecurityMetadataSource() {
	    final ManualGrpcSecurityMetadataSource source = new ManualGrpcSecurityMetadataSource();
	    source.set(AuthGrpcServiceGrpc.getDeleteUserMethod(), AccessPredicate.authenticated());
	    source.set(AuthGrpcServiceGrpc.getDisableUserMethod(), AccessPredicate.authenticated());
	    source.set(AuthGrpcServiceGrpc.getEnableUserMethod(), AccessPredicate.authenticated());
	    source.setDefault(AccessPredicate.permitAll());
	    return source;
	}		
	/**
	 * makes decisions on access to methods based on authentication
	 * @return AccessDecisionManager
	 */
	@SuppressWarnings("deprecation")
	@Bean
	AccessDecisionManager accessDecisionManager() {
	    final List<AccessDecisionVoter<?>> voters = new ArrayList<>();
	    voters.add(new AccessPredicateVoter());
	    return new UnanimousBased(voters);
	}
	
}
