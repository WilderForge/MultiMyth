package com.wildermods.multimyth;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import com.wildermods.masshash.exception.IntegrityException;
import com.wildermods.multimyth.artifact.maven.MavenArtifactDefinition;
import com.wildermods.multimyth.artifact.maven.MavenDownloader;
import com.wildermods.thrixlvault.ChrysalisizedVault;
import com.wildermods.thrixlvault.MassDownloadWeaver;
import com.wildermods.thrixlvault.exception.MissingVersionException;
import com.wildermods.thrixlvault.wildermyth.WildermythManifest;
import com.wildermods.workspace.dependency.WWProjectDependency;

public class TestMain {

	private static final Path dir = Path.of("").resolve("./bin/test");
	static {
		try {
			Files.createDirectories(dir);
		} catch (IOException e) {
			throw new Error(e);
		}
	}
	
	public static void main(String[] args) throws IOException, InterruptedException, IntegrityException, ExecutionException {
/*		
		WildermythManifest manifest = WildermythManifest.getLatest();
		
		ChrysalisizedVault c;
		try {
			c = ChrysalisizedVault.DEFAULT.chrysalisize(manifest);
		}
		catch(MissingVersionException e) {
			MassDownloadWeaver downloader = new MassDownloadWeaver("wilderforge", Set.of(manifest));
			downloader.run();
			c = ChrysalisizedVault.DEFAULT.chrysalisize(manifest);
		}

		c.export(dir, true);
		*/
		LinkedHashSet<MavenArtifactDefinition> fabricDeps = new LinkedHashSet<>();
		LinkedHashSet<MavenArtifactDefinition> fabricImpls = new LinkedHashSet<>();
		
		for(WWProjectDependency dependency : WWProjectDependency.values()) {
			switch(dependency.getType()) {
				case fabricDep:
					fabricDeps.add(new MavenArtifactDefinition(dependency));
					break;
				case fabricImpl:
					fabricImpls.add(new MavenArtifactDefinition(dependency));
					break;
				case retrieveJson:
					//NO-OP
					break;
				default:
					throw new AssertionError("Only fabric dependencies should be here??");
			}
		}
		
		System.out.println(Path.of("").toAbsolutePath());
		
		MavenDownloader depsDownloader = new MavenDownloader(fabricDeps, (download) -> {
			Path dest = Path.of("./bin/test").resolve("fabric").resolve(download.name() + "-" + download.version() + ".jar");
			try {
				Files.createDirectories(dest.getParent());
				Files.copy(download.dest(), dest);
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
		MavenDownloader implDownloader = new MavenDownloader(fabricImpls, (download) -> {
			Path dest = Path.of("./bin/test").resolve(download.name() + "-" + download.version() + ".jar");
			try {
				Files.createDirectories(dest.getParent());
				Files.copy(download.dest(), dest);
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
		
		implDownloader.run().forEach((download) -> {
			System.out.println(download);
		});
		depsDownloader.run().forEach((download) -> {
			System.out.println(download);
		});
	}
	
}
