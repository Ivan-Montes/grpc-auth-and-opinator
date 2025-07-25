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
import dev.proto.CreateCategoryRequest;
import dev.proto.DeleteCategoryRequest;
import dev.proto.GetCategoryRequest;
import dev.proto.UpdateCategoryRequest;

@Component
public class CategoryRequestValidator {

	private static final Map<Class<? extends GeneratedMessageV3>, Function<String, FieldDescriptor>> FIELD_DESCRIPTOR_MAP;

	static {
        FIELD_DESCRIPTOR_MAP = new HashMap<>();
        FIELD_DESCRIPTOR_MAP.put(CreateCategoryRequest.class, CreateCategoryRequest.getDescriptor()::findFieldByName);
        FIELD_DESCRIPTOR_MAP.put(UpdateCategoryRequest.class, UpdateCategoryRequest.getDescriptor()::findFieldByName);
        FIELD_DESCRIPTOR_MAP.put(DeleteCategoryRequest.class, DeleteCategoryRequest.getDescriptor()::findFieldByName);
        FIELD_DESCRIPTOR_MAP.put(GetCategoryRequest.class, GetCategoryRequest.getDescriptor()::findFieldByName);
    }

	public void validateCreateRequest(CreateCategoryRequest request) {
		
		checkString(request, GlobalConstants.CAT_NAME, GlobalConstants.PATTERN_NAME_FULL);		
	}

	public void validateUpdateRequest(UpdateCategoryRequest request) {
		
		checkUUIDField(request, GlobalConstants.CAT_ID);
		checkString(request, GlobalConstants.CAT_NAME, GlobalConstants.PATTERN_NAME_FULL);		
	}

	public void validateDeleteRequest(DeleteCategoryRequest request) {
		
		checkUUIDField(request, GlobalConstants.CAT_ID);		
	}

	public void validateGetRequest(GetCategoryRequest request) {
		
		checkUUIDField(request, GlobalConstants.CAT_ID);		
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
