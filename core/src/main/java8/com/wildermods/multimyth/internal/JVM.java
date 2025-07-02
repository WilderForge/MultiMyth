package com.wildermods.multimyth.internal;

import java.util.Base64;
import java.util.Base64.Encoder;
import java.util.Properties;

/**
 * This interface bootstraps JVM introspection. Its {@code main} method is intended to be invoked within a child JVM,
 * and its output is designed to be consumed and parsed by that JVM's parent.
 * <p>
 * It is annotated with {@code @CompileStrictly("1.8")} to ensure compatibility with older JVM targets.
 * 
 * @see JavaInstance
 */
@FunctionalInterface
@CompileStrictly("1.8")
public interface JVM { //REFERENCED LITERALLY in JavaInstance
	
	/**
	 * @return the system properties associated with this JVM instance
	 */
	public Properties getProperties();
	
	public static void main(String[] args) {
		System.out.println('\02');
		Encoder encoder = Base64.getEncoder();
		System.getProperties().forEach((k, v) -> System.out.println(new String(encoder.encode((k + "=" + v).toString().getBytes()))));
		System.out.println('\04');
		//System.getProperties().forEach((k, v) -> System.out.println((k + "=" + v)));
	}
	
}
