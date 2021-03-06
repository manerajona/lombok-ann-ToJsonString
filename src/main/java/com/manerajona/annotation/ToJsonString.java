package com.manerajona.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.SOURCE;

@Target(TYPE)
@Retention(SOURCE)
public @interface ToJsonString {
    /**
     * Include the result of the superclass's implementation of {@code toString} in the output.
     * <strong>default: false</strong>
     *
     * @return Whether to call the superclass's {@code toString} implementation as part of the generated toJsonString algorithm.
     */
    boolean callSuper() default false;

    /**
     * Include the implementation of {@code toString} with masked values.
     * <strong>default: false</strong>
     *
     * @return {@code toString} implementation as part of the generated toJsonString algorithm.
     */
    boolean isSecret() default false;
}
