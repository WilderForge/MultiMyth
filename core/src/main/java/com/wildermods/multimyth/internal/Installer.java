package com.wildermods.multimyth.internal;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.apache.commons.io.file.PathUtils;

import com.wildermods.masshash.exception.IntegrityException;
import com.wildermods.multimyth.MainApplication;
import com.wildermods.multimyth.artifact.maven.MavenArtifactDefinition;
import com.wildermods.multimyth.artifact.maven.MavenDownloader;
import com.wildermods.thrixlvault.ChrysalisizedVault;
import com.wildermods.thrixlvault.MassDownloadWeaver;
import com.wildermods.thrixlvault.exception.MissingVersionException;
import com.wildermods.thrixlvault.steam.IDownloadable;
import com.wildermods.thrixlvault.steam.IGame;
import com.wildermods.thrixlvault.utils.FileUtil;
import com.wildermods.thrixlvault.wildermyth.WildermythManifest;
import com.wildermods.workspace.WilderWorkspacePluginImpl;
import com.wildermods.workspace.decomp.DecompilerBuilder;
import com.wildermods.workspace.decomp.WildermythDecompilerSetup;
import com.wildermods.workspace.dependency.ProjectDependencyType;
import com.wildermods.workspace.dependency.WWProjectDependency;
import com.wildermods.workspace.util.FileHelper;

public class Installer<Game extends IDownloadable & IGame> {

	private final Game game;
	private final Path dir;
	
	public Installer(MainApplication application, Game game, Path dir, boolean installCoremodEnvironment) {
		this.game = game;
		this.dir = dir;
	}
	
	public void install() throws IOException, InterruptedException, IntegrityException, ExecutionException {
		if(game instanceof WildermythManifest) {
			installThrixlvault(new Install<>((WildermythManifest)game, dir, JVMInstance.thisVM(), true));
		}
		else if (game instanceof GameOnDisk) {
			
		}
	}
	
	@SuppressWarnings("unchecked")
	public void installThrixlvault(Install<WildermythManifest> install) throws IOException, InterruptedException, IntegrityException, ExecutionException {
		WildermythManifest manifest = install.game();
		
		ChrysalisizedVault c;
		try {
			c = ChrysalisizedVault.DEFAULT.chrysalisize(manifest);
		}
		catch(MissingVersionException e) {
			MassDownloadWeaver downloader = new MassDownloadWeaver(MainApplication.INSTANCE.getSteam().username(), Set.of(manifest));
			downloader.run();
			c = ChrysalisizedVault.DEFAULT.chrysalisize(manifest);
		}
		
		c.export(dir, true);
		
		if(install.isCoremodded()) {
			installCoremodEnvironment((Install<Game>) install);
		}
		
	}
	
	public void installFromGameOnDisk(Install<GameOnDisk> install) throws IOException {
		if(!Files.exists(dir)) {
			Files.createDirectories(dir);
		}
		
		Path installDir = install.game().getPath();
		
		Files.walkFileTree(installDir, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				Path target;
				if(FileHelper.shouldBeRemapped(file)) {
					target = dir.resolve("unmapped").resolve(installDir.relativize(file));
				}
				else {
					target = dir.resolve(installDir.relativize(file));
				}
				Files.createDirectories(target.getParent());
				Files.copy(file, target, StandardCopyOption.REPLACE_EXISTING);
				return FileVisitResult.CONTINUE;
			}
			
			@Override
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
				Path target = dir.resolve(installDir.relativize(dir));
				if(Files.isSymbolicLink(dir) || attrs.isSymbolicLink() || dir.getFileName().endsWith("backup") || dir.getFileName().endsWith("feedback") || dir.getFileName().endsWith("logs") || dir.getFileName().endsWith("out") || dir.getFileName().endsWith("players") || dir.getFileName().endsWith("screenshots")) {
					return FileVisitResult.SKIP_SUBTREE;
				}
				Files.createDirectories(target);
				return FileVisitResult.CONTINUE;
			}
		});
		
		Path patchFile = dir.resolve("patchline.txt");
		PathUtils.writeString(patchFile, dir + " - [WilderWorkspace " + WilderWorkspacePluginImpl.VERSION + "]", Charset.defaultCharset(), StandardOpenOption.TRUNCATE_EXISTING);
	}
	
	public void installCoremodEnvironment(Install<Game> install) throws IOException {
		
		decompileAndRemap(install);
		
		LinkedHashSet<MavenArtifactDefinition> fabricDeps = new LinkedHashSet<>();
		LinkedHashSet<MavenArtifactDefinition> fabricImpls = new LinkedHashSet<>();
		
		dependencies:
		for(WWProjectDependency dependency : WWProjectDependency.values()) {
			for(ProjectDependencyType type : dependency.getTypes()) {
					switch(type) {
					case fabricDep:
						fabricDeps.add(new MavenArtifactDefinition(dependency, type));
						continue dependencies;
					case fabricImpl:
						fabricImpls.add(new MavenArtifactDefinition(dependency, type));
						continue dependencies;
					case retrieveJson:
						//NO-OP
						break;
					default:
						throw new AssertionError("Only fabric dependencies should be here??");
				}
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
			Path dest = Path.of("./bin/test");
			String fullName = download.name() + "-" + download.version() + ".jar";
			if(download.name().equals("fabric-loader") || download.name().equals("provider")) {
				dest = dest.resolve(fullName);
			}
			else {
				dest = dest.resolve("fabric").resolve(fullName);
			}
			
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
	
	public void decompileAndRemap(Install<Game> install) throws IOException {
		Path compiledDir = install.installPath();
		Path decompDir = install.installPath();
		
		DecompilerBuilder b = new DecompilerBuilder();
		WildermythDecompilerSetup decompSetup = new WildermythDecompilerSetup(b);
		decompSetup.decompile(compiledDir, decompDir);
		FileUtil.deleteDirectory(decompDir.resolve("decomp"));
	}
	
}
