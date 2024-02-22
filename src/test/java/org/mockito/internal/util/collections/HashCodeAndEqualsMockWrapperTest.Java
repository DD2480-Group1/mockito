package org.mockito.internal.util.collections;

import org.junit.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class HashCodeAndEqualsMockWrapperTest {
/**
Test when objects are equal
Expected: true
*/ 
    @Test
    public void testEqualsSameInstance() {
        Object mockInstance = mock(Object.class);
        HashCodeAndEqualsMockWrapper wrapper = new HashCodeAndEqualsMockWrapper(mockInstance);

        assertTrue(wrapper.equals(wrapper), "A wrapper should be equal to itself");
    }
/**
Test when objects are different with same instance
Expected: false
*/ 
    @Test
    public void testEqualsDifferentInstance() {
        Object mockInstance = mock(Object.class);
        String test= "test";
        HashCodeAndEqualsMockWrapper wrapper = new HashCodeAndEqualsMockWrapper(mockInstance);
        HashCodeAndEqualsMockWrapper wrapper2 = new HashCodeAndEqualsMockWrapper(test);

        assertFalse(wrapper.equals(wrapper2), "Different wrappers with the same instance should be different");
    }
/**
Test when objects are same with same instance
Expected: true
*/ 
    @Test
    public void testEqualsSameInstanceDifferentWrappers() {
        Object mockInstance = mock(Object.class);
        HashCodeAndEqualsMockWrapper wrapper = new HashCodeAndEqualsMockWrapper(mockInstance);
        HashCodeAndEqualsMockWrapper wrapper2 = new HashCodeAndEqualsMockWrapper(mockInstance);
        assertTrue(wrapper.equals(wrapper2), "Wrappers of the same instance should be equal");
    }
/**
Test when objects are with different instance
Expected: false
*/ 
    @Test
    public void testEqualsDifferentInstanceDifferentObjects() {
        Object mockInstance = mock(Object.class);
        String test= "test";
        HashCodeAndEqualsMockWrapper wrapper = new HashCodeAndEqualsMockWrapper(mockInstance);
        assertFalse(wrapper.equals(test), "Different objects with different instance should be different");
    }
}