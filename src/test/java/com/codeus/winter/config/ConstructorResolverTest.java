package com.codeus.winter.config;

import com.codeus.winter.test.BeanA;
import com.codeus.winter.test.BeanB;
import com.codeus.winter.test.BeanC;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

//TODO: add nested classes for each method tests
class ConstructorResolverTest {

    private static BeanA beanAMock;
    private static BeanB beanBMock;
    private static AutowireCapableBeanFactory beanFactoryMock;

    private final ConstructorResolver constructorResolver = new ConstructorResolver(beanFactoryMock);

    @BeforeAll
    static void setupBeanFactoryMock() {
        beanAMock = mock(BeanA.class);
        beanBMock = mock(BeanB.class);
        beanFactoryMock = mock(AutowireCapableBeanFactory.class);

        doReturn(beanAMock).when(beanFactoryMock).resolveDependency(argThat(desc -> desc.getDependencyClass() == BeanA.class));
        doReturn(beanBMock).when(beanFactoryMock).resolveDependency(argThat(desc -> desc.getDependencyClass() == BeanB.class));
    }

    @Test
    @DisplayName("should autowire constructor with multiple arguments")
    void testAutowiringConstructorWithMultipleArguments() throws NoSuchMethodException {
        Constructor<BeanC> constructor = BeanC.class.getConstructor(BeanA.class, BeanB.class);

        Object beanC = constructorResolver.autowireConstructor(constructor);

        assertEquals(beanC.getClass(), BeanC.class);
    }

    @Test
    @DisplayName("can safely autowire constructor with no arguments")
    void testAutowiringConstructorWithNoArguments() throws NoSuchMethodException {
        Constructor<BeanA> constructor = BeanA.class.getConstructor();

        Object beanA = constructorResolver.autowireConstructor(constructor);

        assertEquals(beanA.getClass(), BeanA.class);
    }

    @Test
    @DisplayName("should make an argument array for a constructor with multiple arguments")
    void testMakingArgumentArrayForConstructorWithMultipleArguments() throws NoSuchMethodException {
        Constructor<BeanC> constructor = BeanC.class.getConstructor(BeanA.class, BeanB.class);

        Object[] argumentArray = constructorResolver.makeArgumentArray(constructor);

        assertArrayEquals(argumentArray, new Object[]{beanAMock, beanBMock});
    }

    @Test
    @DisplayName("can safely process constructor with no arguments")
    void testMakingArgumentArrayForConstructorWithNoArguments() throws NoSuchMethodException {
        Constructor<BeanA> constructor = BeanA.class.getConstructor();

        Object[] argumentArray = constructorResolver.makeArgumentArray(constructor);

        assertArrayEquals(argumentArray, new Object[]{});
    }

}