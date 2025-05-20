package dev.ime.application.exception;

import java.util.Map;
import java.util.UUID;

import dev.ime.common.constants.GlobalConstants;

public class CreateEventException extends BasicException {

	private static final long serialVersionUID = -4953966239482893610L;

	public CreateEventException(Map<String, String> errors) {
		super(
				UUID.randomUUID(), 
				GlobalConstants.EX_CREATEEVENT, 
				GlobalConstants.EX_CREATEEVENT_DESC, 
				errors);
	}

}
