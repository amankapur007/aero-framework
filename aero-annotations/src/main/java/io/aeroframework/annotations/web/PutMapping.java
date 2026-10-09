package io.aeroframework.annotations.web;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface PutMapping {
    String value() default "";
}
