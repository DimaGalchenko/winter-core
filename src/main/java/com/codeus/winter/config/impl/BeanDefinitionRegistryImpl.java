package com.codeus.winter.config.impl;

import com.codeus.winter.config.BeanDefinition;
import com.codeus.winter.config.BeanDefinitionRegistry;
import com.codeus.winter.exception.BeanDefinitionStoreException;
import java.util.HashMap;
import java.util.Map;

public class BeanDefinitionRegistryImpl implements BeanDefinitionRegistry {
    private final Map<String, BeanDefinition> beanDefinitionMap = new HashMap<>();

    @Override
    public final void registerBeanDefinition(String beanName, BeanDefinition beanDefinition) {
        if (beanDefinitionMap.containsKey(beanName)) {
            throw new BeanDefinitionStoreException(String.format("Cannot register bean definition with name '%s' " +
                 "another bean with the same name already exists and overriding is not allowed.", beanName));
        }
        beanDefinitionMap.put(beanName, beanDefinition);
    }

    @Override
    public final void removeBeanDefinition(String beanName) {
        if (!beanDefinitionMap.containsKey(beanName)) {
            throw new BeanDefinitionStoreException(String.format("No bean definition found for name '%s'", beanName));
        }
        beanDefinitionMap.remove(beanName);
    }

    @Override
    public final BeanDefinition getBeanDefinition(String beanName) {
        return beanDefinitionMap.get(beanName);
    }

    @Override
    public final boolean containsBeanDefinition(String beanName) {
        return beanDefinitionMap.containsKey(beanName);
    }

    @Override
    public final String[] getBeanDefinitionNames() {
        return beanDefinitionMap.keySet().toArray(new String[0]);
    }

    @Override
    public final int getBeanDefinitionCount() {
        return beanDefinitionMap.size();
    }

    @Override
    public Map<String, BeanDefinition> getRegisteredBeanDefinitions() {
        return beanDefinitionMap;
    }
}
