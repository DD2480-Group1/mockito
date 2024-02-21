package org.mockito.internal.util.reflection;
/*
 * Copyright (c) 2007 Mockito contributors
 * This program is made available under the terms of the MIT License.
 */

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;

import org.junit.Before;
import org.junit.Test;

public class InstanceFieldTest {

    InstanceField instanceField;

    /**
     * Create a instanceField parameter that can be reused for the unit-tests
     */
    @Before
    public void createInstanceFieldToTestWith() {
        Field field = String.class.getDeclaredFields()[0];
        Object instance = "test";

        instanceField = new InstanceField(field, instance);
    }

    /**
     * Test covers case when object compares against itself.
     * expected: return true when equal itself
     */
    @Test
    public void shouldEqualTrueWhenSameObject() {
        assertTrue(instanceField.equals(instanceField));
    }

    /**
     * Test cover case when object that is not null tries to compare against null.
     * expected: return false
     */
    @Test
    public void shouldEqualFalseWhenInstanceNull() {
        assertFalse(instanceField.equals(null));
    }

    /**
     * Test covers case when invoking object compares to a comparison object that is a different class.
     * instanceField is a String, and is compared against an Integer.
     * expected: String and Integer is not the same class, thus should return false
     */
    @Test
    public void shouldEqualFalseWhenDifferentClass() {
        // given
        int differentClass = 1;

        // when & then
        assertFalse(instanceField.equals(differentClass));
    }

    /**
     * Tests whether equals can identify an instanceField being equal if the field and instance are the same.
     * expected: should return true
     */
    @Test
    public void shouldEqualTrueWhenSameFieldAndInstance() {
        // given
        Field field = String.class.getDeclaredFields()[0];
        Object instance = "test";

        // when
        InstanceField sameInstanceField = new InstanceField(field, instance);

        // then
        assertTrue(instanceField.equals(sameInstanceField));
    }

    /**
     * Tests if equals method can reject an instanceField where the field is different but the instance is the same.
     * In this case, the instanceField field is a String and is compared to a Integer field.
     * expected: fields are different, so should return false
     */
    @Test
    public void shouldEqualFalseWhenDifferentField() {
        // given
        Field differentField = Integer.class.getDeclaredFields()[0];
        Object instance = "test";

        // when
        InstanceField differentInstanceField = new InstanceField(differentField, instance);

        // then
        assertFalse(instanceField.equals(differentInstanceField));
    }

    /**
     * Tests whether equals method can reject an instanceField where the instance is different but the fields are same.
     * In this case, the InstanceFields have the same field (String) but different instances.
     * expected: instances are different, so should return false
     */
    @Test
    public void shouldEqualFalseWhenDifferentInstance() {
        // given
        Field field = String.class.getDeclaredFields()[0];
        Object differentInstance = "different test";

        // when
        InstanceField differentInstanceField = new InstanceField(field, differentInstance);

        // then
        assertFalse(instanceField.equals(differentInstanceField));
    }
}
