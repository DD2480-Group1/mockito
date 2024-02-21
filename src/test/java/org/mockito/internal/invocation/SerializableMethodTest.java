/*
 * Copyright (c) 2007 Mockito contributors
 * This program is made available under the terms of the MIT License.
 */
package org.mockito.internal.invocation;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Field;

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


    //
    // Equals tests
    //

    @Test
    public void shouldGiveFalseIfMethodIsNull() throws Exception {
        Object testNullObj = null;
        assertFalse(method.equals(testNullObj));
    }

    @Test
    public void shouldGiveFalseIfDifferentGetClass() throws Exception {
        Method m1 = String.class.getMethod("toString", args);
        assertFalse(method.equals(m1));
    }


    @Test
    public void shouldBeTrueIfBothDeclareClassAreTrue() throws Exception {
        SerializableMethod m1 = new SerializableMethod(String.class.getMethod("toString", args));
        SerializableMethod m2 = new SerializableMethod(String.class.getMethod("toString", args));

        Field f = m1.getClass().getDeclaredField("declaringClass");

        f.setAccessible(true);
        f.set(m1, null);
        f.set(m2, null);

        assertTrue(m1.equals(m2));
    }

    @Test
    public void shouldBeFalseIfOneDeclaringClassIsNullAndTheOtherIsNot() throws Exception {
        SerializableMethod m1 = new SerializableMethod(String.class.getMethod("toString", args));
        SerializableMethod m2 = new SerializableMethod(String.class.getMethod("toString", args));

        Field f = m1.getClass().getDeclaredField("declaringClass");

        f.setAccessible(true);
        f.set(m1, null);

        assertFalse(m1.equals(m2));
    }

    @Test
    public void shouldGiveFalseIfDifferentMethodName() throws Exception {
        SerializableMethod m1 = new SerializableMethod(String.class.getMethod("toString", args));
        SerializableMethod m2 = new SerializableMethod(String.class.getMethod("toString", args));

        Field f = m1.getClass().getDeclaredField("methodName");

        f.setAccessible(true);
        f.set(m1, "someOtherName");

        assertFalse(m1.equals(m2));
    }

    @Test
    public void shouldGiveTrueIfBothMethodNamesAreNull() throws Exception {
        SerializableMethod m1 = new SerializableMethod(String.class.getMethod("toString", args));
        SerializableMethod m2 = new SerializableMethod(String.class.getMethod("toString", args));

        Field f = m1.getClass().getDeclaredField("methodName");

        f.setAccessible(true);
        f.set(m1, null);
        f.set(m2, null);

        assertTrue(m1.equals(m2));
    }

    @Test
    public void shouldGiveFalseIfOneMethodNameIsNullAndOtherIsNot() throws Exception {
        SerializableMethod m1 = new SerializableMethod(String.class.getMethod("toString", args));
        SerializableMethod m2 = new SerializableMethod(String.class.getMethod("toString", args));

        Field f = m1.getClass().getDeclaredField("methodName");

        f.setAccessible(true);
        f.set(m1, null);

        assertFalse(m1.equals(m2));
    }

    @Test
    public void shouldGiveFalseIfDifferentReturnType() throws Exception {
        SerializableMethod m1 = new SerializableMethod(String.class.getMethod("toString", args));
        SerializableMethod m2 = new SerializableMethod(String.class.getMethod("toString", args));

        Field f = m1.getClass().getDeclaredField("returnType");

        f.setAccessible(true);

        Class<?> c1 = String.class;
        Class<?> c2 = Integer.class;

        f.set(m1, c1);
        f.set(m2, c2);

        assertFalse(m1.equals(m2));
    }

    @Test
    public void shouldGiveTrueIfBothReturnTypesAreNull() throws Exception {
        SerializableMethod m1 = new SerializableMethod(String.class.getMethod("toString", args));
        SerializableMethod m2 = new SerializableMethod(String.class.getMethod("toString", args));

        Field f = m1.getClass().getDeclaredField("returnType");

        f.setAccessible(true);
        f.set(m1, null);
        f.set(m2, null);

        assertTrue(m1.equals(m2));
    }

    @Test
    public void shouldGiveFalseIfOneReturnTypeIsNullOtherIsNot() throws Exception {
        SerializableMethod m1 = new SerializableMethod(String.class.getMethod("toString", args));
        SerializableMethod m2 = new SerializableMethod(String.class.getMethod("toString", args));

        Field f = m1.getClass().getDeclaredField("returnType");

        f.setAccessible(true);
        f.set(m1, null);

        assertFalse(m1.equals(m2));
    }

    @Test
    public void shouldGiveFalseIfDifferentParameterTypes() throws Exception {
        Class<?>[] param1 = new Class<?>[1];
        Class<?>[] param2 = new Class<?>[1];
        param1[0] = String.class;
        param2[0] = Integer.class;

        SerializableMethod m1 = new SerializableMethod(String.class.getMethod("toString", args));
        SerializableMethod m2 = new SerializableMethod(String.class.getMethod("toString", args));

        Field f = m1.getClass().getDeclaredField("parameterTypes");

        f.setAccessible(true);
        f.set(m1, param1);
        f.set(m2,param2);

        assertFalse(m1.equals(m2));
    }

}
