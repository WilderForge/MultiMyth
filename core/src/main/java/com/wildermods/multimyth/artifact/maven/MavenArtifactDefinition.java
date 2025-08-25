package com.wildermods.multimyth.artifact.maven;

import org.myjtools.mavenfetcher.FetchedArtifact;

import com.wildermods.workspace.dependency.WWProjectDependency;

public record MavenArtifactDefinition(String type, String group, String name, String version) implements IMavenArtifact {

	public MavenArtifactDefinition(String type, FetchedArtifact artifact) {
		this(type, artifact.groupId(), artifact.artifactId(), artifact.version());
	}
	
	public MavenArtifactDefinition(WWProjectDependency dependency) {
		this(dependency.getType().toString(), dependency.getGroup(), dependency.getName(), dependency.getVersion());
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
	
	public String toString() {
		return group + ":" + name + ":" + version;
	}
	
}
