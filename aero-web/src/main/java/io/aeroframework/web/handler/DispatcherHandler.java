package io.aeroframework.web.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.aeroframework.annotations.web.PathVariable;
import io.aeroframework.annotations.web.RequestParam;
import io.aeroframework.web.error.ProblemDetails;
import io.aeroframework.web.server.HttpRequest;
import io.aeroframework.web.server.HttpResponse;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

public class DispatcherHandler implements WebHandler {

    private final HandlerMapping handlerMapping;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public DispatcherHandler(HandlerMapping handlerMapping) {
        this.handlerMapping = handlerMapping;
    }


    @Override
    public void handle(HttpRequest httpRequest, HttpResponse httpResponse) {
        HandlerExecutionChain chain =  handlerMapping.getHandler(httpRequest);
        if (chain == null || chain.getRoute() == null) {
            sendNotFound(httpRequest, httpResponse);
            return;
        }
        chain.getPathVariables().forEach(httpRequest::setPathVariables);

        Object controller = chain.getRoute().getControllerBean();
        Method method = chain.getRoute().getMethod();
        method.setAccessible(true);

        Object[] args = resolveParam(httpRequest, httpResponse, method);

        try {
            Object result = method.invoke(controller, args);
            sendSuccess(httpRequest, httpResponse, result);
        } catch (IllegalAccessException | InvocationTargetException e) {
            sendError(httpRequest, httpResponse, e);
        }
    }

    private void sendSuccess(HttpRequest httpRequest, HttpResponse httpResponse, Object result) {
        try {
            String body = objectMapper.writeValueAsString(result);
            httpResponse.setHeader("Content-Type", "application/json");
            httpResponse.setBody(body);
        } catch (JsonProcessingException e) {
            sendError(httpRequest, httpResponse, e);
        }
    }

    private Object[] resolveParam(HttpRequest httpRequest, HttpResponse httpResponse, Method method) {
        Parameter[] parameters = method.getParameters();
        Object[] args = new Object[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            if(HttpRequest.class.isAssignableFrom(parameters[i].getType())) {
                args[i] = httpRequest;
            }else if(HttpResponse.class.isAssignableFrom(parameters[i].getType())) {
                args[i] = httpResponse;
            } else if (parameters[i].isAnnotationPresent(RequestParam.class)) {
                RequestParam rp = parameters[i].getAnnotation(RequestParam.class);
                String paramName = !rp.value().isEmpty()?rp.value():parameters[i].getName();
                String value = httpRequest.getQueryParameter(paramName);
                args[i] = convertType(value, parameters[i].getType());
            } else if (parameters[i].isAnnotationPresent(PathVariable.class)) {
                PathVariable vp = parameters[i].getAnnotation(PathVariable.class);
                String paramName = !vp.value().isEmpty()?vp.value():parameters[i].getName();
                String value = httpRequest.getPathVariables().get(paramName);
                args[i] = convertType(value, parameters[i].getType());
            }
        }
        return args;
    }

    private Object convertType(String value, Class<?> parameterType) {
        if(value == null){
            return parameterType.isPrimitive() ? getDefaultValueForPrimitive(parameterType): null;
        }

        if(parameterType==String.class){
            return value;
        }
        if(parameterType==Integer.class){
            return Integer.parseInt(value);
        }
        if(parameterType==Long.class){
            return Long.parseLong(value);
        }
        if(parameterType==Double.class){
            return Double.parseDouble(value);
        }
        if(parameterType==Boolean.class){
            return Boolean.parseBoolean(value);
        }
        return value;
    }

    private Object getDefaultValueForPrimitive(Class<?> parameterType) {
        if(parameterType == int.class){
            return 0;
        }
        if(parameterType == long.class){
            return 0L;
        }
        if(parameterType == double.class){
            return 0.0;
        }
        if(parameterType == boolean.class){
            return false;
        }
        return null;
    }

    private void sendError(HttpRequest httpRequest, HttpResponse httpResponse, Exception e) {
        try {
            String body = objectMapper.writeValueAsString(new ProblemDetails(
                    "INTERNAL_SERVER_ERROR", e.getMessage(), e.getCause().getMessage(), httpRequest.getUri(),500));
            httpResponse.setHeader("Content-Type", "application/json");
            httpResponse.setBody(body);
        } catch (JsonProcessingException ex) {
            throw new RuntimeException(ex);
        }
    }

    private void sendNotFound(HttpRequest httpRequest, HttpResponse httpResponse) {
        try {
            String body = objectMapper.writeValueAsString(new ProblemDetails(
                    "NOT_FOUND", "Route not found", "Route Not Found", httpRequest.getUri(),404));
            httpResponse.setHeader("Content-Type", "application/json");
            httpResponse.setBody(body);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
