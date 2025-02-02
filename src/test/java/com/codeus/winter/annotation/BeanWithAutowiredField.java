package com.codeus.winter.annotation;

@Component
@SuppressWarnings("unused")
public class BeanWithAutowiredField {

    @Autowired
    private BeanComponent dependency;


    public BeanWithAutowiredField() {
        // default constructor
    }

    public BeanComponent getDependency() {
        return dependency;
    }
}
