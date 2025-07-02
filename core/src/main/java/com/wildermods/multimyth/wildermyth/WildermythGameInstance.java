package com.wildermods.multimyth.wildermyth;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import com.wildermods.multimyth.internal.CompileStrictly;

import net.fabricmc.loader.impl.game.GameDefinition;
import net.fabricmc.loader.impl.game.GameProvider.BuiltinMod;
import net.fabricmc.loader.impl.metadata.BuiltinModMetadata;
import net.fabricmc.loader.impl.metadata.ContactInformationImpl;
import net.fabricmc.loader.impl.util.version.StringVersion;

@CompileStrictly
public class WildermythGameInstance implements GameDefinition {

	private final Path dir;
	
	public WildermythGameInstance(Path dir) {
		this.dir = dir;
	}
	
	@Override
	public String getGameId() {
		return "wildermyth";
	}

	@Override
	public String getGameName() {
		return "Wildermyth";
	}

	@Override
	public String getRawGameVersion() {
		return getGameVersion().getFriendlyString();
	}

	@Override
	public String getNormalizedGameVersion() {
		return getRawGameVersion();
	}

	@Override
	public Collection<BuiltinMod> getBuiltinMods() {
		
		HashMap<String, String> wildermythContactInformation = new HashMap<>();
		wildermythContactInformation.put("homepage", "https://wildermyth.com/");
		wildermythContactInformation.put("issues", "https://discord.gg/wildermyth");
		wildermythContactInformation.put("license", "https://wildermyth.com/terms.php");
		
		BuiltinModMetadata.Builder wildermythMetaData = 
				new BuiltinModMetadata.Builder(getGameId(), getNormalizedGameVersion())
				.setName(getGameName())
				.addAuthor("Worldwalker Games, LLC.", wildermythContactInformation)
				.setContact(new ContactInformationImpl(wildermythContactInformation))
				.setDescription("A procedural storytelling RPG where tactical combat and story decisions will alter your world and reshape your cast of characters.");
		
		ArrayList<BuiltinMod> builtinMods = new ArrayList<>();
		builtinMods.add(new BuiltinMod(List.of(getLaunchDirectory().resolve("wildermyth.jar")), wildermythMetaData.build()));
		
		//todo: other builtin mods
		
		return Collections.unmodifiableList(builtinMods);
	}

	@Override
	public Path getLaunchDirectory() {
		return dir;
	}
	
	private StringVersion getGameVersion() {
		Path versionFile = getLaunchDirectory().resolve("version.txt");
		try {
			if(Files.exists(versionFile)) {
				return new StringVersion(Files.readString(versionFile).split(" ")[0]);
			}
		}
		catch(IOException e) {
			throw new Error("Could not detect wildermyth version");
		}
		throw new Error("Could not detect wildermyth version. Missing versions.txt?");
	}

}
