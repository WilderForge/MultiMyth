package com.wildermods.multimyth.internal;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

@CompileStrictly("1.8")
public class JavaInstance implements JVM {

	private static final String JAVA_HOME = System.getProperty("java.home");
	private static final String JAVA_SPEC_VERSION_KEY = "java.specification.version";
	private static final String JAVA_SPEC_VERSION = System.getProperty(JAVA_SPEC_VERSION_KEY);
	
	public final String version;
	public final Path jvmLocation;
	private final transient Path launchDir;
	private transient Properties properties;
	
	JavaInstance(String version, Path jvmLocation, Path launchDir) {
		this.version = version;
		this.jvmLocation = jvmLocation;
		this.launchDir = launchDir;
	}
	
	JavaInstance(String version, Path jvmLocation, Path launchDir, Properties properties) {
		this.version = version;
		this.jvmLocation = jvmLocation;
		this.launchDir = launchDir;
		this.properties = properties;
	}
	
	JavaInstance() {
		this(JAVA_SPEC_VERSION, currentJVMBinaryPath(), Paths.get(System.getProperty("user.dir")), System.getProperties());
	}
	
	public static JavaInstance fromCurrentVM() {
		return new JavaInstance();
	}
	
	public static JavaInstance fromPath(Path jvmLocation, Path launchDir) throws IOException {
		
		String currentClasspath = System.getProperty("java.class.path");
		List<String> classpathEntries = new ArrayList<>();
		
		Collections.addAll(classpathEntries, currentClasspath.split(File.pathSeparator));
		
		Collections.addAll(classpathEntries, //make sure we add the game and its dependencies to the child JVM's classpath
			launchDir.resolve("*").toAbsolutePath().toString(), 
			launchDir.resolve("lib").resolve("*").toAbsolutePath().toString()
		);
		
		String fullClasspath = String.join(File.pathSeparator, classpathEntries);
		
		ProcessBuilder pb = new ProcessBuilder(
			jvmLocation.toAbsolutePath().toString(),
			"-cp",
			fullClasspath,
			JVM.class.getName()
		);
		
		pb.directory(launchDir.toFile());
		
		try {
			Process process = pb.start();
			Properties properties = new Properties();
			boolean parsingProperties = false;
			boolean finished = false;
			
			try(BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
				String line;
				while((line = reader.readLine()) != null) {
					if(line.indexOf('\2') != -1) {
						parsingProperties = true;
						continue;
					}
					if(line.indexOf('\4') != -1) {
						parsingProperties = false;
						finished = true;
						break;
					}
					if (parsingProperties) {
						Decoder decoder = Base64.getDecoder();
						line = new String(decoder.decode(line));
						int split = line.indexOf('=');
						if(split > 0) {
							String key = line.substring(0, split);
							String value = line.substring(split + 1);
							if(properties.setProperty(key, value) != null) {
								throw new AssertionError("Property " + key + " was already set?!");
							}
						}
					}
				}
			}
			catch(Exception e) {
				e.printStackTrace();
			}
			
			int exitCode = process.waitFor();
			if(exitCode != 0 || properties.isEmpty() || !finished) {
				throw new IOException("Failed to read properties from JVM at " + jvmLocation + " (Exit code: " + exitCode + ")");
			}
			String version = properties.getProperty(JAVA_SPEC_VERSION_KEY);
			if (version == null) {
				throw new IOException("External JVM didn't report version: " + jvmLocation);
			}
			
			JavaInstance jvm = new JavaInstance(version, jvmLocation, launchDir);
			jvm.properties = properties;
			
			return jvm;
		}
		catch (InterruptedException e) {
			throw new IOException(e);
		}
	}
	
	
	public boolean isValid() {
		return version != null && version.length() > 0 && Files.exists(jvmLocation) && properties != null && !properties.isEmpty();
	}
	
	public final String getVersion() {
		return version;
	}
	
	public final Path getJVMLocation() {
		return jvmLocation;
	}
	
	public final Path getLaunchDir() {
		return launchDir;
	}
	
	@Override
	public final boolean equals(Object o) {
		if(o instanceof JavaInstance) {
			return Objects.equals(this.jvmLocation, ((JavaInstance)o).jvmLocation) && Objects.equals(this.version, ((JavaInstance)o).version);
		}
		return false;
	}
	
	@Override
	public final int hashCode() {
		return Objects.hash(this.jvmLocation.toAbsolutePath(), this.version);
	}

	@Override
	public final Properties getProperties() {
		return properties;
	}
	
	public static JavaInstance ofCurrent() throws IOException {
		JavaInstance current = new JavaInstance();
		if(!current.isValid()) {
			throw new AssertionError();
		}
		return current;
	}
	
	private static Path currentJVMBinaryPath() {
		return Paths.get(JAVA_HOME).resolve("bin").resolve("java" + OS.getOS().applicationExtension);
	}
	
}
