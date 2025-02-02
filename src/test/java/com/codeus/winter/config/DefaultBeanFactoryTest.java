package com.codeus.winter.config;

import com.codeus.winter.config.impl.BeanDefinitionImpl;
import com.codeus.winter.exception.BeanCurrentlyInCreationException;
import com.codeus.winter.exception.BeanFactoryException;
import com.codeus.winter.exception.BeanNotFoundException;
import com.codeus.winter.exception.NotUniqueBeanDefinitionException;
import com.codeus.winter.test.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DefaultBeanFactoryTest {
    private final BeanDefinition beanDefinitionA = mock(BeanDefinition.class);
    private final BeanDefinition beanDefinitionB = mock(BeanDefinition.class);
    private final BeanDefinition beanDefinitionC = mock(BeanDefinition.class);
    private final BeanDefinition beanDefinitionD = mock(BeanDefinition.class);
    private final BeanDefinition beanDefinitionE = mock(BeanDefinition.class);

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
    }

    @Test
    @DisplayName("Should initialize one bean without dependency using default constructor")
    void testInitializeBeanWithoutDependencyUsingDefaultConstructor() {
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
        Map<String, BeanDefinition> beanDefinitionMap = new LinkedHashMap<>();
        beanDefinitionMap.put("BeanA", beanDefinitionA);
        beanDefinitionMap.put("BeanB", beanDefinitionB);

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
        assertNotNull(beanC.getBeanA());
        assertNotNull(beanC.getBeanB());
        assertEquals(beanA, beanC.getBeanA());
        assertEquals(beanB, beanC.getBeanB());
    }

    @Test
    @DisplayName("Should initialize bean with dependencies using single declared non-annotated constructor")
    void testInitializeBeanWithDependenciesUsingSingleNotAnnotatedConstructor() {
        Map<String, BeanDefinition> beanDefinitionMap = new HashMap<>();
        beanDefinitionMap.put("BeanA", beanDefinitionA);
        beanDefinitionMap.put("BeanB", beanDefinitionB);

        DefaultBeanFactory factory = new DefaultBeanFactory(beanDefinitionMap);
        factory.initializeBeans();
        BeanA beanA = factory.getBean(BeanA.class);
        BeanB beanB = factory.getBean(BeanB.class);

        assertNotNull(beanA);
        assertNotNull(beanB);
        assertNotNull(beanB.getBeanA());
        assertEquals(beanA, beanB.getBeanA());
    }

    @Test
    @DisplayName("Should initialize bean with dependencies using declared non-annotated constructor and ignore default one")
    void testInitializeBeanWithDependenciesUsingDeclaredNonAnnotatedConstructor() {
        DefaultBeanFactory factory = new DefaultBeanFactory();
        factory.registerBeanDefinition("BeanA", beanDefinitionA);
        factory.registerBeanDefinition("BeanWithNonAnnotatedAndDefaultConstructors", singletonBeanDefinitionMock(BeanWithNonAnnotatedAndDefaultConstructors.class));

        factory.initializeBeans();
        BeanA beanA = factory.getBean(BeanA.class);
        BeanWithNonAnnotatedAndDefaultConstructors beanB = factory.getBean(BeanWithNonAnnotatedAndDefaultConstructors.class);

        assertNotNull(beanA);
        assertNotNull(beanB);
        assertNotNull(beanB.getBeanA());
        assertEquals(beanA, beanB.getBeanA());
    }

    @Test
    @DisplayName("Should initialize bean with dependencies using declared annotated constructor")
    void testInitializeBeanWithDependenciesUsingDeclaredAnnotatedConstructor() {
        DefaultBeanFactory factory = new DefaultBeanFactory();
        factory.registerBeanDefinition("BeanWithAnnotatedConstructor", singletonBeanDefinitionMock(BeanWithAnnotatedConstructor.class));
        factory.registerBeanDefinition("BeanA", beanDefinitionA);

        factory.initializeBeans();
        BeanA beanA = factory.getBean(BeanA.class);
        BeanWithAnnotatedConstructor beanWrapper = factory.getBean(BeanWithAnnotatedConstructor.class);

        assertNotNull(beanA);
        assertNotNull(beanWrapper);
        assertNotNull(beanWrapper.getBeanA());
        assertEquals(beanA, beanWrapper.getBeanA());
    }

    @Test
    @DisplayName("Should initialize bean using default constructor ignoring declared non-annotated constructors")
    void testInitializeBeanWithMultipleConstructorsUsingDefaultConstructor() {
        DefaultBeanFactory factory = new DefaultBeanFactory();
        factory.registerBeanDefinition("BeanWithMultipleNonAnnotatedAndDefaultConstructors",
                singletonBeanDefinitionMock(BeanWithMultipleNonAnnotatedAndDefaultConstructors.class));

        factory.initializeBeans();
        BeanWithMultipleNonAnnotatedAndDefaultConstructors bean = factory.getBean(BeanWithMultipleNonAnnotatedAndDefaultConstructors.class);

        assertNotNull(bean);
        assertNotNull(bean.getBean());
        assertEquals(bean.getBean().getClass(), BeanWithMultipleNonAnnotatedAndDefaultConstructors.DefaultBean.class);
    }

    @Test
    @DisplayName("Should fail to initialize bean with multiple annotated constructors")
    void testFailInitializeBeanWithMultipleAutowiringConstructors() {
        DefaultBeanFactory factory = new DefaultBeanFactory();
        factory.registerBeanDefinition("BeanWithMultipleAutowiringConstructors",
                singletonBeanDefinitionMock(BeanWithMultipleAutowiringConstructors.class));

        assertThrows(BeanFactoryException.class, factory::initializeBeans,
                "Cannot create bean for class com.codeus.winter.test.BeanWithMultipleAutowiringConstructors, " +
                        "it has multiple constructors marked with autowire annotation");
    }

    @Test
    @DisplayName("Should fail to initialize bean with single private default constructor")
    void testFailInitializeBeanWithSinglePrivateDefaultConstructor() {
        DefaultBeanFactory factory = new DefaultBeanFactory();
        factory.registerBeanDefinition("BeanWithPrivateConstructor",
                singletonBeanDefinitionMock(BeanWithPrivateConstructor.class));

        assertThrows(BeanFactoryException.class, factory::initializeBeans,
                "Class has no public default constructor: com.codeus.winter.test.BeanWithPrivateConstructor");
    }

    @Test
    @DisplayName("Should fail to initialize bean with multiple non-annotated constructors and without default one")
    void testFailInitializeBeanWithMultipleNonAnnotatedConstructors() {
        DefaultBeanFactory factory = new DefaultBeanFactory();
        factory.registerBeanDefinition("BeanWithNonAnnotatedConstructors",
                singletonBeanDefinitionMock(BeanWithNonAnnotatedConstructors.class));

        assertThrows(BeanFactoryException.class, factory::initializeBeans,
                "Class has no public default constructor: com.codeus.winter.test.BeanWithNonAnnotatedConstructors");
    }

    @Test
    @DisplayName("Should autowire Bean dependency by interface type")
    void testInitializeBeansDependenciesByInterface() {
        BeanDefinition beanDefinition = mock(BeanDefinition.class);
        when(beanDefinition.getBeanClassName()).thenReturn("com.codeus.winter.test.BeanWithDependencyByInterface");
        when(beanDefinition.isSingleton()).thenReturn(true);

        Map<String, BeanDefinition> beanDefinitionMap = new LinkedHashMap<>();
        beanDefinitionMap.put("BeanWithDependencyByInterface", beanDefinition);
        beanDefinitionMap.put("BeanA", beanDefinitionA);
        DefaultBeanFactory factory = new DefaultBeanFactory(beanDefinitionMap);

        factory.initializeBeans();
        BeanA beanA = factory.getBean(BeanA.class);
        BeanWithDependencyByInterface beanWrapper = factory.getBean(BeanWithDependencyByInterface.class);

        assertNotNull(beanA);
        assertNotNull(beanWrapper);
        assertNotNull(beanWrapper.getWrappeeBean());
        assertEquals(beanA, beanWrapper.getWrappeeBean());
    }

    @Test
    @DisplayName("Should fail autowire Bean dependency by interface type when no candidates available")
    void testFailInitializeBeanWithDependencyByInterfaceIfNoCandidatesAvailable() {
        BeanDefinition beanDefinition = mock(BeanDefinition.class);
        when(beanDefinition.getBeanClassName()).thenReturn("com.codeus.winter.test.BeanWithDependencyByInterface");
        when(beanDefinition.isSingleton()).thenReturn(true);

        Map<String, BeanDefinition> beanDefinitionMap = new LinkedHashMap<>();
        beanDefinitionMap.put("BeanWithDependencyByInterface", beanDefinition);
        DefaultBeanFactory factory = new DefaultBeanFactory(beanDefinitionMap);

        assertThrows(BeanNotFoundException.class, factory::initializeBeans,
                "Cannot resolve bean for type='%s', no bean definitions available".formatted(Common.class.getName()));
    }

    @Test
    @DisplayName("Should fail initializing Beans with cyclic dependencies")
    void testThrowExceptionWhenBeanFactoryCantResolveCyclicDependencies() {
        BeanDefinition beanDefinitionOne = mock(BeanDefinition.class);
        when(beanDefinitionOne.getBeanClassName()).thenReturn("com.codeus.winter.test.BeansWithCyclicDependency$BeanOne");
        when(beanDefinitionOne.isSingleton()).thenReturn(true);

        BeanDefinition beanDefinitionTwo = mock(BeanDefinition.class);
        when(beanDefinitionTwo.getBeanClassName()).thenReturn("com.codeus.winter.test.BeansWithCyclicDependency$BeanTwo");
        when(beanDefinitionTwo.isSingleton()).thenReturn(true);

        Map<String, BeanDefinition> beanDefinitionMap = new LinkedHashMap<>();
        beanDefinitionMap.put("BeanOne", beanDefinitionOne);
        beanDefinitionMap.put("BeanTwo", beanDefinitionTwo);
        DefaultBeanFactory factory = new DefaultBeanFactory(beanDefinitionMap);

        assertThrows(BeanCurrentlyInCreationException.class, factory::initializeBeans);
    }

    @Test
    @DisplayName("Should fail initializing Bean with self injection")
    void testFailOnSelfInjection() {
        BeanDefinition beanDefinition = mock(BeanDefinition.class);
        when(beanDefinition.getBeanClassName()).thenReturn("com.codeus.winter.test.BeanWithSelfInjection");
        when(beanDefinition.isSingleton()).thenReturn(true);

        Map<String, BeanDefinition> beanDefinitionMap = new LinkedHashMap<>();
        beanDefinitionMap.put("BeanWithSelfInjection", beanDefinition);
        DefaultBeanFactory factory = new DefaultBeanFactory(beanDefinitionMap);

        assertThrows(BeanCurrentlyInCreationException.class, factory::initializeBeans);
    }

    @Test
    @DisplayName("Should fail initializing Bean if multiple non-qualified candidates available for it dependency")
    void testFailInitializeBeanWithDependencyIfMultipleNonQualifiedCandidatesAvailable() {
        BeanDefinition beanDefinition = mock(BeanDefinition.class);
        when(beanDefinition.getBeanClassName()).thenReturn("com.codeus.winter.test.BeanWithDependencyByInterface");
        when(beanDefinition.isSingleton()).thenReturn(true);

        Map<String, BeanDefinition> beanDefinitionMap = new LinkedHashMap<>();
        beanDefinitionMap.put("BeanWithDependencyByInterface", beanDefinition);
        beanDefinitionMap.put("BeanA", beanDefinitionA);
        beanDefinitionMap.put("BeanE", beanDefinitionE);
        DefaultBeanFactory factory = new DefaultBeanFactory(beanDefinitionMap);

        NotUniqueBeanDefinitionException exception = assertThrows(NotUniqueBeanDefinitionException.class, factory::initializeBeans);
        assertEquals("Cannot resolve bean for type='com.codeus.winter.test.Common', " +
                "multiple beans are available: com.codeus.winter.test.BeanA, com.codeus.winter.test.BeanE", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when bean definitions does not contain dependency bean")
    void testThrowExceptionWhenBeanDefinitionsDoesNotContainDependencyBean() {
        Map<String, BeanDefinition> beanDefinitionMap = new LinkedHashMap<>();
        beanDefinitionMap.put("BeanB", beanDefinitionB);
        DefaultBeanFactory factory = new DefaultBeanFactory(beanDefinitionMap);

        assertThrows(BeanNotFoundException.class, factory::initializeBeans,
                "Cannot resolve bean for type='%s', no bean definitions available".formatted(BeanA.class.getName()));
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
        assertEquals("Bean for name='BeanA' not found", exception.getMessage());
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
    void testGetBeanByNameAndTypeThrowExceptionWhenFactoryDoesNotContainBeanName() {
        DefaultBeanFactory factory = new DefaultBeanFactory(Map.of("BeanA", beanDefinitionA));
        factory.initializeBeans();

        BeanNotFoundException exception = assertThrows(BeanNotFoundException.class,
                () -> factory.getBean("BeanB", BeanA.class));

        assertEquals("Bean for name='BeanB' not found", exception.getMessage());

    }

    @Test
    @DisplayName("Should throw exception when try to get by bean name and bean type but bean has another type")
    void testGetBeanByNameAndTypeThrowExceptionWhenBeanHasDifferentType() {
        String beanName = "BeanA";
        DefaultBeanFactory factory = new DefaultBeanFactory(Map.of(beanName, beanDefinitionA));
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
        assertEquals("Bean for type=%s not found".formatted(BeanA.class.getName()), exception.getMessage());
    }

    @Test
    @DisplayName("Should register singleton bean")
    void testRegisterSingletonBean() {
        Map<String, BeanDefinition> beanDefinitionsSpy = spy(new HashMap<>());

        DefaultBeanFactory beanFactory = new DefaultBeanFactory(beanDefinitionsSpy);
        String beanName = "BeanA";
        beanFactory.registerBean(beanName, beanDefinitionA, new BeanA());

        verify(beanDefinitionsSpy).put(beanName, beanDefinitionA);
        verify(beanDefinitionsSpy).put(anyString(), any(BeanDefinition.class));
        BeanA beanA = beanFactory.getBean(beanName, BeanA.class);
        assertNotNull(beanA);
        assertEquals(BeanA.class, beanA.getClass());
    }

    @Test
    @DisplayName("Should create a prototype-scoped bean for given not-annotated class")
    void testCreateBean() {
        BeanFactory beanFactory = new DefaultBeanFactory();

        BeanA prototypeBean1 = beanFactory.createBean(BeanA.class);
        BeanA prototypeBean2 = beanFactory.createBean(BeanA.class);

        assertNotSame(prototypeBean1, prototypeBean2);
    }

    @Test
    @DisplayName("Should create a prototype-scoped bean with singleton dependencies for given not-annotated class")
    void testCreateBeanWithSingletonDependencies() {
        BeanFactory beanFactory = new DefaultBeanFactory();
        beanFactory.registerBeanDefinition("BeanA", beanDefinitionA);

        BeanB prototypeBean1 = beanFactory.createBean(BeanB.class);
        BeanB prototypeBean2 = beanFactory.createBean(BeanB.class);

        assertNotSame(prototypeBean1, prototypeBean2);
        assertSame(prototypeBean1.getBeanA(), prototypeBean2.getBeanA());
    }

    @Test
    @DisplayName("Should create a prototype-scoped bean with prototype dependencies for given not-annotated class")
    void testCreateBeanWithPrototypeDependencies() {
        BeanFactory beanFactory = new DefaultBeanFactory();
        beanFactory.registerBeanDefinition("BeanA", prototypeBeanDefinitionMock(BeanA.class));

        BeanB prototypeBean1 = beanFactory.createBean(BeanB.class);
        BeanB prototypeBean2 = beanFactory.createBean(BeanB.class);

        assertNotSame(prototypeBean1, prototypeBean2);
        assertNotSame(prototypeBean1.getBeanA(), prototypeBean2.getBeanA());
    }

    @Test
    @DisplayName("Should fail creating a prototype-scoped bean if cannot resolve one of the dependencies")
    void testCreateBeanFailWhenCannotResolveDependencies() {
        BeanFactory beanFactory = new DefaultBeanFactory();

        assertThrows(BeanNotFoundException.class, () -> beanFactory.createBean(BeanB.class),
                "Cannot resolve bean for type='%s', no bean definitions available".formatted(BeanA.class.getName()));
    }

    @Test
    @DisplayName("Should not store a prototype-scoped bean for given not-annotated class")
    void testCreateBeanShouldNotStorePrototypeBean() {
        BeanFactory beanFactory = new DefaultBeanFactory();

        BeanA prototypeBean = beanFactory.createBean(BeanA.class);

        assertNotNull(prototypeBean);
        assertThrows(BeanNotFoundException.class, () -> beanFactory.getBean(BeanA.class),
                "Bean for type=%s not found".formatted(BeanA.class.getName()));
    }

    @Test
    @DisplayName("Should apply bean post processors before initialization")
    void testApplyBeanPostProcessorsBeforeInitialization() {
        DefaultBeanFactory beanFactory = new DefaultBeanFactory(Map.of("BeanA", beanDefinitionA));
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
    @DisplayName("Should fail collection injection if factory doesn't have any beans for dependency class")
    void testFailInjectionListOfBeansWhenNoCandidatesFound() {
        DefaultBeanFactory factory = new DefaultBeanFactory();
        factory.registerBeanDefinition("BeanD", beanDefinitionD);

        assertThrows(BeanNotFoundException.class, factory::initializeBeans,
                "Cannot resolve bean for type='%s', no bean definitions available".formatted(Common.class.getName()));
    }

    @Test
    @DisplayName("Should return null for bean definition with the unsupported scope")
    void testFailInitializeBeanWithPrototypeScope() {
        BeanDefinition beanDefinition = mock(BeanDefinition.class);
        when(beanDefinition.getBeanClassName()).thenReturn(BeanA.class.getName());
        when(beanDefinition.isSingleton()).thenReturn(false);
        when(beanDefinition.isPrototype()).thenReturn(false);
        when(beanDefinition.getScope()).thenReturn("MY_CUSTOM_SCOPE");

        DefaultBeanFactory factory = new DefaultBeanFactory();
        factory.registerBeanDefinition("BeanA", beanDefinition);

        assertThrows(IllegalArgumentException.class, factory::initializeBeans,
                "DefaultBeanFactory cannot create bean (name='BeanA') with the 'MY_CUSTOM_SCOPE' scope.");

    }

    @Test
    @DisplayName("Should register bean definition")
    void testShouldRegisterBeanDefinition() {
        Map<String, BeanDefinition> beanDefinitionMap = spy(new HashMap<>());
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

    static BeanDefinition singletonBeanDefinitionMock(Class<?> beanClass) {
        BeanDefinition beanDefinition = mock(BeanDefinition.class);
        when(beanDefinition.getBeanClassName()).thenReturn(beanClass.getName());
        when(beanDefinition.isSingleton()).thenReturn(true);

        return beanDefinition;
    }

    @SuppressWarnings("SameParameterValue")
    static BeanDefinition prototypeBeanDefinitionMock(Class<?> beanClass) {
        BeanDefinition beanDefinition = mock(BeanDefinition.class);
        when(beanDefinition.getBeanClassName()).thenReturn(beanClass.getName());
        when(beanDefinition.isPrototype()).thenReturn(true);

        return beanDefinition;
    }
}
