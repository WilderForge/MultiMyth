package com.wildermods.multimyth.internal;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.CLASS;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Target(TYPE)
@Retention(CLASS)
@CompileStrictly("1.8") //Self annotation marks this annotation to be compiled with java 8 so it can actually be used to enforce Java 8 compliance elsewhere. We can't put java 21 annotation bytecode onto a java 8 class and expect a java 8 compiler to handle it.
public @interface CompileStrictly { //REFERENCED LITERALLY IN verifyCompilation.gradle
	
	/**
	* The project's default Java version (for modern classes)
	*/
	public static final String PROJECT_COMPILATION_VERSION = "21"; //REFERENCED LITERALLY IN verifyCompilation.gradle
	
	String value() default PROJECT_COMPILATION_VERSION;
	
}
