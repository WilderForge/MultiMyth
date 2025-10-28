package com.wildermods.multimyth.internal;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Properties;

@CompileStrictly("1.8")
public class JVMBinary {

	protected static final String JAVA_HOME_KEY = "java.home";
	protected static final String JAVA_HOME = System.getProperty(JAVA_HOME_KEY);
	protected static final String JAVA_SPEC_VERSION_KEY = "java.specification.version";
	protected static final String JAVA_SPEC_VERSION = System.getProperty(JAVA_SPEC_VERSION_KEY);
	
	public final String version;
	public final Path jvmLocation;
	
	JVMBinary(String version, Path jvmLocation) {
		this(version, jvmLocation, null);
	}
	
	JVMBinary(String version, Path jvmLocation, Properties properties) {
		this.version = version;
		this.jvmLocation = jvmLocation;
	}
	
	JVMBinary(Properties properties) {
		version = properties.getProperty(JAVA_SPEC_VERSION_KEY);
		jvmLocation = Paths.get(properties.getProperty(JAVA_HOME_KEY));
	}
	
	public JVMBinary(InputStream stream) throws SerializationException {
		this(deserialize(stream));
	}
	
	protected static Properties deserialize(InputStream stream) throws SerializationException {
		Properties properties = new Properties();
		try {
			properties.loadFromXML(stream);
		} catch (IOException e) {
			throw new SerializationException(e);
		}
		return properties;
	}
	
	public final String getVersion() {
		return version;
	}
	
	public final Path getJVMLocation() {
		return jvmLocation;
	}
	
	@Override
	public final boolean equals(Object o) {
		if(o instanceof JVMBinary) {
			return Objects.equals(this.jvmLocation, ((JVMBinary)o).jvmLocation) && Objects.equals(this.version, ((JVMBinary)o).version);
		}
		return false;
	}
	
	@Override
	public final int hashCode() {
		return Objects.hash(this.jvmLocation.toAbsolutePath(), this.version);
	}
	
	protected static Path currentJVMBinaryPath() {
		return Paths.get(JAVA_HOME).resolve("bin").resolve("java" + OS.getOS().applicationExtension);
	}

	public OutputStream serialize() throws SerializationException {
		Properties properties = new Properties();
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
	
}
