/*
 * Copyright (c) 2024 Mockito contributors
 * This program is made available under the terms of the MIT License.
 */
package org.mockito.internal.creation.bytebuddy;

import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.implementation.Implementation;
import net.bytebuddy.jar.asm.MethodVisitor;
import net.bytebuddy.pool.TypePool;
import org.junit.Test;
import org.mockito.internal.creation.bytebuddy.MockMethodAdvice;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;
import org.junit.AfterClass;
import org.mockitoutil.TestBase;

public class MockitoMethodAdviceTest extends TestBase {
    @AfterClass
    public static void printCoverageMertics() {
        MockMethodAdvice.ConstructorShortcut.printBranchCoverage();
    }
}
