package org.checkerframework.checker.nullness.qual;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Lightweight compile-time compatibility annotation. */
@Documented
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.TYPE_USE, ElementType.TYPE_PARAMETER, ElementType.FIELD, ElementType.METHOD,
        ElementType.PARAMETER, ElementType.LOCAL_VARIABLE, ElementType.ANNOTATION_TYPE})
public @interface NonNull
{
}
