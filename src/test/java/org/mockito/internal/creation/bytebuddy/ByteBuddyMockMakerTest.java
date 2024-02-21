/*
 * Copyright (c) 2017 Mockito contributors
 * This program is made available under the terms of the MIT License.
 */
package org.mockito.internal.creation.bytebuddy;

import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.mockito.Mock;
import org.mockito.internal.creation.settings.CreationSettings;
import org.mockito.internal.handler.MockHandlerImpl;
import org.mockito.plugins.MockMaker;
import org.mockitoutil.TestBase;

import static org.assertj.core.api.Assertions.assertThat;

public class ByteBuddyMockMakerTest extends TestBase {

    @Mock private SubclassByteBuddyMockMaker delegate;

    @Mock private SubclassByteBuddyMockMaker delegateStatic;

    @Test
    public void should_delegate_call() {
        ByteBuddyMockMaker mockMaker = new ByteBuddyMockMaker(delegate);

        CreationSettings<Object> creationSettings = new CreationSettings<Object>();
        MockHandlerImpl<Object> handler = new MockHandlerImpl<Object>(creationSettings);

        mockMaker.createMockType(creationSettings);
        mockMaker.createMock(creationSettings, handler);
        mockMaker.getHandler(this);
        mockMaker.isTypeMockable(Object.class);
        mockMaker.resetMock(this, handler, creationSettings);

        verify(delegate).createMock(creationSettings, handler);
        verify(delegate).createMockType(creationSettings);
        verify(delegate).getHandler(this);
        verify(delegate).isTypeMockable(Object.class);
        verify(delegate).resetMock(this, handler, creationSettings);
    }

    @Test
    public void simple_mock_maker_not_delegate() {
        // create a mock class mockMaker with no delegate
        ByteBuddyMockMaker mockMaker = new ByteBuddyMockMaker();
        // ensure the mockMaker is not null
        assertThat(mockMaker).isNotNull();
    }

    @Test
    public void test_clear_mock_cache() {
        ByteBuddyMockMaker mockMaker = new ByteBuddyMockMaker(delegate);
        CreationSettings<Base> creationSettings = new CreationSettings<Base>();
        MockHandlerImpl<Base> handler = new MockHandlerImpl<Base>(creationSettings);
        assertThat(mockMaker).isNotNull();

        MockMaker.StaticMockControl<Base> control = mockMaker.createStaticMock(Base.class, creationSettings, handler);
        mockMaker.createMockType(creationSettings);
        mockMaker.getHandler(this);
        mockMaker.isTypeMockable(Base.class);
        mockMaker.resetMock(this, handler, creationSettings);
        // the static mock is not supported by ByteBuddyMockMaker and thus
        // will return null
        assertThat(control).isNull();

        // clear all caches
        mockMaker.clearAllCaches();

        // verify all calls to the mockMaker
        // dogfooding, use Mockito to verify the calls to the mockMaker
        verify(delegate).createStaticMock(Base.class, creationSettings, handler);
        verify(delegate).createMockType(creationSettings);
        verify(delegate).getHandler(this);
        verify(delegate).isTypeMockable(Base.class);
        verify(delegate).resetMock(this, handler, creationSettings);
        // verify cache is cleared
        verify(delegate).clearAllCaches();
    }

    @Test
    public void test_static_mock_should_delegate_all() {
        // create a mock class mockMaker
        ByteBuddyMockMaker mockMaker = new ByteBuddyMockMaker(delegateStatic);
        CreationSettings<Object> creationSettings = new CreationSettings<Object>();
        MockHandlerImpl<Object> handler = new MockHandlerImpl<Object>(creationSettings);

        // delegate all calls to the mockMaker with static mockMaker
        mockMaker.createStaticMock(Object.class, creationSettings, handler);
        mockMaker.createMockType(creationSettings);
        mockMaker.getHandler(this);
        mockMaker.isTypeMockable(Object.class);
        mockMaker.resetMock(this, handler, creationSettings);

        // verify all calls to the mockMaker with static mockMaker
        // dogfooding, use Mockito to verify the calls to the mockMaker
        verify(delegateStatic).createStaticMock(Object.class, creationSettings, handler);
        verify(delegateStatic).createMockType(creationSettings);
        verify(delegateStatic).getHandler(this);
        verify(delegateStatic).isTypeMockable(Object.class);
        verify(delegateStatic).resetMock(this, handler, creationSettings);
    }

    abstract static class Base {}
}
