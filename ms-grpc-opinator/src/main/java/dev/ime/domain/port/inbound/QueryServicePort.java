package dev.ime.domain.port.inbound;

import java.util.UUID;

import org.springframework.data.domain.Pageable;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface QueryServicePort<T> {
	
	Flux<T>findAll(Pageable pageable);
	Mono<T>findById(UUID id);
}
