package io.aeroframework.core;

public class BeanDefination {
    private final boolean isSingleton;
    private final String beanName;
    private final Class<?> beanType;

    BeanDefination(String beanName, Class<?> beanType) {
        this.beanName = beanName;
        this.beanType = beanType;
        isSingleton = true;
    }

    BeanDefination(boolean isSingleton, String beanName, Class<?> beanType) {
        this.isSingleton = isSingleton;
        this.beanName = beanName;
        this.beanType = beanType;
    }
    public boolean isSingleton() {
        return isSingleton;
    }

    public String getBeanName() {
        return beanName;
    }

    public Class<?> getBeanType() {
        return beanType;
    }
}
