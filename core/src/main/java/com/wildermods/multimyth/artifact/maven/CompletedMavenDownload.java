package com.wildermods.multimyth.artifact.maven;

import java.nio.file.Path;

import org.myjtools.mavenfetcher.FetchedArtifact;

public record CompletedMavenDownload(String type, String group, String name, String version, Path dest) implements MavenDownload {
	
	public CompletedMavenDownload(String type, FetchedArtifact artifact) {
		this(type, artifact.groupId(), artifact.artifactId(), artifact.version(), artifact.path());
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
