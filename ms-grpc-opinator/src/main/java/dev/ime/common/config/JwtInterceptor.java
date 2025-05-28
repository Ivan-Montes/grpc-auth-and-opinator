package dev.ime.common.config;

import dev.ime.common.constants.JwtConstants;
import io.grpc.Context;
import io.grpc.Contexts;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCall.Listener;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;


public class JwtInterceptor implements ServerInterceptor {

	@Override
	public <ReqT, RespT> Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call, Metadata headers,
			ServerCallHandler<ReqT, RespT> next) {		

        Context current = Context.current();
        
	    String jwtToken = headers.get(JwtConstants.AUTHORIZATION_METADATA_KEY);
        if (jwtToken != null && jwtToken.startsWith("Bearer ")) {
            jwtToken = jwtToken.substring(7); 
            current = current.withValue(JwtConstants.AUTHORIZATION_CONTEXT_KEY, jwtToken);
        }
        
        return Contexts.interceptCall(current, call, headers, next);
        
	}

}
