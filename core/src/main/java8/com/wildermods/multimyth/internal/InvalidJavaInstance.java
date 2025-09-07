package com.wildermods.multimyth.internal;

import java.nio.file.Path;

@CompileStrictly("1.8")
public class InvalidJavaInstance extends JavaInstance {

	InvalidJavaInstance(String version, Path jvmLocation, Path launchDir) {
		super(version, jvmLocation, launchDir);
	}
	
	@Override
	public boolean isValid() {
		return false;
	}

}
