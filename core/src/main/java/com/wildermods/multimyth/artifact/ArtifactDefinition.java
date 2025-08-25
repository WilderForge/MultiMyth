package com.wildermods.multimyth.artifact;

import java.nio.file.Path;

import com.wildermods.thrixlvault.steam.IDownloadable;
import com.wildermods.thrixlvault.wildermyth.WildermythManifest;

public interface ArtifactDefinition extends IDownloadable {

	@Override
	public default Path artifactPath() {
		return Path.of(WildermythManifest.GAME_NAME).resolve("MODS").resolve(name()).resolve(version());
	}
	
}
