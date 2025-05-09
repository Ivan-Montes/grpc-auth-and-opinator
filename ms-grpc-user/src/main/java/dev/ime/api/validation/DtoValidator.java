package dev.ime.api.validation;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.GeneratedMessageV3;

import dev.ime.application.exception.ValidationException;
import dev.ime.common.constants.GlobalConstants;
import dev.proto.CreateUserAppRequest;
import dev.proto.DeleteUserAppRequest;
import dev.proto.GetUserAppRequest;
import dev.proto.UpdateUserAppRequest;

@Component
public class DtoValidator {

	private static final Map<Class<? extends GeneratedMessageV3>, Function<String, FieldDescriptor>> FIELD_DESCRIPTOR_MAP;
	
	static {
        FIELD_DESCRIPTOR_MAP = new HashMap<>();
        FIELD_DESCRIPTOR_MAP.put(CreateUserAppRequest.class, CreateUserAppRequest.getDescriptor()::findFieldByName);
        FIELD_DESCRIPTOR_MAP.put(UpdateUserAppRequest.class, UpdateUserAppRequest.getDescriptor()::findFieldByName);
        FIELD_DESCRIPTOR_MAP.put(DeleteUserAppRequest.class, DeleteUserAppRequest.getDescriptor()::findFieldByName);
        FIELD_DESCRIPTOR_MAP.put(GetUserAppRequest.class, GetUserAppRequest.getDescriptor()::findFieldByName);
    }
	
	public void validateCreateUserAppRequest(CreateUserAppRequest request) {
		
		checkString(request, GlobalConstants.USERAPP_EMAIL, GlobalConstants.PATTERN_EMAIL);
		checkString(request, GlobalConstants.USERAPP_NAME, GlobalConstants.PATTERN_NAME_FULL);
		checkString(request, GlobalConstants.USERAPP_LASTNAME, GlobalConstants.PATTERN_NAME_FULL);		
	}

	public void validateUpdateUserAppRequest(UpdateUserAppRequest request) {
		
		checkUUIDField(request, GlobalConstants.USERAPP_ID);
		checkString(request, GlobalConstants.USERAPP_EMAIL, GlobalConstants.PATTERN_EMAIL);
		checkString(request, GlobalConstants.USERAPP_NAME, GlobalConstants.PATTERN_NAME_FULL);
		checkString(request, GlobalConstants.USERAPP_LASTNAME, GlobalConstants.PATTERN_NAME_FULL);		
	}

	public void validateGetUserAppRequest(GetUserAppRequest request) {
		
		checkUUIDField(request, GlobalConstants.USERAPP_ID);
	}	

	public void validateDeleteUserAppRequest(DeleteUserAppRequest request) {
		
		checkUUIDField(request, GlobalConstants.USERAPP_ID);
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

	private <T> void checkString(T request, String key, String patternConstraint) {
		
		FieldDescriptor fieldDescriptor = extractFieldDescriptor(request, key);		

		String value = Optional.ofNullable(((GeneratedMessageV3) request).getField(fieldDescriptor))
				.map(Object::toString)
                .orElse("");
		Pattern compiledPattern = Pattern.compile(patternConstraint);
	    Matcher matcher = compiledPattern.matcher(value);
	    if (!matcher.matches()) {
	        throw new ValidationException(Map.of(GlobalConstants.OBJ_FIELD, key));
	    }	    
	}
	
	private <T> FieldDescriptor extractFieldDescriptor(T request, String key) {
        
		return Optional.ofNullable(FIELD_DESCRIPTOR_MAP.get(request.getClass()))
                .map(function -> function.apply(key))
                .orElseThrow(() -> new IllegalArgumentException(GlobalConstants.MSG_UNSUP_REQ + ":" + request.getClass().getSimpleName()));
    }

}
