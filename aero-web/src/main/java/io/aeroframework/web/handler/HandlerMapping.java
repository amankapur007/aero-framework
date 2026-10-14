package io.aeroframework.web.handler;

import io.aeroframework.annotations.Controller;
import io.aeroframework.annotations.web.*;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 */
public class HandlerMapping {

    List<String> routes = new ArrayList<>();

    public List<String> getRoutes() {
        return routes;
    }

    public void registerController(Object controllerBean) {
        if(controllerBean == null)
            return;
        Class<?> controllerClass = controllerBean.getClass();
        String basePath = "";
        if(controllerClass.isAnnotationPresent(Controller.class)){
            if(controllerClass.isAnnotationPresent(RequestMapping.class)){
                basePath = controllerClass.getAnnotation(RequestMapping.class).value();
            }

            Method[] methods = controllerClass.getDeclaredMethods();
            for(Method method : methods){
                if(method.isAnnotationPresent(GetMapping.class)){
                    String subPath = method.getAnnotation(GetMapping.class).value();
                    String methodString = "GET";
                    String fullPath = basePath + "/" + subPath;
                    routes.add(fullPath+"-"+methodString+"-"+method.getName()+"-"+ Arrays.toString(method.getParameters()));
                } else if(method.isAnnotationPresent(PostMapping.class)){
                    String subPath = method.getAnnotation(PostMapping.class).value();
                    String methodString = "POST";
                    String fullPath = basePath + "/" + subPath;
                    routes.add(fullPath+"-"+methodString+"-"+method.getName()+"-"+ Arrays.toString(method.getParameters()));
                } else if(method.isAnnotationPresent(PutMapping.class)){
                    String subPath = method.getAnnotation(PutMapping.class).value();
                    String methodString = "PUT";
                    String fullPath = basePath + "/" + subPath;
                    routes.add(fullPath+"-"+methodString+"-"+method.getName()+"-"+ Arrays.toString(method.getParameters()));
                } else if(method.isAnnotationPresent(PatchMapping.class)){
                    String subPath = method.getAnnotation(PatchMapping.class).value();
                    String methodString = "PATCH";
                    String fullPath = basePath + "/" + subPath;
                    routes.add(fullPath+"-"+methodString+"-"+method.getName()+"-"+ Arrays.toString(method.getParameters()));
                } else if(method.isAnnotationPresent(DeleteMapping.class)){
                    String subPath = method.getAnnotation(DeleteMapping.class).value();
                    String methodString = "DELETE";
                    String fullPath = basePath + "/" + subPath;
                    routes.add(fullPath+"-"+methodString+"-"+method.getName()+"-"+ Arrays.toString(method.getParameters()));
                }
            }
        }
    }
}
