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
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import com.wildermods.masshash.exception.IntegrityException;
import com.wildermods.multimyth.MainApplication;
import com.wildermods.multimyth.artifact.maven.MavenArtifactDefinition;
import com.wildermods.multimyth.artifact.maven.MavenDownloader;
import com.wildermods.multimyth.internal.logging.MultimythDecompLogger;
import com.wildermods.thrixlvault.ChrysalisizedVault;
import com.wildermods.thrixlvault.MassDownloadWeaver;
import com.wildermods.thrixlvault.exception.MissingVersionException;
import com.wildermods.thrixlvault.steam.CompletedDownload;
import com.wildermods.thrixlvault.steam.FailedDownload;
import com.wildermods.thrixlvault.steam.IDownloadable;
import com.wildermods.thrixlvault.steam.IGame;
import com.wildermods.thrixlvault.steam.ISteamDownload;
import com.wildermods.thrixlvault.steam.SkippedDownload;
import com.wildermods.thrixlvault.utils.FileUtil;
import com.wildermods.thrixlvault.wildermyth.WildermythManifest;
import com.wildermods.workspace.WilderWorkspacePluginImpl;
import com.wildermods.workspace.decomp.DecompilerBuilder;
import com.wildermods.workspace.decomp.WildermythDecompilerSetup;
import com.wildermods.workspace.dependency.ProjectDependencyType;
import com.wildermods.workspace.dependency.WWProjectDependency;
import com.wildermods.workspace.util.FileHelper;

public class Installer<Game extends IDownloadable & IGame> {

	private final Logger LOGGER;
	private final Marker marker;
	private final Game game;
	private final Path dir;
	private final JVMBinary jvm;
	
	public Installer(Game game, Path dir, boolean installCoremodEnvironment) {
		this(game, dir, JVMInstance.thisVM(), installCoremodEnvironment);
	}
	
	public Installer(Game game, Path dir, JVMBinary jvm, boolean installCoremodEnvironment) {
		this.game = game;
		this.dir = dir;
		this.jvm = jvm;
		LOGGER = LogManager.getLogger(getClass());
		marker = MarkerManager.getMarker(game.toString());
	}
	
	public void install() throws InstallException {
		try {
			if(game instanceof WildermythManifest) {
				installThrixlvault(new Install<>((WildermythManifest)game, dir, JVMInstance.thisVM(), true));
			}
			else if (game instanceof GameOnDisk) {
				
			}
			
		}
		catch(Throwable t) {
			throw new InstallException(t);
		}
	}
	
	@SuppressWarnings("unchecked")
	public void installThrixlvault(Install<WildermythManifest> install) throws IOException, InterruptedException, IntegrityException, ExecutionException {
		WildermythManifest manifest = install.installableGame();
		
		ChrysalisizedVault c;
		try {
			c = ChrysalisizedVault.DEFAULT.chrysalisize(manifest);
		}
		catch(MissingVersionException e) {
			MassDownloadWeaver downloader = new MassDownloadWeaver(MainApplication.INSTANCE.getSteam().username(), Set.of(manifest));
			downloader.setStopOnInterrupt(true);
			Set<ISteamDownload> downloads = downloader.run();
			if(downloads.size() != 1) {
				throw new AssertionError("Expected 1 download, got " + downloads.size());
			}
			ISteamDownload download = downloads.iterator().next();
			if(download instanceof FailedDownload) {
				throw new IOException(((FailedDownload) download).failReason());
			}
			else if(download instanceof SkippedDownload) {
				throw new AssertionError("Download was skipped: " + download.downloadBlockedReason());
			}
			else if(download instanceof CompletedDownload){
				//NO-OP
			}
			c = ChrysalisizedVault.DEFAULT.chrysalisize(manifest);
		}
		
		c.export(dir, true);
		
		if(install.isCoremodded()) {
			installCoremodEnvironment((Install<Game>) install);
		}
		
		applyPatchline();
		addMetadata(install);
	}
	
	public void installFromGameOnDisk(Install<GameOnDisk> install) throws IOException {
		if(!Files.exists(dir)) {
			Files.createDirectories(dir);
		}
		
		Path installDir = install.installableGame().getPath();
		
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
				Path target = Installer.this.dir.resolve(installDir.relativize(dir));
				if(Files.isSymbolicLink(dir) || attrs.isSymbolicLink() || dir.getFileName().endsWith("backup") || dir.getFileName().endsWith("feedback") || dir.getFileName().endsWith("logs") || dir.getFileName().endsWith("out") || dir.getFileName().endsWith("players") || dir.getFileName().endsWith("screenshots")) {
					return FileVisitResult.SKIP_SUBTREE;
				}
				Files.createDirectories(target);
				return FileVisitResult.CONTINUE;
			}
		});
		
		applyPatchline();
	}
	
	public void installCoremodEnvironment(Install<Game> install) throws IOException, InterruptedException {
		
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
		
		MavenDownloader depsDownloader = new MavenDownloader(fabricDeps, (download) -> {
			Path dest = install.installPath().resolve("fabric").resolve(download.name() + "-" + download.version() + ".jar");
			try {
				Files.createDirectories(dest.getParent());
				Files.copy(download.dest(), dest);
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
		MavenDownloader implDownloader = new MavenDownloader(fabricImpls, (download) -> {
			Path dest = install.installPath();
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
		b.setLogger(new MultimythDecompLogger());
		WildermythDecompilerSetup decompSetup = new WildermythDecompilerSetup(b);
		decompSetup.decompile(compiledDir, decompDir);
		FileUtil.deleteDirectory(decompDir.resolve("decomp"));
	}
	
	private void applyPatchline() {
		Path patchFile = dir.resolve("patchline.txt");
		String patchline = "Multimyth " + dir + " - [WilderWorkspace " + WilderWorkspacePluginImpl.VERSION + "]";
		try {
			PathUtils.writeString(patchFile, patchline, Charset.defaultCharset(), StandardOpenOption.TRUNCATE_EXISTING);
		} catch (IOException e) {
			LOGGER.warn(marker, "Could not apply patchline " + patchline);
			LOGGER.catching(Level.WARN, e);
		}
	}
	
	private void addMetadata(Install install) throws IOException {
		Path metadata = dir.resolve("instance.mm");
		try {
			install.serialize(metadata, StandardOpenOption.CREATE_NEW);
		} catch (IOException e) {
			LOGGER.error(marker, "Could not write instance metadata " + metadata);
			LOGGER.catching(Level.ERROR, e);
		}
		
	}
	
}
