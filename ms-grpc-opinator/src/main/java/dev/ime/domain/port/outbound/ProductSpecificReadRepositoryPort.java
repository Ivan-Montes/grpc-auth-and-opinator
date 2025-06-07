package dev.ime.domain.port.outbound;

import java.util.UUID;

import reactor.core.publisher.Mono;

public interface ProductSpecificReadRepositoryPort {

	Mono<Boolean> existsByName(String name);
	Mono<Boolean> isAvailableByIdAndName(UUID id, String name);
}
