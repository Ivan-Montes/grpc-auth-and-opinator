package dev.ime.common.config;

import java.util.Map;

import org.hibernate.reactive.mutiny.Mutiny;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.persistence.Persistence;

@Configuration
public class HibernateReactiveConfig {
	
	@Bean
	Mutiny.SessionFactory sessionFactory(HibernateReactiveConfigProperties properties) {
	    return Persistence.createEntityManagerFactory("persistence-unit-opinator", Map.of(
	    		"jakarta.persistence.jdbc.driver",properties.getDrivername(),
	    		"jakarta.persistence.jdbc.url",properties.getUrl(),
	    		"jakarta.persistence.jdbc.user",properties.getUser(),
	    		"jakarta.persistence.jdbc.password",properties.getPassword()))
		        .unwrap(Mutiny.SessionFactory.class);
	}
	
}
