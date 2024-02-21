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

    /**
     * Ensure that if we try to compare to null, it returns false.
     * expected: returns false
     * @throws Exception
     */
    @Test
    public void shouldNotBeEqualForNull() throws Exception {
        assertFalse(method.equals(null));
    }

    /**
     * Tests if we compare two methods with same classes (String), but with different method names, the equals should
     * return False.
     * Will therefore cover specific branch where two method names are not the same.
     * expected: toString and hashCode are two different method names, therefore should return false
     * @throws Exception
     */
    @Test
    public void shouldNotBeEqualForDifferentMethodNames() throws Exception {
        Method toStringMethod = String.class.getMethod("toString", args);
        SerializableMethod methodToString = new SerializableMethod(toStringMethod);

        Method hashCodeMethod = String.class.getMethod("hashCode", args);
        SerializableMethod methodHashCode = new SerializableMethod(hashCodeMethod);

        assertFalse(methodToString.equals(methodHashCode));
    }

    /**
     * Test if we compare between different class types.
     * Object Class is different from String Class.
     * expected: the classes are different so should return false
     * @throws Exception
     */
    @Test
    public void shouldNotBeEqualForDifferentClassTypes() throws Exception {
        assertFalse(method.equals(new Object()));
    }

    /**
     * Test if we compare between two methods that have the same class, same method name, but different parameterTypes.
     * StringBuilder "append" method can be overloaded, so it can take different inputs parameters.
     * expected: The parameterTypes for the two append methods are different, so should return false
     * @throws Exception
     */
    @Test
    public void shouldNotBeEqualForDifferentParameterTypes() throws Exception {
        Method appendWithInt = StringBuilder.class.getMethod("append", int.class);
        SerializableMethod method1 = new SerializableMethod(appendWithInt);

        Method appendWithString = StringBuilder.class.getMethod("append", String.class);
        SerializableMethod method2 = new SerializableMethod(appendWithString);

        assertFalse(method1.equals(method2));
    }

    /**
     * Using Mockito's mock class to specifically test for if getDeclaringClass() returns null, by setting that in mock.
     * This covers a specific branch case if the object that is comparing has getDeclaringClass return null while
     * the comparison object's getDeclaringClass does not return null.
     * expected: equals should return false
     * @throws Exception
     */
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

    /**
     * This test check if equals method can handle when method name of invoking object is null.
     * Constructed using mock, create a mock method where the getGanme() returns null.
     * expected: Method name of invoking object should be null, but comparison object is not null, thus return false
     * @throws Exception
     */
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

    /**
     * This test covers a case where the returnType of a method is null. A mock method is created where the
     * getReturnType() method is set to return null. When mock method is invoked for equals, it should fail test where
     * invoking object returnType is null but comparison objects returnType is not null.
     * expected: invoking objects returnType is null and comparison object is not null, thus return false
     * @throws Exception
     */
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

    /**
     * Test whether equals can handel when invoking object and comparison object have same parameters for nearly
     * everything except the returnTypes. Using mock to construct mock method to test against.
     * expected: since method and mockMethod have different returnTypes, equals should return false
     * @throws Exception
     */
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
