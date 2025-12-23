package com.wildermods.multimyth.internal;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.wildermods.thrixlvault.wildermyth.WildermythManifest;
import com.wildermods.workspace.util.OS;

public class GameOnDisk implements IInstall {
	private final Path path;
	
	public GameOnDisk() {
		this(OS.getSteamDefaultDirectory());
	}
	
	public GameOnDisk(Path p) {
		this.path = p;
	}

	@Override
	public String name() {
		return OS.getOS() + " " + version() + "  (installed)";
	}

	@Override
	public Path artifactPath() {
		throw new UnsupportedOperationException("Refusing to vault potenially contaminated game version " + version() + " at " + path);
	}
	
	@Override
	public String version() {
		Path versionFile = path.resolve("version.txt");
		try {
			if(Files.exists(versionFile)) {
				return Files.readString(versionFile).split(" ")[0];
			}
		}
		catch(IOException e) {
			e.printStackTrace();
		}
		return "unknown";
	}
	
	public Path getPath() {
		return path;
	}
	
	@Override
	public String downloadBlockedReason() {
		return "Cannot download a game version already on disk.";
	}

	@Override
	public long game() {
		return WildermythManifest.GAME_ID;
	}
	
	@Override
	public String gameName() {
		return WildermythManifest.GAME_NAME;
	}
	
}
