package dev.ime.common.constants;

import io.grpc.Context;
import io.grpc.Metadata;

public class JwtConstants {

	private JwtConstants() {
		super();
	}

	public static final Metadata.Key<String> AUTHORIZATION_METADATA_KEY = Metadata.Key.of("Authorization",
			Metadata.ASCII_STRING_MARSHALLER);
	public static final Context.Key<String> AUTHORIZATION_CONTEXT_KEY = Context.key("Authorization_context");

}
