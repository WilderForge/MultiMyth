package com.wildermods.multimyth.internal;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;

@CompileStrictly("1.8")
public class JavaFinder {
	
	private static final Path relativeExecutablePath = Paths.get("bin", "java" + OS.getOS().applicationExtension);
	
	public HashSet<JVMBinary> locate(Path... searchPaths) {
		HashSet<JVMBinary> ret = new HashSet<>();
		ret.add(JVMInstance.thisVM()); //make sure the current vm is added
		LinkedHashSet<Path> searchLocs = new LinkedHashSet<>();
		searchLocs.addAll(Arrays.asList(searchPaths));
		switch(OS.getOS()) {
			case LINUX:
				searchLocs.add(Paths.get("/usr/lib/jvm"));
				break;
			case MAC:
				searchLocs.add(Paths.get("/Library/Java/JavaVirtualMachines"));
				break;
			case WINDOWS:
				String userDir = System.getProperty("user.dir");
				if(userDir == null || userDir.length() < 2) {
					userDir = "C:";
				}
				String defaultDrive = userDir.substring(0, 2); // e.g. "C:"
				
				searchLocs.add(Paths.get(defaultDrive + "/Program Files/Java"));
				searchLocs.add(Paths.get(defaultDrive + "/Program Files (x86)/Java"));
				break;
			default:
				throw new AssertionError();
		}
		
		for(Path p : searchLocs) {
			if(Files.isRegularFile(p)) {
				try {
					if(p.endsWith("java" + OS.getOS().applicationExtension)) {
						JVMBinary instance = JVMInstance.fromPath(p, Paths.get("."));
						ret.add(instance);
					}
					else {
						System.out.println("Does not appear to be a java installation: " + p);
					}
				}
				catch(IOException e) {
					e.printStackTrace();
				}
			}
			else if(Files.isDirectory(p)) {
				try {
					Files.list(p).forEach((child) -> {
						Path javaExecutable = child.resolve(relativeExecutablePath);
						if(Files.exists(javaExecutable)) {
							try {
								JVMBinary instance = JVMInstance.fromPath(javaExecutable, Paths.get("."));
								ret.add(instance);
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					});
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		return ret;
	}
}
