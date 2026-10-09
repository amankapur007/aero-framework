package io.aeroframework.web;

import io.aeroframework.annotations.web.PathVariable;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RouteDefinition {
    private final String rawPath;
    private final String method;
    private final Object controllerInstance;
    private final Method controllerMethod;
    private final List<String> pathVariablesNames;
    private final Pattern compiledPattern;

    private final Pattern PATH_PATTERN = Pattern.compile("\\{([^}]+)}");
    public RouteDefinition(String path, Method controllerMethod, Object controllerInstance) {
        //Path
        if(path == null || path.isEmpty()){
            rawPath = "/";
        }else if(!path.startsWith("/")){
            rawPath = "/" + path;
        }else {
            rawPath = path;
        }

        method = controllerMethod.getName().toUpperCase();
        this.controllerMethod = controllerMethod;
        this.controllerInstance = controllerInstance;
        this.pathVariablesNames = new ArrayList<>();

        //extract variables inside {}
        Matcher matcher = PATH_PATTERN.matcher(rawPath);
        while(matcher.find()){
            pathVariablesNames.add(matcher.group(1));
        }
        String regex = "^" + this.rawPath.replaceAll("\\{[^}]+}", "([^/]+)") + "$";
        this.compiledPattern = Pattern.compile(regex);
    }

    public boolean matches(String requestMethod, String requestPath) {
        if (!this.method.equalsIgnoreCase(requestMethod)) {
            return false;
        }
        return this.compiledPattern.matcher(requestPath).matches();
    }

    public List<String> extractVariables(String requestPath) {
        Matcher matcher = this.compiledPattern.matcher(requestPath);
        List<String> values = new ArrayList<>();
        if (matcher.matches()) {
            for (int i = 1; i <= matcher.groupCount(); i++) {
                values.add(matcher.group(i));
            }
        }
        return values;
    }

    public List<String> getPathVariableNames() {
        return pathVariablesNames;
    }

    public Method getControllerMethod() {
        return controllerMethod;
    }

    public Object getControllerInstance() {
        return controllerInstance;
    }
}
