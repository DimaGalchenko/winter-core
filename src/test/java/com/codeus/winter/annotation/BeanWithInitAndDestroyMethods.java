package com.codeus.winter.annotation;

@Component
public class BeanWithInitAndDestroyMethods {

    @PostConstruct
    public void init() {
    }

    @PreDestroy
    public void destroy() {
    }
}
