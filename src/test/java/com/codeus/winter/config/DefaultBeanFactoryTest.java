package com.codeus.winter.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.codeus.winter.config.impl.BeanDefinitionImpl;
import com.codeus.winter.exception.BeanFactoryException;
import com.codeus.winter.exception.BeanNotFoundException;
import com.codeus.winter.exception.NotUniqueBeanDefinitionException;
import com.codeus.winter.test.BeanA;
import com.codeus.winter.test.BeanB;
import com.codeus.winter.test.BeanC;
import com.codeus.winter.test.BeanD;
import com.codeus.winter.test.BeanE;
import com.codeus.winter.test.Common;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

class DefaultBeanFactoryTest {
    private final BeanDefinition beanDefinitionA = mock(BeanDefinition.class);
    private final BeanDefinition beanDefinitionB = mock(BeanDefinition.class);
    private final BeanDefinition beanDefinitionC = mock(BeanDefinition.class);
    private final BeanDefinition beanDefinitionD = mock(BeanDefinition.class);
    private final BeanDefinition beanDefinitionE = mock(BeanDefinition.class);
    private final BeanDefinition cycleBeanDefinitionOne = mock(BeanDefinition.class);
    private final BeanDefinition cycleBeanDefinitionTwo = mock(BeanDefinition.class);

    @BeforeEach
    void setUpBeforeEach() {
        when(beanDefinitionA.getBeanClassName()).thenReturn("com.codeus.winter.test.BeanA");
        when(beanDefinitionA.isSingleton()).thenReturn(true);

        when(beanDefinitionB.getBeanClassName()).thenReturn("com.codeus.winter.test.BeanB");
        when(beanDefinitionB.isSingleton()).thenReturn(true);
        when(beanDefinitionB.getDependsOn()).thenReturn(new String[]{"BeanA"});

        when(beanDefinitionC.getBeanClassName()).thenReturn("com.codeus.winter.test.BeanC");
        when(beanDefinitionC.isSingleton()).thenReturn(true);
        when(beanDefinitionC.getDependsOn()).thenReturn(new String[]{"BeanA", "BeanB"});

        when(beanDefinitionD.getBeanClassName()).thenReturn("com.codeus.winter.test.BeanD");
        when(beanDefinitionD.isSingleton()).thenReturn(true);

        when(beanDefinitionE.getBeanClassName()).thenReturn("com.codeus.winter.test.BeanE");
        when(beanDefinitionE.isSingleton()).thenReturn(true);

        when(cycleBeanDefinitionOne.getBeanClassName()).thenReturn("com.codeus.winter.test.CyclicBean.BeanOne");
        when(cycleBeanDefinitionOne.isSingleton()).thenReturn(true);

        when(cycleBeanDefinitionTwo.getBeanClassName()).thenReturn("com.codeus.winter.test.CyclicBean.BeanTwo");
        when(cycleBeanDefinitionTwo.isSingleton()).thenReturn(true);
    }

    @Test
    @DisplayName("Should initialize one bean without dependency")
    void testInitializeSingleBeanAWithoutDependency() {
        DefaultBeanFactory factory = new DefaultBeanFactory(
                Map.of("BeanA", beanDefinitionA)
        );
        factory.initializeBeans();

        BeanA beanA = factory.getBean(BeanA.class);
        assertNotNull(beanA);
    }

    @Test
    @DisplayName("Should initialize two bean with dependencies in order:" +
            "second bean definition depends on first bean definition")
    void testInitializeBeansWithDependencyInOrder() {
        DefaultBeanFactory factory = new DefaultBeanFactory(
                Map.of("BeanA", beanDefinitionA,
                        "BeanB", beanDefinitionB)
        );
        factory.initializeBeans();

        BeanA beanA = factory.getBean(BeanA.class);
        assertNotNull(beanA);
        BeanB beanB = factory.getBean(BeanB.class);
        assertNotNull(beanB);
        assertNotNull(beanB.getBeanA());
        assertEquals(beanA, beanB.getBeanA());
    }

    @Test
    @DisplayName("Should initialize two bean with dependencies in reverse order:" +
            "first bean definition depends on second bean definition")
    void testInitializeBeansWithDependencyInReverseOrder() {
        Map<String, BeanDefinition> beanDefinitionMap = new LinkedHashMap<>();
        beanDefinitionMap.put("BeanB", beanDefinitionB);
        beanDefinitionMap.put("BeanA", beanDefinitionA);

        DefaultBeanFactory factory = new DefaultBeanFactory(beanDefinitionMap);
        factory.initializeBeans();

        BeanA beanA = factory.getBean(BeanA.class);
        assertNotNull(beanA);
        BeanB beanB = factory.getBean(BeanB.class);
        assertNotNull(beanB);
        assertNotNull(beanB.getBeanA());
        assertEquals(beanA, beanB.getBeanA());
    }

    @Test
    @DisplayName("Should initialize three beans with dependencies")
    void testInitializeManyBeansWithDependencyInReverseOrder() {
        Map<String, BeanDefinition> beanDefinitionMap = new LinkedHashMap<>();
        beanDefinitionMap.put("BeanC", beanDefinitionC);
        beanDefinitionMap.put("BeanB", beanDefinitionB);
        beanDefinitionMap.put("BeanA", beanDefinitionA);

        DefaultBeanFactory factory = new DefaultBeanFactory(beanDefinitionMap);
        factory.initializeBeans();

        BeanA beanA = factory.getBean(BeanA.class);
        assertNotNull(beanA);
        BeanB beanB = factory.getBean(BeanB.class);
        assertNotNull(beanB);
        assertNotNull(beanB.getBeanA());
        assertEquals(beanA, beanB.getBeanA());
        BeanC beanC = factory.getBean(BeanC.class);
        assertNotNull(beanC);
        assertNotNull(beanB.getBeanA());
        assertNotNull(beanC.getBeanA());
        assertNotNull(beanC.getBeanB());
        assertEquals(beanA, beanB.getBeanA());
        assertEquals(beanA, beanC.getBeanA());
        assertEquals(beanB, beanC.getBeanB());
    }

    @Test
    @DisplayName("Should throw exception when bean definitions does not contain dependency bean")
    void testThrowExceptionWhenBeanDefinitionsDoesNotContainDependencyBean() {
        Map<String, BeanDefinition> beanDefinitionMap = new LinkedHashMap<>();
        beanDefinitionMap.put("BeanB", beanDefinitionB);
        beanDefinitionMap.put("BeanA", beanDefinitionA);
        when(beanDefinitionB.getDependsOn()).thenReturn(new String[]{"BeanA", "BeanC"});
        DefaultBeanFactory factory = new DefaultBeanFactory(beanDefinitionMap);

        BeanFactoryException beanFactoryException = assertThrows(BeanFactoryException.class, factory::initializeBeans);

        assertEquals("Dependency not found for bean: BeanC", beanFactoryException.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when factory can't resolve dependency")
    void testThrowExceptionWhenBeanFactoryCantResolvePendingDependencies() {
        Map<String, BeanDefinition> beanDefinitionMap = new LinkedHashMap<>();
        beanDefinitionMap.put("BeanOne", cycleBeanDefinitionOne);
        beanDefinitionMap.put("BeanTwo", cycleBeanDefinitionTwo);
        when(cycleBeanDefinitionOne.getDependsOn()).thenReturn(new String[]{"BeanTwo"});
        when(cycleBeanDefinitionTwo.getDependsOn()).thenReturn(new String[]{"BeanOne"});
        DefaultBeanFactory factory = new DefaultBeanFactory(beanDefinitionMap);

        BeanFactoryException beanFactoryException = assertThrows(BeanFactoryException.class, factory::initializeBeans);

        assertEquals("Unresolved dependencies for beans: [BeanOne, BeanTwo]", beanFactoryException.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when bean definition does not contain class name")
    void testThrowExceptionWhenBeanDefinitionsDoesNotContainClassName() {
        Map<String, BeanDefinition> beanDefinitionMap = new LinkedHashMap<>();
        beanDefinitionMap.put("BeanA", beanDefinitionA);
        when(beanDefinitionA.getBeanClassName()).thenReturn(null);
        DefaultBeanFactory factory = new DefaultBeanFactory(beanDefinitionMap);
        BeanFactoryException beanFactoryException = assertThrows(BeanFactoryException.class, factory::initializeBeans);

        assertEquals("Bean class name is not set for bean: BeanA", beanFactoryException.getMessage());
    }

    @Test
    @DisplayName("Should get bean object by bean name")
    void testGetBeanByBeanName() {
        DefaultBeanFactory factory = new DefaultBeanFactory(
                Map.of("BeanA", beanDefinitionA)
        );
        factory.initializeBeans();

        Object actual = factory.getBean("BeanA");
        assertNotNull(actual);
        assertEquals(BeanA.class, actual.getClass());
    }

    @Test
    @DisplayName("Should throw exception when try to get by bean name but factory does not contain bean")
    void testGetBeanThrowExceptionWhenBeanIsNull() {
        DefaultBeanFactory factory = new DefaultBeanFactory(new HashMap<>());
        factory.initializeBeans();

        BeanNotFoundException exception = assertThrows(BeanNotFoundException.class,
                () -> factory.getBean("BeanA"));
        assertEquals("Bean: BeanA not found", exception.getMessage());
    }

    @Test
    @DisplayName("Should get bean object by bean name and typeToken")
    void testGetBeanByBeanNameAndType() {
        DefaultBeanFactory factory = new DefaultBeanFactory(
                Map.of("BeanA", beanDefinitionA)
        );
        factory.initializeBeans();

        BeanA actual = factory.getBean("BeanA", BeanA.class);
        assertNotNull(actual);
    }

    @Test
    @DisplayName("Should throw exception when try to get by bean name and bean type but factory does not contain bean")
    void testGetBeanByNameAndTypeThrowExceptionWhenBeanIsNull() {
        String beanName = "BeanA";
        DefaultBeanFactory factory = new DefaultBeanFactory(
                Map.of(beanName, beanDefinitionA)
        );
        factory.initializeBeans();

        BeanNotFoundException exception = assertThrows(BeanNotFoundException.class,
                () -> factory.getBean("beanName", BeanB.class));

        assertEquals("Bean with a name beanName not found", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when try to get by bean name and bean type but bean has another type")
    void testGetBeanByNameAndTypeThrowExceptionWhenBeanHasDifferentType() {
        String beanName = "BeanA";
        DefaultBeanFactory factory = new DefaultBeanFactory(
                Map.of(beanName, beanDefinitionA)
        );
        factory.initializeBeans();

        BeanNotFoundException exception = assertThrows(BeanNotFoundException.class,
                () -> factory.getBean(beanName, BeanB.class));
        assertEquals(String.format(
                        "Bean with a name %s is not compatible with the type %s",
                        beanName,
                        BeanB.class.getName()),
                exception.getMessage()
        );
    }

    @Test
    @DisplayName("Should get bean object by bean type")
    void testGetBeanByBeanType() {
        DefaultBeanFactory factory = new DefaultBeanFactory(
                Map.of("BeanA", beanDefinitionA)
        );
        factory.initializeBeans();

        Object actual = factory.getBean(BeanA.class);
        assertNotNull(actual);
        assertEquals(BeanA.class, actual.getClass());
    }

    @Test
    @DisplayName("Should throw exception when try to get by bean type but factory does not contain bean")
    void testGetBeanByTypeThrowExceptionWhenBeanIsNull() {
        DefaultBeanFactory factory = new DefaultBeanFactory(new HashMap<>());
        factory.initializeBeans();

        BeanNotFoundException exception = assertThrows(BeanNotFoundException.class, () -> factory.getBean(BeanA.class));
        assertEquals("Bean not found for type: " + BeanA.class.getName(), exception.getMessage());
    }

    @Test
    @DisplayName("Should register singleton bean")
    void testRegisterSingletonBean() {
        Map<String, BeanDefinition> beanDefinitions = spy(Map.class);

        DefaultBeanFactory beanFactory = new DefaultBeanFactory(beanDefinitions);
        String beanName = "BeanA";
        beanFactory.registerBean(beanName, beanDefinitionA, new BeanA());

        verify(beanDefinitions).put(beanName, beanDefinitionA);
        verify(beanDefinitions).put(anyString(), any(BeanDefinition.class));
        BeanA beanA = beanFactory.getBean(beanName, BeanA.class);
        assertNotNull(beanA);
        assertEquals(BeanA.class, beanA.getClass());
    }

    @Test
    @DisplayName("Should create bean")
    void testCreateBean()
            throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        BeanFactory beanFactory = new DefaultBeanFactory(new HashMap<>());

        beanFactory.createBean(BeanA.class);

        BeanA beanA = beanFactory.getBean(BeanA.class);
        assertNotNull(beanA);
        assertEquals(BeanA.class.getName(), beanA.getClass().getName());
    }

    @Test
    @DisplayName("Should create bean should throw exception when bean is not unique")
    void testCreateBeanShouldThrowExceptionWhenBeanIsNotUnique() {
        DefaultBeanFactory beanFactory = new DefaultBeanFactory(Map.of(
                "BeanA", beanDefinitionA
        ));
        beanFactory.initializeBeans();

        NotUniqueBeanDefinitionException exception = assertThrows(NotUniqueBeanDefinitionException.class,
                () -> beanFactory.createBean(BeanA.class)
        );

        assertEquals(String.format("Bean with type '%s' already exists", BeanA.class.getName()),
                exception.getMessage()
        );
    }

    @Test
    @DisplayName("Should apply bean post processors before initialization")
    void testApplyBeanPostProcessorsBeforeInitialization() {
        DefaultBeanFactory beanFactory = new DefaultBeanFactory(Map.of(
                "BeanA", beanDefinitionA
        ));
        BeanPostProcessor beanPostProcessor = spy(BeanPostProcessor.class);
        BeanA beanAfterPostProcessing = new BeanA();
        when(beanPostProcessor.postProcessBeforeInitialization(any(), anyString())).thenReturn(beanAfterPostProcessing);
        beanFactory.addBeanPostProcessor(beanPostProcessor);
        beanFactory.initializeBeans();

        verify(beanPostProcessor).postProcessBeforeInitialization(any(), anyString());
        BeanA beanA = beanFactory.getBean(BeanA.class);
        assertNotNull(beanA);
        assertEquals(beanAfterPostProcessing, beanA);
    }

    @Test
    @DisplayName("Should apply bean post processors after initialization")
    void testApplyBeanPostProcessorsAfterInitialization() {
        DefaultBeanFactory beanFactory = new DefaultBeanFactory(Map.of(
                "BeanA", beanDefinitionA
        ));
        BeanPostProcessor beanPostProcessor = spy(BeanPostProcessor.class);
        BeanA beanAfterPostProcessing = new BeanA();
        when(beanPostProcessor.postProcessAfterInitialization(any(), anyString())).thenReturn(beanAfterPostProcessing);
        beanFactory.addBeanPostProcessor(beanPostProcessor);
        beanFactory.initializeBeans();

        verify(beanPostProcessor).postProcessAfterInitialization(any(), anyString());
        BeanA beanA = beanFactory.getBean(BeanA.class);
        assertNotNull(beanA);
        assertEquals(beanAfterPostProcessing, beanA);
    }

    @Test
    @DisplayName("Should throw exception when beanPostProcessor after initialization returns null")
    void testThrowExceptionWhenBeanPostProcessorAfterInitReturnNull() {
        DefaultBeanFactory beanFactory = new DefaultBeanFactory(Map.of(
                "BeanA", beanDefinitionA
        ));
        BeanPostProcessor beanPostProcessor = spy(BeanPostProcessor.class);
        when(beanPostProcessor.postProcessAfterInitialization(any(), anyString())).thenReturn(null);
        beanFactory.addBeanPostProcessor(beanPostProcessor);

        assertThrows(BeanFactoryException.class, beanFactory::initializeBeans);
    }

    @Test
    @DisplayName("Should throw exception when beanPostProcessor before initialization returns null")
    void testThrowExceptionWhenBeanPostProcessorBeforeInitReturnNull() {
        DefaultBeanFactory beanFactory = new DefaultBeanFactory(Map.of(
                "BeanA", beanDefinitionA
        ));
        BeanPostProcessor beanPostProcessor = spy(BeanPostProcessor.class);
        when(beanPostProcessor.postProcessBeforeInitialization(any(), anyString())).thenReturn(null);
        beanFactory.addBeanPostProcessor(beanPostProcessor);

        assertThrows(BeanFactoryException.class, beanFactory::initializeBeans);
    }

    @Test
    @DisplayName("Should inject list of beans")
    void testInjectionListOfBeans() {
        Map<String, BeanDefinition> beanDefinitionMap = new LinkedHashMap<>();
        beanDefinitionMap.put("BeanA", beanDefinitionA);
        beanDefinitionMap.put("BeanE", beanDefinitionE);
        beanDefinitionMap.put("BeanD", beanDefinitionD);

        DefaultBeanFactory factory = new DefaultBeanFactory(beanDefinitionMap);
        factory.initializeBeans();

        BeanA beanA = factory.getBean(BeanA.class);
        assertNotNull(beanA);
        BeanD beanD = factory.getBean(BeanD.class);
        assertNotNull(beanD);
        BeanE beanE = factory.getBean(BeanE.class);
        assertNotNull(beanE);
        List<Common> list = beanD.getList();
        assertNotNull(beanD.getList());
        assertEquals(beanA, list.getFirst());
        assertEquals(beanE, list.get(1));
    }

    @Test
    @DisplayName("Should inject Set of beans")
    void testInjectionSetOfBeans() {
        Map<String, BeanDefinition> beanDefinitionMap = new LinkedHashMap<>();
        beanDefinitionMap.put("BeanA", beanDefinitionA);
        beanDefinitionMap.put("BeanE", beanDefinitionE);
        beanDefinitionMap.put("BeanD", beanDefinitionD);

        DefaultBeanFactory factory = new DefaultBeanFactory(beanDefinitionMap);
        factory.initializeBeans();

        BeanA beanA = factory.getBean(BeanA.class);
        assertNotNull(beanA);
        BeanD beanD = factory.getBean(BeanD.class);
        assertNotNull(beanD);
        BeanE beanE = factory.getBean(BeanE.class);
        assertNotNull(beanE);
        Set<Common> set = beanD.getSet();
        assertNotNull(set);
        assertTrue(set.contains(beanA));
        assertTrue(set.contains(beanE));
    }

    @Test
    @DisplayName("Should inject Map of beans")
    void testInjectionMapOfBeans() {
        Map<String, BeanDefinition> beanDefinitionMap = new LinkedHashMap<>();
        beanDefinitionMap.put("BeanA", beanDefinitionA);
        beanDefinitionMap.put("BeanE", beanDefinitionE);
        beanDefinitionMap.put("BeanD", beanDefinitionD);

        DefaultBeanFactory factory = new DefaultBeanFactory(beanDefinitionMap);
        factory.initializeBeans();

        BeanA beanA = factory.getBean(BeanA.class);
        assertNotNull(beanA);
        BeanD beanD = factory.getBean(BeanD.class);
        assertNotNull(beanD);
        BeanE beanE = factory.getBean(BeanE.class);
        assertNotNull(beanE);
        Map<String, Common> map = beanD.getMap();
        assertNotNull(map);
        assertEquals(beanA, map.get(beanA.getClass().getName()));
        assertEquals(beanE, map.get(beanE.getClass().getName()));
    }

    @Test
    @DisplayName("Should register bean definition")
    void testShouldRegisterBeanDefinition() {
        HashMap<String, BeanDefinition> beanDefinitionMap = spy(HashMap.class);
        BeanFactory beanFactory = new DefaultBeanFactory(beanDefinitionMap);
        String beanDefinitionName = "BeanDefinition";
        BeanDefinition beanDefinition = new BeanDefinitionImpl();

        beanFactory.registerBeanDefinition(beanDefinitionName, beanDefinition);

        verify(beanDefinitionMap).put(beanDefinitionName, beanDefinition);
    }

    @Test
    @DisplayName("Should throw exception when user register bean definition with same name")
    void testShouldThrowExceptionWhenRegisterBeanDefinitionWithSameName() {
        BeanFactory beanFactory = new DefaultBeanFactory();
        String beanDefinitionName = "BeanDefinition";
        BeanDefinition beanDefinition = new BeanDefinitionImpl();

        beanFactory.registerBeanDefinition(beanDefinitionName, beanDefinition);
        assertThrows(
                BeanFactoryException.class,
                () -> beanFactory.registerBeanDefinition(beanDefinitionName, beanDefinition)
        );

    }
}
