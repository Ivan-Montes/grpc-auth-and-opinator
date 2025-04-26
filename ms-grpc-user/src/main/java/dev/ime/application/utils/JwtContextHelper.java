package dev.ime.application.utils;

import dev.ime.common.constants.JwtConstants;

public class JwtContextHelper {
	
	private JwtContextHelper() {
		super();
	}

	public static String getJwtToken() {
        return JwtConstants.AUTHORIZATION_CONTEXT_KEY.get();        
    }
	
}
