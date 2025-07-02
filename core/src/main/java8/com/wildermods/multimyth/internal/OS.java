package com.wildermods.multimyth.internal;

@CompileStrictly("1.8")
public enum OS {

	LINUX,
	MAC,
	WINDOWS(".exe");

	public final String applicationExtension;
	
	OS(String applicationExtension) {
		this.applicationExtension = applicationExtension;
	}

	OS() {
		this("");
	}
	
	public static OS getOS() {
		String osName = System.getProperty("os.name").toLowerCase();

		if (osName.contains("win")) {
			return OS.WINDOWS;
		} else if (osName.contains("mac")) {
			return OS.MAC;
		} else if (osName.contains("nix") || osName.contains("nux") || osName.contains("aix")) {
			return OS.LINUX;
		} else {
			throw new UnsupportedOperationException("Unsupported operating system: " + osName);
		}
	}
	
}
