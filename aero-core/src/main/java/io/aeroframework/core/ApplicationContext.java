package io.aeroframework.core;

import io.aeroframework.annotations.Component;
import io.aeroframework.core.util.AnnotationUtils;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ConfigurationBuilder;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ApplicationContext implements BeanFactory {

    private final Map<String, BeanDefination> beanRegistry = new ConcurrentHashMap<>();
    private final Map<String, Object> singletonObjects = new ConcurrentHashMap<>();

    //run
    public void run(String packageName){
        scan(packageName);
        intializeSingletons();
        injectDependencies();
    }
    //scan
    public void scan(String basePackage) {
        ConfigurationBuilder configurationBuilder = new ConfigurationBuilder()
                .addScanners(Scanners.TypesAnnotated, Scanners.SubTypes.filterResultsBy(s -> true))
                .forPackage(basePackage);
        Reflections reflections = new Reflections(configurationBuilder);

        Set<Class<?>> classes = reflections.get(Scanners.SubTypes.of(Object.class).asClass());
        for (Class<?> clazz : classes) {
            if(clazz.isInterface() || clazz.isAnnotation() || Modifier.isAbstract(clazz.getModifiers())){
                continue;
            }
            if(AnnotationUtils.isComponent(clazz)){
                if(!singletonObjects.containsKey(clazz.getName())){
                    String beanName = generateBeanName(clazz);
                    BeanDefination beanDefination = new BeanDefination(beanName, clazz);
                    beanRegistry.put(beanName, beanDefination);
                }
            }
        }
    }

    //create bean

    public void intializeSingletons(){
        for(BeanDefination beanDefination : beanRegistry.values()){
            if(beanDefination.isSingleton() && !singletonObjects.containsKey(beanDefination.getBeanName())){
                createBean(beanDefination);
            }
        }
    }

    public void injectDependencies(){
        for(Object bean : singletonObjects.values()){
            Class<?> targetClass = bean.getClass();
            if(targetClass != Object.class){
                for(Field field : targetClass.getDeclaredFields()){
                    field.setAccessible(true);
                    if(field.isAnnotationPresent(io.aeroframework.annotations.Autowired.class)){
                        Object dependency = getBean(field.getType());
                        try {
                            field.set(bean, dependency);
                        } catch (IllegalAccessException e) {
                            throw new RuntimeException("Failed to inject dependency " + dependency, e);
                        }
                    }
                }
            }

        }
    }

    private void createBean(BeanDefination beanDefination) {
        try{
            Object instance = beanDefination.getBeanType().getDeclaredConstructor().newInstance();
            singletonObjects.put(beanDefination.getBeanName(), instance);
        }catch (Exception e){
            throw new RuntimeException("Unable to create bean of type "+ beanDefination.getBeanType());
        }
    }

    private String generateBeanName(Class<?> clazz) {
        String name = clazz.getSimpleName();
        return Character.toLowerCase(name.charAt(0)) + name.substring(1);
    }

    //inject

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getBean(Class<T> requiredType) {
        List<Object> matchers = new ArrayList<>();
        for (Object bean: singletonObjects.values()) {
            if (requiredType.isInstance(bean)){
                matchers.add(bean);
            }

            if(matchers.size()>1){
                throw new IllegalArgumentException("Multiple beans of type " + requiredType.getName() + " found");
            }
        }

        if(matchers.isEmpty()){
            throw new NoSuchElementException("No qualifying bean found for type " + requiredType.getName());
        }
        return (T)matchers.getFirst();
    }

    @Override
    public <T> T getBean(String name, Class<?> requiredType) {
        if(singletonObjects.containsKey(name)){
            if(requiredType.isInstance(singletonObjects.get(name))){
                return (T)singletonObjects.get(name);
            }
        }
        throw new NoSuchElementException("No qualifying bean found for type " + requiredType.getName()+ " and bean name " + name);
    }

    @Override
    public Object getBean(String name) {
        if(singletonObjects.containsKey(name)){
            return singletonObjects.get(name);
        }
        throw new NoSuchElementException("No qualifying bean found for bean name " + name);
    }

    @Override
    public Map<String, Object> getBeans(Class<?> requiredType) {
        Map<String,Object> beans = new HashMap<>();
        for (Object bean: singletonObjects.values()) {
            if (requiredType.isInstance(bean)){
                beans.put(bean.getClass().getName(), bean);
            }
        }
        return beans;
    }

    @Override
    public boolean containsBean(String name) {
        return singletonObjects.containsKey(name);
    }

}
