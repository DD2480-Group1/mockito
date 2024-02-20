/*
 * Copyright (c) 2007 Mockito contributors
 * This program is made available under the terms of the MIT License.
 */
package org.mockito.internal.invocation;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

import org.mockito.exceptions.base.MockitoException;
import org.mockito.internal.creation.SuspendMethod;

public class SerializableMethod implements Serializable, MockitoMethod {

    private static final long serialVersionUID = 6005610965006048445L;

    private final Class<?> declaringClass;
    private final String methodName;
    private final Class<?>[] parameterTypes;
    private final Class<?> returnType;
    private final Class<?>[] exceptionTypes;
    private final boolean isVarArgs;
    private final boolean isAbstract;

    private transient volatile Method method;

    public SerializableMethod(Method method) {
        this.method = method;
        declaringClass = method.getDeclaringClass();
        methodName = method.getName();
        parameterTypes = SuspendMethod.trimSuspendParameterTypes(method.getParameterTypes());
        returnType = method.getReturnType();
        exceptionTypes = method.getExceptionTypes();
        isVarArgs = method.isVarArgs();
        isAbstract = (method.getModifiers() & Modifier.ABSTRACT) != 0;
    }

    @Override
    public String getName() {
        return methodName;
    }

    @Override
    public Class<?> getReturnType() {
        return returnType;
    }

    @Override
    public Class<?>[] getParameterTypes() {
        return parameterTypes;
    }

    @Override
    public Class<?>[] getExceptionTypes() {
        return exceptionTypes;
    }

    @Override
    public boolean isVarArgs() {
        return isVarArgs;
    }

    @Override
    public boolean isAbstract() {
        return isAbstract;
    }

    @Override
    public Method getJavaMethod() {
        if (method != null) {
            return method;
        }
        try {
            method = declaringClass.getDeclaredMethod(methodName, parameterTypes);
            return method;
        } catch (SecurityException e) {
            String message =
                    String.format(
                            "The method %1$s.%2$s is probably private or protected and cannot be mocked.\n"
                                    + "Please report this as a defect with an example of how to reproduce it.",
                            declaringClass, methodName);
            throw new MockitoException(message, e);
        } catch (NoSuchMethodException e) {
            String message =
                    String.format(
                            "The method %1$s.%2$s does not exists and you should not get to this point.\n"
                                    + "Please report this as a defect with an example of how to reproduce it.",
                            declaringClass, methodName);
            throw new MockitoException(message, e);
        }
    }

    @Override
    public int hashCode() {
        return 1;
    }

    public static void printBranchCoverage() {
        int cov = 0;
        for (int i = 0; i < branches.length; i++) {
            if (branches[i]) {
                System.out.println("[SerializableMethod.java] [@equals] Branch " + i + ": True ");
                cov++;
            }
            else {
                System.out.println("[SerializableMethod.java] [@equals] Branch " + i + ": False ");
            }
        }
        double result = ((double) cov / BRANCH_NO) * 100;
        System.out.println("[SerializableMethod.java] [@equals] Branch hits: " + cov + " (" + result + "%)");
    }

    private static final int BRANCH_NO = 23;
    private static boolean branches[] = new boolean[BRANCH_NO];

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            branches[0] = true;
            return true;
        }
        branches[1] = true;
        if (obj == null) {
            branches[2] = true;
            return false;
        }
        branches[3] = true;
        if (getClass() != obj.getClass()) {
            branches[4] = true;
            return false;
        }
        branches[5] = true;
        SerializableMethod other = (SerializableMethod) obj;
        if (declaringClass == null) {
            branches[6] = true;
            if (other.declaringClass != null) {
                branches[7] = true;
                return false;
            }
            branches[8] = true;
        } else if (!declaringClass.equals(other.declaringClass)) {
            branches[9] = true;
            return false;
        }
        branches[10] = true;
        if (methodName == null) {
            branches[11] = true;
            if (other.methodName != null) {
                branches[12] = true;
                return false;
            }
            branches[13] = true;
        } else if (!methodName.equals(other.methodName)) {
            branches[14] = true;
            return false;
        }
        branches[15] = true;
        if (!Arrays.equals(parameterTypes, other.parameterTypes)) {
            branches[16] = true;
            return false;
        }
        branches[17] = true;
        if (returnType == null) {
            branches[18] = true;
            if (other.returnType != null) {
                branches[19] = true;
                return false;
            }
            branches[20] = true;
        } else if (!returnType.equals(other.returnType)) {
            branches[21] = true;
            return false;
        }
        branches[22] = true;
        return true;
    }
}
