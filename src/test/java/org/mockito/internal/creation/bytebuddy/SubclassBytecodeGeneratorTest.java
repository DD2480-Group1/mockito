/*
 * Copyright (c) 2017 Mockito contributors
 * This program is made available under the terms of the MIT License.
 */
package org.mockito.internal.creation.bytebuddy;

import org.junit.Test;
import org.mockito.Answers;
import org.mockito.mock.SerializableMode;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;

import static org.mockito.internal.creation.bytebuddy.MockFeatures.withMockFeatures;
import static org.mockitoutil.ClassLoaders.inMemoryClassLoader;
import static org.mockitoutil.SimpleClassGenerator.makeMarkerInterface;

public class SubclassBytecodeGeneratorTest {

    @Test
    public void test_serializable_mode() throws Exception {
        ClassLoader classLoader =
            inMemoryClassLoader()
                .withClassDefinition("foo.Bar", makeMarkerInterface("foo.Bar"))
                .build();
        SubclassBytecodeGenerator subclassBytecodeGenerator = new SubclassBytecodeGenerator();
        assertThat(subclassBytecodeGenerator).isNotNull();
        // create a mock class with serializable mode ACROSS_CLASSLOADERS
        Class<?> the_mock_type =
            subclassBytecodeGenerator.mockClass(
                withMockFeatures(
                    classLoader.loadClass("foo.Bar"),
                    Collections.<Class<?>>emptySet(),
                    SerializableMode.ACROSS_CLASSLOADERS,
                    false,
                    Answers.RETURNS_DEFAULTS));
        // ensure the mock class is not null and is created
        assertThat(the_mock_type).isNotNull();
    }

}
