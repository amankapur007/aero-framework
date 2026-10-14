package io.aeroframework.annotations.web;


import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD,  ElementType.TYPE})
@Documented
public @interface RequestMapping {
    String value() default "";
}
