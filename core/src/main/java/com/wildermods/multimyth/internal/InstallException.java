package com.wildermods.multimyth.internal;

import java.io.IOException;

@SuppressWarnings("serial")
public class InstallException extends IOException {

	public InstallException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public InstallException(String message) {
		super(message);
	}
	
	public InstallException(Throwable cause) {
		super(cause);
	}
	
}
