package io.aeroframework.core.util;

import java.lang.annotation.Annotation;

public final class AnnotationUtils {

    public static boolean isComponent(Class<?> clazz){
        if(clazz.isAnnotationPresent(io.aeroframework.annotations.Component.class)){
            return true;
        }

        for(Annotation annotation : clazz.getAnnotations()){
            if(annotation.annotationType().isAnnotationPresent(io.aeroframework.annotations.Component.class)){
                return true;
            }
        }

    return false;
    }
}
