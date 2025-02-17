package com.codeus.winter.config.impl;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.codeus.winter.config.BeanDefinition;
import com.codeus.winter.exception.BeanDefinitionStoreException;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class BeanDefinitionRegistryImplTest {
    private BeanDefinitionRegistryImpl registry;

    @BeforeEach
    void setUp() {
        registry = new BeanDefinitionRegistryImpl();
    }

    @Test
    @DisplayName("Should register a BeanDefinition successfully")
    void testRegisterBeanDefinition() {
        BeanDefinition mockBeanDefinition = mock(BeanDefinition.class);
        registry.registerBeanDefinition("testBean", mockBeanDefinition);
        assertTrue(registry.containsBeanDefinition("testBean"),
                "Registry should contain 'testBean'");
    }

    @Test
    @DisplayName("Should create a bean using a mocked BeanDefinition")
    void testCreateBeanWithMockBeanDefinition() {
        BeanDefinition mockBeanDefinition = mock(BeanDefinition.class);
        when(mockBeanDefinition.getBeanClassName()).thenReturn("test.MyTestBean");
        when(mockBeanDefinition.isSingleton()).thenReturn(true);

        registry.registerBeanDefinition("testBean", mockBeanDefinition);

        Object bean = registry.getBeanDefinition("testBean");
        assertNotNull(bean, "Bean should not be null");
    }

    @Test
    @DisplayName("Should throw exception if a BeanDefinition with the same name already exists")
    void testRegisterBeanDefinitionThrowsExceptionOnDuplicate() {
        BeanDefinition mockBeanDefinition = mock(BeanDefinition.class);
        registry.registerBeanDefinition("testBean", mockBeanDefinition);
        BeanDefinition anotherMockBeanDefinition = mock(BeanDefinition.class);
        assertThrows(BeanDefinitionStoreException.class,
                () -> registry.registerBeanDefinition("testBean", anotherMockBeanDefinition),
                "Should throw exception if bean name already exists");
    }

    @Test
    @DisplayName("Should register and retrieve a BeanDefinition successfully")
    void testRegisterAndRetrieveBeanDefinition() {
        BeanDefinition mockBeanDefinition = mock(BeanDefinition.class);
        when(mockBeanDefinition.getBeanClassName()).thenReturn("test.MyTestBean");

        registry.registerBeanDefinition("testBean", mockBeanDefinition);

        BeanDefinition retrievedDefinition = registry.getBeanDefinition("testBean");

        assertNotNull(retrievedDefinition, "BeanDefinition should not be null");
        assertEquals("test.MyTestBean", retrievedDefinition.getBeanClassName());
    }


    @Test
    @DisplayName("Should return null if a BeanDefinition is not found")
    void testGetBeanDefinitionReturnsNullIfNotFound() {
        assertNull(registry.getBeanDefinition("nonExistentBean"),
                "Should return null for non-existent bean");
    }

    @Test
    @DisplayName("Should check if a BeanDefinition exists in the registry")
    void testContainsBeanDefinition() {
        BeanDefinition mockBeanDefinition = mock(BeanDefinition.class);

        registry.registerBeanDefinition("testBean", mockBeanDefinition);

        assertTrue(registry.containsBeanDefinition("testBean"),
                "Registry should contain 'testBean'");
        assertFalse(registry.containsBeanDefinition("nonExistentBean"),
                "Registry should not contain 'nonExistentBean'");
    }



    @Test
    @DisplayName("Should remove a BeanDefinition from the registry")
    void testRemoveBeanDefinition() {
        BeanDefinition mockBeanDefinition = mock(BeanDefinition.class);

        registry.registerBeanDefinition("testBean", mockBeanDefinition);
        registry.removeBeanDefinition("testBean");

        assertFalse(registry.containsBeanDefinition("testBean"),
                "Registry should not contain 'testBean' after removal");
    }


    @Test
    @DisplayName("Should throw exception when removing a non-existent BeanDefinition")
    void testRemoveBeanDefinitionThrowsExceptionIfNotFound() {
        assertThrows(BeanDefinitionStoreException.class,
                () -> registry.removeBeanDefinition("nonExistentBean"),
                "Should throw exception if trying to remove non-existent bean");
    }

    @Test
    @DisplayName("Should retrieve all BeanDefinition names")
    void testGetBeanDefinitionNames() {
        BeanDefinition mockBeanDefinition1 = mock(BeanDefinition.class);
        BeanDefinition mockBeanDefinition2 = mock(BeanDefinition.class);

        registry.registerBeanDefinition("testBean1", mockBeanDefinition1);
        registry.registerBeanDefinition("testBean2", mockBeanDefinition2);

        String[] beanNames = registry.getBeanDefinitionNames();
        assertArrayEquals(new String[]{"testBean1", "testBean2"}, beanNames,
                "Should return all registered bean names");
    }

    @Test
    @DisplayName("Should return the correct count of BeanDefinitions")
    void testGetBeanDefinitionCount() {
        assertEquals(0, registry.getBeanDefinitionCount(),
                "Initially, registry should contain 0 BeanDefinitions");

        BeanDefinition mockBeanDefinition1 = mock(BeanDefinition.class);
        BeanDefinition mockBeanDefinition2 = mock(BeanDefinition.class);

        registry.registerBeanDefinition("testBean1", mockBeanDefinition1);
        registry.registerBeanDefinition("testBean2", mockBeanDefinition2);

        assertEquals(2, registry.getBeanDefinitionCount(),
                "Registry should contain 2 BeanDefinitions");
    }

    @Test
    @DisplayName("Should get a BeanDefinitions map successfully")
    void testGetBeanDefinitionsMap() {
        BeanDefinition mockBeanDefinitionFirst = mock(BeanDefinition.class);
        BeanDefinition mockBeanDefinitionSecond = mock(BeanDefinition.class);
        String firstBeanName = "testBeanFirst";
        String secondBeanName = "testBeanSecond";
        registry.registerBeanDefinition(firstBeanName, mockBeanDefinitionFirst);
        registry.registerBeanDefinition(secondBeanName, mockBeanDefinitionSecond);

        Map<String, BeanDefinition> registeredBeanDefinitions = registry.getRegisteredBeanDefinitions();

        assertNotNull(registeredBeanDefinitions.get(firstBeanName));
        assertNotNull(registeredBeanDefinitions.get(secondBeanName));
    }

}
