package com.MohafizDZ.framework_repository.core;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public class MAnnotations {
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface Functional {
        String method() default "";
        String[] depends() default {};
        boolean onlyToStore() default true;
    }
}
