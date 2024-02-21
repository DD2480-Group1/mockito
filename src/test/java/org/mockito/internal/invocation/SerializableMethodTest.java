/*
 * Copyright (c) 2007 Mockito contributors
 * This program is made available under the terms of the MIT License.
 */
package org.mockito.internal.invocation;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doReturn;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;

import org.junit.Before;
import org.junit.Test;
import org.mockitoutil.TestBase;

public class SerializableMethodTest extends TestBase {

    private MockitoMethod method;
    private Method toStringMethod;
    private Class<?>[] args;

    @Before
    public void createMethodToTestWith() throws SecurityException, NoSuchMethodException {
        args = new Class<?>[0];
        toStringMethod = this.getClass().getMethod("toString", args);
        method = new SerializableMethod(toStringMethod);
    }

    @Test
    public void shouldBeSerializable() throws Exception {
        ByteArrayOutputStream serialized = new ByteArrayOutputStream();
        new ObjectOutputStream(serialized).writeObject(method);
    }

    @Test
    public void shouldBeAbleToRetrieveMethodExceptionTypes() throws Exception {
        assertArrayEquals(toStringMethod.getExceptionTypes(), method.getExceptionTypes());
    }

    @Test
    public void shouldBeAbleToRetrieveMethodName() throws Exception {
        assertEquals(toStringMethod.getName(), method.getName());
    }

    @Test
    public void shouldBeAbleToCheckIsArgVargs() throws Exception {
        assertEquals(toStringMethod.isVarArgs(), method.isVarArgs());
    }

    @Test
    public void shouldBeAbleToGetParameterTypes() throws Exception {
        assertArrayEquals(toStringMethod.getParameterTypes(), method.getParameterTypes());
    }

    @Test
    public void shouldBeAbleToGetReturnType() throws Exception {
        assertEquals(toStringMethod.getReturnType(), method.getReturnType());
    }

    @Test
    public void shouldBeEqualForTwoInstances() throws Exception {
        assertTrue(new SerializableMethod(toStringMethod).equals(method));
    }

    @Test
    public void shouldNotBeEqualForSameMethodFromTwoDifferentClasses() throws Exception {
        Method testBaseToStringMethod = String.class.getMethod("toString", args);
        assertFalse(new SerializableMethod(testBaseToStringMethod).equals(method));
    }

    // TODO: add tests for generated equals() method
    @Test
    public void shouldNotBeEqualForNull() throws Exception {
        assertFalse(method.equals(null));
    }

    @Test
    public void shouldNotBeEqualForDifferentMethodNames() throws Exception {
        Method toStringMethod = String.class.getMethod("toString", args);
        SerializableMethod methodToString = new SerializableMethod(toStringMethod);

        Method hashCodeMethod = String.class.getMethod("hashCode", args);
        SerializableMethod methodHashCode = new SerializableMethod(hashCodeMethod);

        assertFalse(methodToString.equals(methodHashCode));
    }

    @Test
    public void shouldNotBeEqualForDifferentClassTypes() throws Exception {
        assertFalse(method.equals(new Object()));
    }

    @Test
    public void shouldNotBeEqualForDifferentParameterTypes() throws Exception {
        Method appendWithInt = StringBuilder.class.getMethod("append", int.class);
        SerializableMethod method1 = new SerializableMethod(appendWithInt);

        Method appendWithString = StringBuilder.class.getMethod("append", String.class);
        SerializableMethod method2 = new SerializableMethod(appendWithString);

        assertFalse(method1.equals(method2));
    }

    @Test
    public void shouldNotBeEqualWhenDeclaringClassIsNull() throws Exception {
        // given
        // utilizing Mockito's mock class for testing, dogfooding
        Method mockMethod = mock(Method.class);
        when(mockMethod.getDeclaringClass()).thenReturn(null);
        when(mockMethod.getParameterTypes()).thenReturn(method.getParameterTypes());

        // when
        SerializableMethod mockSerializableMethod = new SerializableMethod(mockMethod);

        // then
        assertFalse(mockSerializableMethod.equals(method));
    }

    @Test
    public void shouldNotBeEqualWhenMethodNameIsNull() throws Exception {
        // given
        Method mockMethod = mock(Method.class);
        doReturn(toStringMethod.getDeclaringClass()).when(mockMethod).getDeclaringClass();
        when(mockMethod.getParameterTypes()).thenReturn(method.getParameterTypes());
        when(mockMethod.getName()).thenReturn(null);

        // when
        SerializableMethod mockSerializableMethod = new SerializableMethod(mockMethod);

        // then
        assertFalse(mockSerializableMethod.equals(method));
    }

    @Test
    public void shouldNotBeEqualWhenReturnTypeIsNull() throws Exception {
        // given
        Method mockMethod = mock(Method.class);
        doReturn(toStringMethod.getDeclaringClass()).when(mockMethod).getDeclaringClass();
        when(mockMethod.getParameterTypes()).thenReturn(method.getParameterTypes());
        when(mockMethod.getName()).thenReturn(method.getName());
        when(mockMethod.getReturnType()).thenReturn(null);

        // when
        SerializableMethod mockSerializableMethod = new SerializableMethod(mockMethod);

        // then
        assertFalse(mockSerializableMethod.equals(method));
    }

    @Test
    public void shouldNotBeEqualForDifferentReturnTypes() throws Exception {
        // given
        Method mockMethod = mock(Method.class);
        doReturn(toStringMethod.getDeclaringClass()).when(mockMethod).getDeclaringClass();
        when(mockMethod.getParameterTypes()).thenReturn(method.getParameterTypes());
        when(mockMethod.getName()).thenReturn(method.getName());
        doReturn(int.class).when(mockMethod).getReturnType();

        // when
        SerializableMethod mockSerializableMethod = new SerializableMethod(mockMethod);

        // then
        assertFalse(mockSerializableMethod.equals(method));
    }
}
