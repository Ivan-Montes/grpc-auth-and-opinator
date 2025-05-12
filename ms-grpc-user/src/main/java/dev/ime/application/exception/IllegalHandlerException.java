package dev.ime.application.exception;

import java.util.Map;
import java.util.UUID;

import dev.ime.common.constants.GlobalConstants;

public class IllegalHandlerException extends BasicException {

	private static final long serialVersionUID = 9144146339234980345L;

	public IllegalHandlerException(Map<String, String> errors) {
		super(
				UUID.randomUUID(), 
				GlobalConstants.EX_ILLEGALHANDLER, 
				GlobalConstants.EX_ILLEGALHANDLER_DESC, 
				errors);
		}
}
