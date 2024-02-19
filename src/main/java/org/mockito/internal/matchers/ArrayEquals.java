/*
 * Copyright (c) 2007 Mockito contributors
 * This program is made available under the terms of the MIT License.
 */
package org.mockito.internal.matchers;

import java.lang.reflect.Array;
import java.util.Arrays;

public class ArrayEquals extends Equals {

    public ArrayEquals(Object wanted) {
        super(wanted);
    }

    private static final int BRANCHES = 40;
    private static boolean[] branches = new boolean[BRANCHES];

    private static void branchReached(int id) {
        // leave if branch already reached
        if (branches[id])
            return;
        branches[id] = true;

        // todo: write syso to file instead
        System.out.println("[ArrayEquals.java] [@matches] Branch " + id + " reached.");
    }

    public static void printBranchCoverage() {
        int cov = 0;
        for (int i = 0; i < branches.length; i++) {
            if (branches[i]) {
                System.out.println("[ArrayEquals.java] [@matches] Branch " + i + ": True ");
                cov++;
            }
            else {
                System.out.println("[ArrayEquals.java] [@matches] Branch " + i + ": False ");
            }
        }
        double result = ((double) cov / BRANCHES) * 100;
        System.out.println("[ArrayEquals.java] [@matches] Branch hits: " + cov + " (" + result + "%)");
    }

    @Override
    public boolean matches(Object actual) {
        Object wanted = getWanted();

        boolean A;
        boolean B;

        // A  B   &&  ||
        // 1  1   1   1
        // 1  0   0   1
        // 0  1   0   1
        // 0  0   0   0

        // each if-statement generates 4 unique branches
        // 10 ifs creates: 10 * 4 = 40 branches

        if (wanted == null) {
            branchReached(0);
            return super.matches(actual);
        } else {
            branchReached(1);
        }

        if (actual == null) {
            branchReached(2);
            return super.matches(actual);
        } else {
            branchReached(3);
        }

        A = wanted instanceof boolean[];
        B = actual instanceof boolean[];
        if (A) {
            branchReached(4);
            if (B) {
                branchReached(5);
                return Arrays.equals((boolean[]) wanted, (boolean[]) actual);
            }
            else {
                branchReached(6);
            }
        }
        else {
            branchReached(7);
        }

        A = wanted instanceof byte[];
        B = actual instanceof byte[];
        if (A) {
            branchReached(8);
            if (B) {
                branchReached(9);
                return Arrays.equals((byte[]) wanted, (byte[]) actual);
            }
            else {
                branchReached(10);
            }
        }
        else {
            branchReached(11);
        }

        A = wanted instanceof char[];
        B = actual instanceof char[];
        if (A) {
            branchReached(12);
            if (B) {
                branchReached(13);
                return Arrays.equals((char[]) wanted, (char[]) actual);
            }
            else {
                branchReached(14);
            }
        }
        else {
            branchReached(15);
        }

        A = wanted instanceof double[];
        B = actual instanceof double[];
        if (A) {
            branchReached(16);
            if (B) {
                branchReached(17);
                return Arrays.equals((double[]) wanted, (double[]) actual);
            }
            else {
                branchReached(18);
            }
        }
        else {
            branchReached(19);
        }

        A = wanted instanceof float[];
        B = actual instanceof float[];
        if (A) {
            branchReached(20);
            if (B) {
                branchReached(21);
                return Arrays.equals((float[]) wanted, (float[]) actual);
            }
            else {
                branchReached(22);
            }
        }
        else {
            branchReached(23);
        }

        A = wanted instanceof int[];
        B = actual instanceof int[];
        if (A) {
            branchReached(24);
            if (B) {
                branchReached(25);
                return Arrays.equals((int[]) wanted, (int[]) actual);
            }
            else {
                branchReached(26);
            }
        }
        else {
            branchReached(27);
        }

        A = wanted instanceof long[];
        B = actual instanceof long[];
        if (A) {
            branchReached(28);
            if (B) {
                branchReached(29);
                return Arrays.equals((long[]) wanted, (long[]) actual);
            }
            else {
                branchReached(30);
            }
        }
        else {
            branchReached(31);
        }

        A = wanted instanceof short[];
        B = actual instanceof short[];
        if (A) {
            branchReached(32);
            if (B) {
                branchReached(33);
                return Arrays.equals((short[]) wanted, (short[]) actual);
            }
            else {
                branchReached(34);
            }
        }
        else {
            branchReached(35);
        }

        A = wanted instanceof Object[];
        B = actual instanceof Object[];
        if (A) {
            branchReached(36);
            if (B) {
                branchReached(37);
                return Arrays.equals((Object[]) wanted, (Object[]) actual);
            }
            else {
                branchReached(38);
            }
        }
        else {
            branchReached(39);
        }

        return false;

    }

    @Override
    public String toString() {
        if (getWanted() != null && getWanted().getClass().isArray()) {
            return appendArray(createObjectArray(getWanted()));
        } else {
            return super.toString();
        }
    }

    private String appendArray(Object[] array) {
        // TODO SF overlap with ValuePrinter
        StringBuilder out = new StringBuilder("[");
        for (int i = 0; i < array.length; i++) {
            out.append(new Equals(array[i]));
            if (i != array.length - 1) {
                out.append(", ");
            }
        }
        out.append("]");
        return out.toString();
    }

    public static Object[] createObjectArray(Object array) {
        if (array instanceof Object[]) {
            return (Object[]) array;
        }
        Object[] result = new Object[Array.getLength(array)];
        for (int i = 0; i < Array.getLength(array); i++) {
            result[i] = Array.get(array, i);
        }
        return result;
    }
}
