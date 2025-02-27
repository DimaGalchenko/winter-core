package com.codeus.winter.context.test;

import com.codeus.winter.annotation.Autowired;
import com.codeus.winter.annotation.Component;
import com.codeus.winter.annotation.PostConstruct;
import com.codeus.winter.annotation.PreDestroy;

@Component
@SuppressWarnings({"unused"})
public class ComplexBean {

    @Autowired
    private SimpleBean dependency;


    public ComplexBean() {
        // default constructor
    }

    public SimpleBean getDependency() {
        return dependency;
    }

    @PostConstruct
    public void init() {
        // empty init method
    }

    @PreDestroy
    public void destroy() {
        // empty destroy method
    }
}
