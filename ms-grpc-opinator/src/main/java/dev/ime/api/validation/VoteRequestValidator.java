package dev.ime.api.validation;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

import org.springframework.stereotype.Component;

import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.GeneratedMessageV3;

import dev.ime.application.exception.ValidationException;
import dev.ime.common.constants.GlobalConstants;
import dev.proto.CreateVoteRequest;
import dev.proto.DeleteVoteRequest;
import dev.proto.GetVoteRequest;
import dev.proto.UpdateVoteRequest;

@Component
public class VoteRequestValidator {

	private static final Map<Class<? extends GeneratedMessageV3>, Function<String, FieldDescriptor>> FIELD_DESCRIPTOR_MAP;

	static {
        FIELD_DESCRIPTOR_MAP = new HashMap<>();
        FIELD_DESCRIPTOR_MAP.put(CreateVoteRequest.class, CreateVoteRequest.getDescriptor()::findFieldByName);
        FIELD_DESCRIPTOR_MAP.put(UpdateVoteRequest.class, UpdateVoteRequest.getDescriptor()::findFieldByName);
        FIELD_DESCRIPTOR_MAP.put(DeleteVoteRequest.class, DeleteVoteRequest.getDescriptor()::findFieldByName);
        FIELD_DESCRIPTOR_MAP.put(GetVoteRequest.class, GetVoteRequest.getDescriptor()::findFieldByName);
    }

	public void validateCreateRequest(CreateVoteRequest request) {
		
		checkUUIDField(request, GlobalConstants.REV_ID);
	}

	public void validateUpdateRequest(UpdateVoteRequest request) {
		
		checkUUIDField(request, GlobalConstants.VOT_ID);
	}

	public void validateDeleteRequest(DeleteVoteRequest request) {
		
		checkUUIDField(request, GlobalConstants.VOT_ID);		
	}

	public void validateGetRequest(GetVoteRequest request) {
		
		checkUUIDField(request, GlobalConstants.VOT_ID);		
	}

	private <T> void checkUUIDField(T request, String key) {
		
		FieldDescriptor fieldDescriptor = extractFieldDescriptor(request, key);		

		String value = Optional.ofNullable(((GeneratedMessageV3) request).getField(fieldDescriptor))
				.map(Object::toString)
                .orElse("");		
		
		try {
			UUID.fromString(value);
		} catch (Exception e) {
	        throw new ValidationException(Map.of(GlobalConstants.OBJ_FIELD, key));
		}		
	}

	private <T> FieldDescriptor extractFieldDescriptor(T request, String key) {
        
		return Optional.ofNullable(FIELD_DESCRIPTOR_MAP.get(request.getClass()))
                .map(function -> function.apply(key))
                .orElseThrow(() -> new IllegalArgumentException(GlobalConstants.MSG_UNSUP_REQ + ":" + request.getClass().getSimpleName()));
    }
	
}
