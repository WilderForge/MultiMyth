package com.wildermods.multimyth.internal;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.Properties;
import java.util.Base64.Decoder;
import java.util.concurrent.TimeUnit;

public class JVMInstance extends JVMBinary implements JVM {

	private final Properties properties;
	
	JVMInstance(String version, Path jvmLocation) {
		this(new JVMBinary(version, jvmLocation));
	}
	
	JVMInstance(String version, Path jvmLocation, Properties properties) {
		this(new JVMBinary(version, jvmLocation), properties);
	}
	
	JVMInstance(JVMBinary binary) {
		this(binary, new Properties());
	}
	
	JVMInstance(JVMBinary binary, Properties properties) {
		super(binary.version, binary.jvmLocation);
		this.properties = properties;
	}
	
	public JVMInstance(InputStream stream) throws SerializationException {
		super(this.properties = deserialize(stream));
	}
	
	public static JVMInstance thisVM() {
		JVMInstance current = new JVMInstance(JAVA_SPEC_VERSION, currentJVMBinaryPath(), System.getProperties());
		if(!current.isValid()) {
			throw new AssertionError();
		}
		return current;
	}
	
	public static JVMBinary fromPath(Path jvmLocation, Path launchDir) throws IOException {
		
		String currentClasspath = System.getProperty("java.class.path");
		List<String> classpathEntries = new ArrayList<>();
		
		Collections.addAll(classpathEntries, currentClasspath.split(File.pathSeparator));
		
		Collections.addAll(classpathEntries, //make sure we add the game and its dependencies to the child JVM's classpath
			launchDir.resolve("*").toAbsolutePath().toString(), 
			launchDir.resolve("lib").resolve("*").toAbsolutePath().toString()
		);
		
		boolean isDevelopment = classpathEntries.stream().anyMatch((entry) -> {
			return entry.contains("/bin/java8") || entry.contains("/bin/main") || entry.contains("/bin/test");
		});
		
		if(isDevelopment) {
			ListIterator<String> i = classpathEntries.listIterator();
			while(i.hasNext()) {
				String classpathEntry = i.next();
				classpathEntry = classpathEntry.replace("/bin/java8", "/build/classes/java/java8");
				classpathEntry = classpathEntry.replace("/bin/main", "/build/classes/java/main");
				classpathEntry = classpathEntry.replace("/bin/test", "/build/classes/java/test");
				i.set(classpathEntry);
			}
		}
		
		String fullClasspath = String.join(File.pathSeparator, classpathEntries);
		
		ProcessBuilder pb = new ProcessBuilder(
			jvmLocation.toAbsolutePath().toString(),
			"-cp",
			fullClasspath,
			JVM.class.getName()
		);
		
		pb.directory(launchDir.toFile());
		
		try {
			/*
				for(String arg : pb.command()) {
					System.out.print(arg);
					System.out.print(" ");
				}
				System.out.println();
			*/

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
			
			if(!process.waitFor(5, TimeUnit.SECONDS)) {
				process.destroyForcibly();
			}
			int exitCode = process.waitFor();
			if(exitCode != 0 || properties.isEmpty() || !finished) {
				throw new IOException("Failed to read properties from JVM at " + jvmLocation + " (Exit code: " + exitCode + ")");
			}
			String version = properties.getProperty(JAVA_SPEC_VERSION_KEY);
			if (version == null) {
				throw new IOException("External JVM didn't report version: " + jvmLocation);
			}
			
			JVMInstance jvm = new JVMInstance(version, jvmLocation, properties);
			
			return jvm;
		}
		catch (InterruptedException e) {
			throw new IOException(e);
		}
	}
	
	@Override
	public final Properties getProperties() {
		return properties;
	}
	
	public OutputStream serialize() throws SerializationException {
		properties.put("version", version);
		properties.put("jvmLocation", jvmLocation.toString());
		ByteArrayOutputStream ret = new ByteArrayOutputStream();
		try {
			properties.storeToXML(ret, null, "utf8");
		} catch (IOException e) {
			throw new SerializationException(e);
		}
		return ret;
	}
	
	public boolean isValid() {
		return version != null && version.length() > 0 && Files.exists(jvmLocation) && properties != null && !properties.isEmpty();
	}

}
