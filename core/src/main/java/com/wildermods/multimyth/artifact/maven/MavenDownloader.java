package com.wildermods.multimyth.artifact.maven;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Consumer;

import org.myjtools.mavenfetcher.MavenFetchRequest;
import org.myjtools.mavenfetcher.MavenFetchResult;
import org.myjtools.mavenfetcher.MavenFetcher;

import com.google.common.collect.ImmutableSet;
import com.wildermods.thrixlvault.Downloader;
import com.wildermods.thrixlvault.steam.IDownload;

public class MavenDownloader extends Downloader<IMavenArtifact, MavenDownload>{

	private final MavenFetcher fetcher = new MavenFetcher();
	{
		fetcher.localRepositoryPath(Path.of(System.getProperty("user.home")).resolve(".m2").resolve("repository"));
		fetcher.addRemoteRepository("MavenLocal", "file://" + fetcher.getLocalRepository());
		fetcher.addRemoteRepository("fabric", "https://maven.fabricmc.net");
		fetcher.addRemoteRepository("wilderforge", "https://maven.wildermods.com");
	}
	
	private final Consumer<IDownload> consumer;
	
	@SuppressWarnings("unchecked")
	public MavenDownloader(Collection<? extends IMavenArtifact> downloadables, Consumer<IDownload> onDownload) {
		super((Collection<IMavenArtifact>) downloadables);
		this.consumer = onDownload;
	}

	@Override
	@SuppressWarnings("unchecked")
	protected Set<MavenDownload> runImpl() throws IOException {
		LinkedHashMap<IMavenArtifact, String> downloads = new LinkedHashMap<>();
		LinkedHashSet<MavenDownload> ret = new LinkedHashSet<>();
		for(IMavenArtifact artifact : downloadables) {
			downloads.put(artifact, artifact.toString());
		}
		LinkedHashMap<IMavenArtifact, String> downloadTracker = (LinkedHashMap<IMavenArtifact, String>) downloads.clone();
		
		MavenFetchRequest request = new MavenFetchRequest(downloads.values());
		MavenFetchResult fetchResult = fetcher.fetchArtifacts(request);
		
		Iterator<IMavenArtifact> i = downloads.keySet().iterator();
		fetchResult.artifacts().forEach((fetchedArtifact) -> {
			CompletedMavenDownload completed = new CompletedMavenDownload(i.next().type(), fetchedArtifact);
			downloadTracker.remove(completed);
			ret.add(completed);
		});
		
		Iterator<IMavenArtifact> iterator = downloadTracker.keySet().iterator();
		while(iterator.hasNext()) {
			IMavenArtifact failed = iterator.next();
			ret.add(new FailedMavenDownload(failed, fetcher.getLocalRepository()));
			iterator.remove();
		}
		
		if(!downloadTracker.isEmpty()) {
			throw new AssertionError();
		}
		
		for(MavenDownload download : ret) {
			consumer.accept(download);
		}
		
		return ImmutableSet.copyOf(ret);
		
	}

	@Override
	protected ImmutableSet<MavenDownload> getDownloadsInProgress() throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}
	
}
