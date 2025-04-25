package dev.ime.common.mapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import dev.ime.application.dto.UserAppDto;
import dev.ime.common.constants.GlobalConstants;
import dev.ime.domain.command.Command;
import dev.ime.domain.model.Event;
import dev.ime.domain.model.UserApp;
import dev.ime.infrastructure.entity.EventMongoEntity;
import dev.ime.infrastructure.entity.UserAppJpaEntity;
import dev.proto.CreateUserAppRequest;
import dev.proto.UpdateUserAppRequest;
import dev.proto.UserAppProto;

@Component
public class UserAppMapper {

	private final ObjectMapper objectMapper;
	
	public UserAppMapper(ObjectMapper objectMapper) {
		super();
		this.objectMapper = objectMapper;
	}

	public UserAppJpaEntity fromDomainToJpa(UserApp domain) {

		return new UserAppJpaEntity(
				domain.getUserAppId(),
				domain.getEmail(),
				domain.getName(),
				domain.getLastname()
				);		
	}

	public UserApp fromDtoToDomain(UserAppDto dto) {

		return new UserApp(
				dto.userAppId(),
				dto.email(),
				dto.name(),
				dto.lastname()
				);
	}

	public UserApp fromJpaToDomain(UserAppJpaEntity entity) {

		return new UserApp(
				entity.getUserAppId(),
				entity.getEmail(),
				entity.getName(),
				entity.getLastname()
				); 		
	}

	public List<UserApp> fromListJpaToListDomain(List<UserAppJpaEntity> listJpa) {

		if ( listJpa == null ) {
			return new ArrayList<>();
		}

		return listJpa.stream()
				.map(this::fromJpaToDomain)
				.toList();	
	}

	public UserAppDto fromDomainToDto(UserApp domain) {

		return new UserAppDto(
				domain.getUserAppId(),
				domain.getEmail(),
				domain.getName(),
				domain.getLastname()
				);		
	}	

	public List<UserAppDto> fromListDomainToListDto(List<UserApp> listDomain) {

		if ( listDomain == null ) {
			return new ArrayList<>();
		}

		return listDomain.stream()
				.map(this::fromDomainToDto)
				.toList();	
	}	

	public UserAppDto fromCreateUserAppRequestToUserAppDto(CreateUserAppRequest request){

		return new UserAppDto(
				null,
				request.getEmail(),
				request.getName(),
				request.getLastname()
				);
	}

	public UserAppDto fromUpdateUserAppRequestToUserAppDto(UpdateUserAppRequest request) {

		return new UserAppDto(
				UUID.fromString(request.getUserAppId()),
				request.getEmail(),
				request.getName(),
				request.getLastname()
				);
	}

	public UserAppProto fromUserAppDtoToUserAppProto(UserAppDto dto) {
		
		return UserAppProto.newBuilder()
				.setUserAppId(dto.userAppId().toString())
				.setEmail(dto.email())
				.setName(dto.name())
				.setLastname(dto.lastname())
				.build();		
	}
	
	public List<UserAppProto> fromListUserAppDtoToListUserAppProto(List<UserAppDto> listDto){
		
		if ( listDto == null ) {
			return new ArrayList<>();
		}
		
		return listDto.stream()
				.map(this::fromUserAppDtoToUserAppProto)
				.toList();	
		
	}	

	public Event createEvent(Command command, String eventType) {		
		
		return new Event(
				GlobalConstants.USERAPP_CAT,
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

	public UserAppProto fromEventToUserAppProto(Event event) {
		
		Map<String, Object> eventData = event.getEventData();
		
		String userAppId = extractString(eventData, GlobalConstants.USERAPP_ID);
        String email = extractString(eventData, GlobalConstants.USERAPP_EMAIL);
        String name = extractString(eventData, GlobalConstants.USERAPP_NAME);
        String lastname = extractString(eventData, GlobalConstants.USERAPP_LASTNAME);

		return UserAppProto.newBuilder()
				.setUserAppId(userAppId)
				.setEmail(email)
				.setName(name)
				.setLastname(lastname)
				.build();		
	}
	
	private String extractString(Map<String, Object> eventData, String key) {
		
		return Optional.ofNullable(eventData.get(key))
	                   .map(Object::toString)
	                   .orElse("");
	}
	
}
