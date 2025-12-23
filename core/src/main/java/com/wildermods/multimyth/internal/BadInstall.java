package com.wildermods.multimyth.internal;

import java.nio.file.Path;

public record BadInstall(Path path, Throwable exception) implements IInstall {
	
	public BadInstall(Path path) {
		this(path, null);
	}
	
	@Override
	public String name() {
		return path.getFileName().toString();
	}

	@Override
	public Path artifactPath() {
		throw new UnsupportedOperationException();
	}

	@Override
	public long game() {
		return -1;
	}

}
