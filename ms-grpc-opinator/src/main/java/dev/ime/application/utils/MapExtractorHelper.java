package dev.ime.application.utils;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import dev.ime.application.exception.ValidationException;
import dev.ime.common.constants.GlobalConstants;

@Component
public final class MapExtractorHelper {

	public MapExtractorHelper() {
		super();
	}

	public UUID extractUuid(Map<String, Object> eventData, String key) {
		
	    return Optional.ofNullable(eventData.get(key))
	                   .map(Object::toString)
	                   .map(UUID::fromString)
	                   .orElseThrow(() -> new ValidationException(Map.of(GlobalConstants.OBJ_FIELD, key)));	    
	}

	public String extractString(Map<String, Object> eventData, String key, String patternConstraint) {
		
		String value = Optional.ofNullable(eventData.get(key))
	                   .map(Object::toString)
	                   .orElse("");
	    
	    Pattern compiledPattern = Pattern.compile(patternConstraint);
	    Matcher matcher = compiledPattern.matcher(value);
	    if (!matcher.matches()) {
	        throw new ValidationException(Map.of(GlobalConstants.OBJ_FIELD, key, GlobalConstants.OBJ_VALUE, value));
	    }

	    return value;	    
	}

}
