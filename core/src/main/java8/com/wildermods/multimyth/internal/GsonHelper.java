package com.wildermods.multimyth.internal;

import java.lang.reflect.Modifier;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@CompileStrictly("1.8")
public class GsonHelper {

	public static final Gson GSON;
	static {
		GsonBuilder b = new GsonBuilder();
		b.setPrettyPrinting();
		
		ExclusionStrategy excludeTransients = new ExclusionStrategy() {

			@Override
			public boolean shouldSkipField(FieldAttributes f) {
				return (f.hasModifier(Modifier.TRANSIENT) || f.getAnnotation(Transient.class) != null);
			}

			@Override
			public boolean shouldSkipClass(Class<?> clazz) {
				return false;
			}
			
		};
		
		b.addSerializationExclusionStrategy(excludeTransients);
		b.addDeserializationExclusionStrategy(excludeTransients);
		GSON = b.create();
	}
	
}
