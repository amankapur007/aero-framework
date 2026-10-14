package io.aeroframework.annotations.web;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface RequestParam {
    String value() default "";
    String defaultValue() default "";
    boolean required() default true;
}
