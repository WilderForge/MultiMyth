package com.wildermods.multimyth.artifact.maven;

import java.io.File;
import java.nio.file.Path;

public record FailedMavenDownload(String type, String group, String name, String version, Path dest) implements MavenDownload {
	
	public FailedMavenDownload(IMavenArtifact artifact, Path repo) {
		this(artifact.type(), artifact.group(), artifact.name(), artifact.version(), repo.resolve(artifact.group().replace('.', File.separatorChar)).resolve(artifact.name()).resolve(artifact.version()).resolve(artifact.name() + "-" + artifact.version() + ".jar"));
	}
	
	@Override
	public int hashCode() {
		return IMavenArtifact.hashCode(this);
	}
	
	@Override
	public boolean equals(Object o) {
		if(o instanceof IMavenArtifact) {
			return IMavenArtifact.isEqual(this, (IMavenArtifact) o);
		}
		return false;
	}

}
