package io.aeroframework.annotations;

import java.lang.annotation.*;

@Target(ElementType.FIELD)
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface Autowired {
}
