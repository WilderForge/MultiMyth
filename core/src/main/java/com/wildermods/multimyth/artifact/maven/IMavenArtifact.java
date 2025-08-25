package com.wildermods.multimyth.artifact.maven;

import java.nio.file.Path;
import java.util.Objects;

import com.wildermods.multimyth.artifact.ArtifactDefinition;
import com.wildermods.thrixlvault.wildermyth.WildermythManifest;

public interface IMavenArtifact extends ArtifactDefinition {

	public String type();
	
	public String group();
	
	@Override
	public default Path artifactPath() {
		return Path.of(WildermythManifest.GAME_NAME).resolve("MODS").resolve(name()).resolve(version());
	}
	
	public static boolean isEqual(IMavenArtifact artifact1, IMavenArtifact artifact2) {
		return artifact1.type().equals(artifact2.type()) && artifact1.group().equals(artifact2.group()) && artifact1.name().equals(artifact2.name()) && artifact1.version().equals(artifact2.version());
	};
	
	public static int hashCode(IMavenArtifact artifact) {
		return Objects.hash(artifact.type(), artifact.group(), artifact.name(), artifact.version());
	}
	
}
