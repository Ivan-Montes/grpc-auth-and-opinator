package dev.ime.application.usecases.query;

import java.util.UUID;

import dev.ime.domain.query.Query;

public record GetByIdProductQuery(UUID id) implements Query {

}
