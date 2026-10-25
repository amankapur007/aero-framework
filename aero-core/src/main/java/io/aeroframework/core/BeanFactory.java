package io.aeroframework.core;

import java.lang.annotation.Annotation;
import java.util.Map;

public interface BeanFactory {
    <T> T getBean( Class<T> requiredType );
    <T> T  getBean( String name,  Class<?> requiredType );
    Object  getBean( String name);
    Map<String,Object> getBeans( Class<?> requiredType );
    boolean containsBean( String name );
    Object getBeansByAnnotation(Class<? extends Annotation> annotation);

    Map<String, Object> getAllBeans();
}
