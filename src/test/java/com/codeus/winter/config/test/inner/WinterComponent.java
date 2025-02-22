package com.codeus.winter.config.test.inner;

import com.codeus.winter.annotation.Autowired;
import com.codeus.winter.annotation.Component;
import com.codeus.winter.annotation.PostConstruct;
import com.codeus.winter.annotation.PreDestroy;
import com.codeus.winter.annotation.Primary;
import com.codeus.winter.annotation.Qualifier;
import com.codeus.winter.annotation.Scope;

/**
 * Mock class annotated with {@link Component} to simulate a real component.
 */
@Primary
@Component
@Scope("prototype")
public class WinterComponent {

    private AutowiredComponent autowiredComponent;

    @Qualifier("value")
    private QualifierComponent qualifierComponent;

    @Autowired
    public WinterComponent(AutowiredComponent autowiredComponent,
        QualifierComponent qualifierComponent) {
        this.autowiredComponent = autowiredComponent;
        this.qualifierComponent = qualifierComponent;
    }

    @Autowired
    public void setAutowiredComponent(AutowiredComponent autowiredComponent) {
        this.autowiredComponent = autowiredComponent;
    }

    @PostConstruct
    public void init() {
    }

    @PreDestroy
    public void destroy() {
    }
}
