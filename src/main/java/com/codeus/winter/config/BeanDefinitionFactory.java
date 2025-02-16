package com.codeus.winter.config;

import com.codeus.winter.config.impl.BeanDefinitionImpl;

/**
 * A factory containing convenient methods to facilitate {@link BeanDefinition} creation and
 * to enclose details about {@link BeanDefinition} implementation in this class.
 * NOTE: Internal use only.
 */
final class BeanDefinitionFactory {

    private BeanDefinitionFactory() {
    }

    /**
     * Creates simple {@link BeanDefinition} with {@link BeanDefinition#SCOPE_PROTOTYPE} scope.
     *
     * @param beanClass a class to create a bean definition for.
     * @return a bean definition with {@link BeanDefinition#SCOPE_PROTOTYPE} scope.
     */
    static BeanDefinition prototypeBeanDefinition(Class<?> beanClass) {
        BeanDefinition beanDefinition = new BeanDefinitionImpl();
        beanDefinition.setBeanClassName(beanClass.getName());
        beanDefinition.setScope(BeanDefinition.SCOPE_PROTOTYPE);

        return beanDefinition;
    }
}
