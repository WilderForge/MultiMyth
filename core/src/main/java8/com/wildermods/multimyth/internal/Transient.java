package com.wildermods.multimyth.internal;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.CLASS;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Used by {@link GsonHelper#GSON} to exclude fields and record
 * components which are intended to be transient.
 */
@Retention(CLASS)
@Target({ FIELD })
@CompileStrictly("1.8")
public @interface Transient {}