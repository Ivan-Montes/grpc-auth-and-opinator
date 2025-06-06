package dev.ime.common.mapper;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import dev.ime.domain.command.Command;
import dev.ime.domain.model.Event;
import dev.ime.infrastructure.entity.EventMongoEntity;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class EventMapper {

	private final ObjectMapper objectMapper;
	
	public Event createEvent(String eventCategory, String eventType, Command command) {		
		
		return new Event(
				eventCategory,
				eventType,
				createEventData(command)
				);		
	}
	
	private <T extends Command> Map<String, Object> createEventData(T command) {
	    return objectMapper.convertValue(command, new TypeReference<Map<String, Object>>() {});
	}

	public EventMongoEntity fromEventDomainToEventMongo(Event event) {
	
		return EventMongoEntity.builder()
				.eventId(event.getEventId())
				.eventCategory(event.getEventCategory())
				.eventType(event.getEventType())
				.eventTimestamp(event.getEventTimestamp())
				.eventData(event.getEventData())
				.build();		
	}
	
	public Event fromEventMongoToEventDomain(EventMongoEntity eventMongoEntity) {
		
		return new Event(
				eventMongoEntity.getEventId(),
				eventMongoEntity.getEventCategory(),
				eventMongoEntity.getEventType(),
				eventMongoEntity.getEventTimestamp(),
				eventMongoEntity.getEventData()				
				);				
	}

}
