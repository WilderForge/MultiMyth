package com.wildermods.multimyth.internal;

import java.io.IOException;

@CompileStrictly("1.8")
@SuppressWarnings("serial")
public class SerializationException extends IOException {

	public SerializationException() {
		super();
	}
	
	public SerializationException(String message) {
		super(message);
	}
	
	public SerializationException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public SerializationException(Throwable cause) {
		super(cause);
	}
	
}
