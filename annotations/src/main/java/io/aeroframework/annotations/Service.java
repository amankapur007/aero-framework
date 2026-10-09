package io.aeroframework.annotations;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Documented
@Component
@Retention(RetentionPolicy.RUNTIME)
public @interface Service {
}
