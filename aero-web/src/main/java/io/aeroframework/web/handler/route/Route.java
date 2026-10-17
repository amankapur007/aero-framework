package io.aeroframework.web.handler.route;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Route {

    private String VARIABLE_PATTERN_REGEX="\\{([A-Za-z0-9]+)\\}";

    private final Pattern variablePattern = Pattern.compile(VARIABLE_PATTERN_REGEX);
    private String rawPath;
    private String httpMethod;
    private List<String> variableNames;
    private Pattern compiledPathRegex;
    private Object controllerBean;
    private Method method;

    public Route(Object controllerBean, Method method, String rawPath, String httpMethod) {
        this.controllerBean = controllerBean;
        this.method = method;
        this.rawPath = normalizePath(rawPath);
        this.httpMethod = httpMethod;
        this.variableNames = new ArrayList<>();
        this.compiledPathRegex=compiledPath(this.rawPath);
    }

    private Pattern compiledPath(String rawPath) {
        Matcher matcher = variablePattern.matcher(rawPath);
        while (matcher.find()) {
            String variableName = matcher.group(1);
            variableNames.add(variableName);
        }

        String p= "^"+rawPath.replaceAll(VARIABLE_PATTERN_REGEX,"([^/]+)")+"$";
        return Pattern.compile(p);
    }

    public boolean isMatch(String requestPath, Map<String, String> extractedVariables) {
        Matcher matcher = compiledPathRegex.matcher(requestPath);
        if(!matcher.find()){
            return false;
        }
        for(int i=0;i<variableNames.size();i++){
            String variableName = variableNames.get(i);
            String variableValue = matcher.group(i+1);
            extractedVariables.put(variableName, variableValue);
        }
        return true;
    }

    public static String normalizePath(String path) {
        if(path == null || path.isBlank()) {
            return "/";
        }

        String p=path;
        if(!p.startsWith("/")) {
            p="/"+p;
        }

        if(p.length()>1 && p.endsWith("/")) {
            p=p.substring(0,p.length()-1);
        }

        return p;
    }

    public List<String> getVariableNames() {
        return Collections.unmodifiableList(variableNames);
    }

    public Method getMethod() {
        return method;
    }

    public Object getControllerBean() {
        return controllerBean;
    }

    public String getRawPath() {
        return rawPath;
    }
    public String getHttpMethod() {
        return httpMethod;
    }
}
