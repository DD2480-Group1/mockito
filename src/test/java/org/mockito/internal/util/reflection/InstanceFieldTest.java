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

    @Before
    public void createInstanceFieldToTestWith() {
        Field field = String.class.getDeclaredFields()[0];
        Object instance = "test";

        instanceField = new InstanceField(field, instance);
    }

    @Test
    public void shouldEqualTrueWhenSameObject() {
        assertTrue(instanceField.equals(instanceField));
    }

    @Test
    public void shouldEqualFalseWhenInstanceNull() {
        assertFalse(instanceField.equals(null));
    }

    @Test
    public void shouldEqualFalseWhenDifferentClass() {
        // given
        int differentClass = 1;

        // when & then
        assertFalse(instanceField.equals(differentClass));
    }

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
