package io.aeroframework.web.handler;

import io.aeroframework.annotations.Controller;
import io.aeroframework.annotations.web.*;
import io.aeroframework.web.handler.route.Route;
import io.aeroframework.web.server.HttpRequest;

import java.lang.reflect.Method;
import java.util.*;

/**
 *
 */
public class HandlerMapping {

    List<Route> routes = new ArrayList<>();

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
                    register(basePath, subPath,methodString, controllerBean, method);
                } else if(method.isAnnotationPresent(PostMapping.class)){
                    String subPath = method.getAnnotation(PostMapping.class).value();
                    String methodString = "POST";
                    register(basePath, subPath,methodString, controllerBean, method);
                } else if(method.isAnnotationPresent(PutMapping.class)){
                    String subPath = method.getAnnotation(PutMapping.class).value();
                    String methodString = "PUT";
                    register(basePath, subPath,methodString, controllerBean, method);
                } else if(method.isAnnotationPresent(PatchMapping.class)){
                    String subPath = method.getAnnotation(PatchMapping.class).value();
                    String methodString = "PATCH";
                    register(basePath, subPath,methodString, controllerBean, method);
                } else if(method.isAnnotationPresent(DeleteMapping.class)){
                    String subPath = method.getAnnotation(DeleteMapping.class).value();
                    String methodString = "DELETE";
                    register(basePath, subPath,methodString, controllerBean, method);
                } else if (method.isAnnotationPresent(RequestMapping.class)){
                    String subPath = method.getAnnotation(RequestMapping.class).value();
                    String methodString = method.getAnnotation(RequestMapping.class).method();
                    register(basePath, subPath,methodString, controllerBean, method);
                }
            }
        }
    }

    public void register(String basePath, String subPath, String httpMethod, Object controllerBean, Method method) {
        basePath = Route.normalizePath(basePath) + Route.normalizePath(subPath);
        Route route=new Route(controllerBean, method, basePath, httpMethod);
        routes.add(route);
    }

    public HandlerExecutionChain getHandler(HttpRequest httpRequest){
        String path = Route.normalizePath(httpRequest.getUri().getPath());
        String method = httpRequest.getHttpMethod();
        for(Route route : routes){
            Map<String, String > extractedVariables = new HashMap<>();
            if(route.isMatch(path, extractedVariables)){
                return new HandlerExecutionChain(route, extractedVariables);
            }
        }
        return null;
    }
}
