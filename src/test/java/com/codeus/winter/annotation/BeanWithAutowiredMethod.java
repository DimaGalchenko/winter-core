package com.codeus.winter.annotation;

@Component
@SuppressWarnings("unused")
public class BeanWithAutowiredMethod {

    private BeanComponent dependency;

    public BeanWithAutowiredMethod() {
        // default constructor
    }

    @Autowired
    public void setDependency(BeanComponent dependency) {
        this.dependency = dependency;
    }

    public BeanComponent getDependency() {
        return dependency;
    }
}
