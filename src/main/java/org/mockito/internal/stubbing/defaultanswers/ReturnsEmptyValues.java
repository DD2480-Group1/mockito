/*
 * Copyright (c) 2016 Mockito contributors
 * This program is made available under the terms of the MIT License.
 */
package org.mockito.internal.stubbing.defaultanswers;

import static org.mockito.internal.util.ObjectMethodsGuru.isCompareToMethod;
import static org.mockito.internal.util.ObjectMethodsGuru.isToStringMethod;

import java.io.Serializable;
import java.time.Duration;
import java.time.Period;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import org.mockito.internal.util.MockUtil;
import org.mockito.internal.util.Primitives;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.mock.MockName;
import org.mockito.stubbing.Answer;

/**
 * Default answer of every Mockito mock.
 * <ul>
 * <li>
 * Returns appropriate primitive for primitive-returning methods
 * </li>
 * <li>
 * Returns consistent values for primitive wrapper classes (e.g. int-returning method returns 0 <b>and</b> Integer-returning method returns 0, too)
 * </li>
 * <li>
 * Returns empty collection for collection-returning methods (works for most commonly used collection types)
 * </li>
 * <li>
 * Returns description of mock for toString() method
 * </li>
 * <li>
 * Returns zero if references are equals otherwise non-zero for Comparable#compareTo(T other) method (see issue 184)
 * </li>
 * <li>
 * Returns an {@code java.util.Optional#empty() empty Optional} for Optional. Similarly for primitive optional variants.
 * </li>
 * <li>
 * Returns an {@code java.util.stream.Stream#empty() empty Stream} for Stream. Similarly for primitive stream variants.
 * </li>
 * <li>
 * Returns an {@code java.time.Duration.ZERO zero Duration} for empty Duration and {@code java.time.Period.ZERO zero Period} for empty Period.
 * </li>
 * <li>
 * Returns null for everything else
 * </li>
 * </ul>
 */
public class ReturnsEmptyValues implements Answer<Object>, Serializable {

    private static final long serialVersionUID = 1998191268711234347L;

    /* (non-Javadoc)
     * @see org.mockito.stubbing.Answer#answer(org.mockito.invocation.InvocationOnMock)
     */
    @Override
    public Object answer(InvocationOnMock invocation) {
        if (isToStringMethod(invocation.getMethod())) {
            Object mock = invocation.getMock();
            MockName name = MockUtil.getMockName(mock);
            if (name.isDefault()) {
                return "Mock for "
                        + MockUtil.getMockSettings(mock).getTypeToMock().getSimpleName()
                        + ", hashCode: "
                        + mock.hashCode();
            } else {
                return name.toString();
            }
        } else if (isCompareToMethod(invocation.getMethod())) {
            // see issue 184.
            // mocks by default should return 0 if references are the same, otherwise some other
            // value because they are not the same. Hence we return 1 (anything but 0 is good).
            // Only for compareTo() method by the Comparable interface
            return invocation.getMock() == invocation.getArgument(0) ? 0 : 1;
        }

        Class<?> returnType = invocation.getMethod().getReturnType();
        return returnValueFor(returnType);
    }

    private static final int BRANCH_COUNT = 23;
    private static boolean branches[] = new boolean[BRANCH_COUNT];
    private static final String FILE_NAME = "[ReturnsEmptyValues.java]";
    private static final String FUNCTION_NAME = "[@returnValueFor]";

    public static void reachBranch(int id) {
        branches[id] = true;
    }
    public static void printBranchCoverage() {
        int coverage = 0;
        String state = "";
        for (int i = 0; i < branches.length; i++) {
            if (branches[i]) {
                state = "True";
                coverage++;
            } else {
                state = "False";
            }

            System.out.println(FILE_NAME + " " + FUNCTION_NAME + " Branch " + i + ": " + state);
        }
        double percentile = ((double) coverage / BRANCH_COUNT) * 100;
        System.out.println(FILE_NAME + " " + FUNCTION_NAME + " Branch hits: " + coverage + " (" + percentile + "%)");
    }

    Object returnValueFor(Class<?> type) {
        if (Primitives.isPrimitiveOrWrapper(type)) {
            reachBranch(0);
            return Primitives.defaultValue(type);
            // new instances are used instead of Collections.emptyList(), etc.
            // to avoid UnsupportedOperationException if code under test modifies returned
            // collection
        } else if (type == Iterable.class) {
            reachBranch(1);
            return new ArrayList<>(0);
        } else if (type == Collection.class) {
            reachBranch(2);
            return new LinkedList<>();
        } else if (type == Set.class) {
            reachBranch(3);
            return new HashSet<>();
        } else if (type == HashSet.class) {
            reachBranch(4);
            return new HashSet<>();
        } else if (type == SortedSet.class) {
            reachBranch(5);
            return new TreeSet<>();
        } else if (type == TreeSet.class) {
            reachBranch(6);
            return new TreeSet<>();
        } else if (type == LinkedHashSet.class) {
            reachBranch(7);
            return new LinkedHashSet<>();
        } else if (type == List.class) {
            reachBranch(8);
            return new LinkedList<>();
        } else if (type == LinkedList.class) {
            reachBranch(9);
            return new LinkedList<>();
        } else if (type == ArrayList.class) {
            reachBranch(10);
            return new ArrayList<>();
        } else if (type == Map.class) {
            reachBranch(11);
            return new HashMap<>();
        } else if (type == HashMap.class) {
            reachBranch(12);
            return new HashMap<>();
        } else if (type == SortedMap.class) {
            reachBranch(13);
            return new TreeMap<>();
        } else if (type == TreeMap.class) {
            reachBranch(14);
            return new TreeMap<>();
        } else if (type == LinkedHashMap.class) {
            reachBranch(15);
            return new LinkedHashMap<>();
        } else if (type == Stream.class) {
            reachBranch(16);
            return Stream.empty();
        } else if (type == DoubleStream.class) {
            reachBranch(17);
            return DoubleStream.empty();
        } else if (type == IntStream.class) {
            reachBranch(18);
            return IntStream.empty();
        } else if (type == LongStream.class) {
            reachBranch(19);
            return LongStream.empty();
        } else if (type == Duration.class) {
            reachBranch(20);
            return Duration.ZERO;
        } else if (type == Period.class) {
            reachBranch(21);
            return Period.ZERO;
        }
        // Let's not care about the rest of collections.
        reachBranch(22);
        return returnCommonEmptyValueFor(type);
    }

    /**
     * Returns empty values for common known types, shared between {@link ReturnsEmptyValues} and {@link ReturnsDeepStubs}.
     *
     * @param type the type to check
     * @return the empty value, or {@code null}
     */
    static Object returnCommonEmptyValueFor(Class<?> type) {
        if (type == Optional.class) {
            return Optional.empty();
        } else if (type == OptionalDouble.class) {
            return OptionalDouble.empty();
        } else if (type == OptionalInt.class) {
            return OptionalInt.empty();
        } else if (type == OptionalLong.class) {
            return OptionalLong.empty();
        }
        return null;
    }
}
