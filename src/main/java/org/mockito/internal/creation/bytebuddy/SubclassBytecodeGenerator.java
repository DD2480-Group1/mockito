/*
 * Copyright (c) 2016 Mockito contributors
 * This program is made available under the terms of the MIT License.
 */
package org.mockito.internal.creation.bytebuddy;

import static java.lang.Thread.currentThread;
import static net.bytebuddy.description.modifier.Visibility.PRIVATE;
import static net.bytebuddy.dynamic.Transformer.ForMethod.withModifiers;
import static net.bytebuddy.implementation.MethodDelegation.to;
import static net.bytebuddy.implementation.attribute.MethodAttributeAppender.ForInstrumentedMethod.INCLUDING_RECEIVER;
import static net.bytebuddy.matcher.ElementMatchers.any;
import static net.bytebuddy.matcher.ElementMatchers.hasParameters;
import static net.bytebuddy.matcher.ElementMatchers.hasType;
import static net.bytebuddy.matcher.ElementMatchers.isEquals;
import static net.bytebuddy.matcher.ElementMatchers.isHashCode;
import static net.bytebuddy.matcher.ElementMatchers.isPackagePrivate;
import static net.bytebuddy.matcher.ElementMatchers.returns;
import static net.bytebuddy.matcher.ElementMatchers.whereAny;
import static org.mockito.internal.util.StringUtil.join;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.*;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.modifier.SynchronizationState;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.loading.MultipleParentClassLoader;
import net.bytebuddy.dynamic.scaffold.TypeValidation;
import net.bytebuddy.implementation.FieldAccessor;
import net.bytebuddy.implementation.Implementation;
import net.bytebuddy.implementation.attribute.MethodAttributeAppender;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.utility.GraalImageCode;
import net.bytebuddy.utility.RandomString;
import org.mockito.codegen.InjectionBase;
import org.mockito.exceptions.base.MockitoException;
import org.mockito.internal.creation.bytebuddy.ByteBuddyCrossClassLoaderSerializationSupport.CrossClassLoaderSerializableMock;
import org.mockito.internal.creation.bytebuddy.MockMethodInterceptor.DispatcherDefaultingToRealMethod;
import org.mockito.mock.SerializableMode;

import javax.swing.*;

public class SubclassBytecodeGenerator implements BytecodeGenerator {

    private static final String CODEGEN_PACKAGE = "org.mockito.codegen.";

    private final SubclassLoader loader;
    private final ModuleHandler handler;
    private final ByteBuddy byteBuddy;
    private final Implementation readReplace;
    private final ElementMatcher<? super MethodDescription> matcher;

    private final Implementation dispatcher = to(DispatcherDefaultingToRealMethod.class);
    private final Implementation hashCode = to(MockMethodInterceptor.ForHashCode.class);
    private final Implementation equals = to(MockMethodInterceptor.ForEquals.class);
    private final Implementation writeReplace = to(MockMethodInterceptor.ForWriteReplace.class);

    public SubclassBytecodeGenerator() {
        this(new SubclassInjectionLoader());
    }

    public SubclassBytecodeGenerator(SubclassLoader loader) {
        this(loader, null, any());
    }

    public SubclassBytecodeGenerator(
            Implementation readReplace, ElementMatcher<? super MethodDescription> matcher) {
        this(new SubclassInjectionLoader(), readReplace, matcher);
    }

    protected SubclassBytecodeGenerator(
            SubclassLoader loader,
            Implementation readReplace,
            ElementMatcher<? super MethodDescription> matcher) {
        this.loader = loader;
        this.readReplace = readReplace;
        this.matcher = matcher;
        byteBuddy = new ByteBuddy().with(TypeValidation.DISABLED);
        handler = ModuleHandler.make(byteBuddy, loader);
    }

    private static boolean needsSamePackageClassLoader(MockFeatures<?> features) {
        if (!Modifier.isPublic(features.mockedType.getModifiers())
                || !features.mockedType.isInterface()) {
            // The mocked type is package private or is not an interface and thus may contain
            // package private methods.
            return true;
        }
        if (hasNonPublicTypeReference
            (features.mockedType)) {
            return true;
        }

        for (Class<?> iface : features.interfaces) {
            if (!Modifier.isPublic(iface.getModifiers())) {
                return true;
            }
            if (hasNonPublicTypeReference(iface)) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasNonPublicTypeReference(Class<?> iface) {
        for (Method method : iface.getMethods()) {
            if (!Modifier.isPublic(method.getReturnType().getModifiers())) {
                return true;
            }
            for (Class<?> param : method.getParameterTypes()) {
                if (!Modifier.isPublic(param.getModifiers())) {
                    return true;
                }
            }
        }
        return false;
    }

    private static final int BRANCHES = 54;
    private static final boolean[] branches = new boolean[BRANCHES];

    private static void branchReached(int index) {
        branches[index] = true;
        if (branches[index]) {
            return;
        }
        System.out.println("Branch " + index + " reached");
    }

    public static void printBranchCoverage() {
        int covered = 0;
        for (int i = 0; i < BRANCHES; i++) {
            if (branches[i]) {
                System.out.println("Branch " + i + " reached");
                covered++;
            }
            else {
                System.out.println("Branch " + i + " not reached");
            }
        }
        double coverage = (double) covered / BRANCHES;
        System.out.println("Branch coverage: " + coverage);
    }
    @Override
    public <T> Class<? extends T> mockClass(MockFeatures<T> features) {
        MultipleParentClassLoader.Builder loaderBuilder =
                new MultipleParentClassLoader.Builder()
                        .appendMostSpecific(features.mockedType)
                        .appendMostSpecific(features.interfaces)
                        .appendMostSpecific(
                                MockAccess.class, DispatcherDefaultingToRealMethod.class)
                        .appendMostSpecific(
                                MockMethodInterceptor.class,
                                MockMethodInterceptor.ForHashCode.class,
                                MockMethodInterceptor.ForEquals.class);
        ClassLoader contextLoader = currentThread().getContextClassLoader();
        boolean shouldIncludeContextLoader = true;
        if (needsSamePackageClassLoader(features)) {
            branchReached(0);
            // For the generated class to access package-private methods, it must be defined by the
            // same classloader as its type. All the other added classloaders are required to load
            // the type; if the context classloader is a child of the mocked type's defining
            // classloader, it will break a mock that would have worked. Check if the context class
            // loader is a child of the classloader we'd otherwise use, and possibly skip it.
            ClassLoader candidateLoader = loaderBuilder.build();
            for (ClassLoader parent = contextLoader; parent != null; parent = parent.getParent()) {
                branchReached(1);
                if (parent == candidateLoader) {
                    branchReached(2);
                    shouldIncludeContextLoader = false;
                    break;
                }
                else {
                    branchReached(3);
                }
            }
        }
        else {
            branchReached(4);
        }

        if (shouldIncludeContextLoader) {
            branchReached(5);
            loaderBuilder = loaderBuilder.appendMostSpecific(contextLoader);
        }
        else {
            branchReached(6);
        }
        ClassLoader classLoader = loaderBuilder.build();

        // If Mockito does not need to create a new class loader and if a mock is not based on a JDK
        // type, we attempt
        // to define the mock class in the user runtime package to allow for mocking package private
        // types and methods.
        // This also requires that we are able to access the package of the mocked class either by
        // override or explicit
        // privilege given by the target package being opened to Mockito.

        boolean localMock;
        if (classLoader == features.mockedType.getClassLoader()) {
            branchReached(7);
            if (features.serializableMode != SerializableMode.ACROSS_CLASSLOADERS) {
                branchReached(8);
                if (!isComingFromJDK(features.mockedType)) {
                    branchReached(9);
                    if (loader.isDisrespectingOpenness()) {
                        branchReached(10);
                        localMock = true;
                    }
                    else {
                        branchReached(11);
                        if (handler.isOpened(features.mockedType, MockAccess.class)) {
                            branchReached(12);
                            localMock = true;
                        }
                        else {
                            branchReached(13);
                            localMock = false;
                        }
                    }
                }
                else {
                    branchReached(14);
                    localMock = false;
                }
            }
            else {
                branchReached(15);
                localMock = false;
            }
        }
        else {
            branchReached(16);
            localMock = false;
        }

        String typeName;
        if (localMock)
        {
            branchReached(17);
            typeName = features.mockedType.getName();
        }
        else
        {
            branchReached(18);
            if (loader instanceof MultipleParentClassLoader)
            {
                branchReached(19);
                if (!isComingFromJDK(features.mockedType))
                {
                    branchReached(20);
                    typeName = features.mockedType.getName();
                }
                else
                {
                    branchReached(21);
                    typeName = InjectionBase.class.getPackage().getName() + "." + features.mockedType.getSimpleName();
                }
            }
            else
            {
                branchReached(22);
                typeName = InjectionBase.class.getPackage().getName() + "." + features.mockedType.getSimpleName();
            }
        }
        String name =
                String.format(
                        "%s$%s$%s",
                        typeName,
                        "MockitoMock",
                        GraalImageCode.getCurrent().isDefined()
                                ? suffix(features)
                                : RandomString.make());

        if (localMock) {
            branchReached(23);
            handler.adjustModuleGraph(features.mockedType, MockAccess.class, false, true);
            boolean innerLoopReached = false;
            for (Class<?> iFace : features.interfaces) {
                innerLoopReached = true;
                branchReached(24);
                handler.adjustModuleGraph(iFace, features.mockedType, true, false);
                handler.adjustModuleGraph(features.mockedType, iFace, false, true);
            }
            if (!innerLoopReached) {
                branchReached(25);
            }
        } else {
            branchReached(26);
            boolean exported = handler.isExported(features.mockedType);
            Iterator<Class<?>> it = features.interfaces.iterator();
            boolean innerLoopReached = false;
            while (exported && it.hasNext()) {
                branchReached(27);
                innerLoopReached = true;
                exported = handler.isExported(it.next());
            }
            if (!innerLoopReached) {
                branchReached(28);
            }
            // We check if all mocked types are exported without qualification to avoid generating a
            // hook type.
            // unless this is necessary. We expect this to be the case for most mocked types what
            // makes this a
            // worthy performance optimization.
            if (exported) {
                branchReached(29);
                boolean innerLoopReached2 = false;
                assertVisibility(features.mockedType);
                for (Class<?> iFace : features.interfaces) {
                    branchReached(30);
                    innerLoopReached2 = true;
                    assertVisibility(iFace);
                }
                if (!innerLoopReached2) {
                    branchReached(31);
                }
            } else {
                branchReached(32);
                Class<?> hook = handler.injectionBase(classLoader, typeName);
                assertVisibility(features.mockedType);
                handler.adjustModuleGraph(features.mockedType, hook, true, false);
                boolean innerLoopReached3 = false;
                for (Class<?> iFace : features.interfaces) {
                    branchReached(33);
                    innerLoopReached3 = true;
                    assertVisibility(iFace);
                    handler.adjustModuleGraph(iFace, hook, true, false);
                }
                if (!innerLoopReached3) {
                    branchReached(34);
                }
            }

        }
        // Graal requires that the byte code of classes is identical what requires that interfaces
        // are always defined in the exact same order. Therefore, we add an interface to the
        // interface set if not mocking a class when Graal is active.

//        Class<T> target =
//                GraalImageCode.getCurrent().isDefined() && features.mockedType.isInterface()
//                        ? (Class<T>) Object.class
//                        : features.mockedType;
        @SuppressWarnings("unchecked")
Class<T> target;
        if (GraalImageCode.getCurrent().isDefined()) {
            branchReached(35);
            if (features.mockedType.isInterface()) {
                branchReached(36);
                target = (Class<T>) Object.class;
            }
            else {
                branchReached(37);
                target = features.mockedType;
            }
        }
        else {
            branchReached(38);
            target = features.mockedType;
        }
        // If we create a mock for an interface with additional interfaces implemented, we do not
        // want to preserve the annotations of either interface. The caching mechanism does not
        // consider the order of these interfaces and the same mock class might be reused for
        // either order. Also, it does not have clean semantics as annotations are not normally
        // preserved for interfaces in Java.
        Annotation[] annotationsOnType;
        if (features.stripAnnotations) {
            branchReached(39);
            annotationsOnType = new Annotation[0];
        } else if (!features.mockedType.isInterface() || features.interfaces.isEmpty()) {
            branchReached(40);
            annotationsOnType = features.mockedType.getAnnotations();
        } else {
            branchReached(41);
            annotationsOnType = new Annotation[0];
        }

        boolean cond1 = GraalImageCode.getCurrent().isDefined();
        boolean cond2 = features.mockedType.isInterface();
        boolean cond3 = features.stripAnnotations;

        DynamicType.Builder<T> builder = byteBuddy
            .subclass(target)
            .name(name)
            .ignoreAlso(BytecodeGenerator.isGroovyMethod(false))
            .annotateType(annotationsOnType)
            .method(matcher)
            .intercept(dispatcher)
            .transform(withModifiers(SynchronizationState.PLAIN))
            .attribute(cond3 ? MethodAttributeAppender.NoOp.INSTANCE : INCLUDING_RECEIVER)
            .serialVersionUid(42L)
            .defineField("mockitoInterceptor", MockMethodInterceptor.class, PRIVATE)
            .implement(MockAccess.class)
            .intercept(FieldAccessor.ofBeanProperty())
            .method(isHashCode())
            .intercept(hashCode)
            .method(isEquals())
            .intercept(equals);
        if (cond1) {
            branchReached(42);
            if (cond2) {
                branchReached(43);
                builder = builder.implement(new ArrayList<>(sortedSerializable(features.interfaces, features.mockedType)));
            }
            else {
                branchReached(44);
                builder = builder.implement(new ArrayList<>(sortedSerializable(features.interfaces, void.class)));
            }
        }
        else {
            branchReached(45);
            builder = builder.implement(new ArrayList<>(features.interfaces));
        }
        if (cond3) {
            branchReached(46);
        }
        else {
            branchReached(47);
        }

        if (features.serializableMode == SerializableMode.ACROSS_CLASSLOADERS) {
            branchReached(48);
            builder =
                    builder.implement(CrossClassLoaderSerializableMock.class)
                            .intercept(writeReplace);
        }
        else {
            branchReached(49);
        }
        if (readReplace != null) {
            branchReached(50);
            builder =
                    builder.defineMethod("readObject", void.class, Visibility.PRIVATE)
                            .withParameters(ObjectInputStream.class)
                            .throwing(ClassNotFoundException.class, IOException.class)
                            .intercept(readReplace);
        }
        else {
            branchReached(51);
        }

        if (name.startsWith(CODEGEN_PACKAGE) || classLoader instanceof MultipleParentClassLoader) {
            branchReached(52);
            builder =
                    builder.ignoreAlso(
                            isPackagePrivate()
                                    .or(returns(isPackagePrivate()))
                                    .or(hasParameters(whereAny(hasType(isPackagePrivate())))));
        }
        else {
            branchReached(53);
        }
        return builder.make()
                .load(
                        classLoader,
                        loader.resolveStrategy(features.mockedType, classLoader, localMock))
                .getLoaded();
    }

    private static CharSequence suffix(MockFeatures<?> features) {
        // Constructs a deterministic suffix for this mock to assure that mocks always carry the
        // same name.
        StringBuilder sb = new StringBuilder();
        Set<String> names = new TreeSet<>();
        names.add(features.mockedType.getName());
        for (Class<?> type : features.interfaces) {
            names.add(type.getName());
        }
        return sb.append(RandomString.hashOf(names.hashCode()))
                .append(RandomString.hashOf(features.serializableMode.name().hashCode()))
                .append(features.stripAnnotations ? "S" : "N");
    }

    private static Collection<? extends Type> sortedSerializable(
            Collection<Class<?>> interfaces, Class<?> mockedType) {
        SortedSet<Class<?>> types = new TreeSet<>(Comparator.comparing(Class::getName));
        types.addAll(interfaces);
        if (mockedType != void.class) {
            types.add(mockedType);
        }
        types.add(Serializable.class);
        return types;
    }

    @Override
    public void mockClassStatic(Class<?> type) {
        throw new MockitoException("The subclass byte code generator cannot create static mocks");
    }

    @Override
    public void mockClassConstruction(Class<?> type) {
        throw new MockitoException(
                "The subclass byte code generator cannot create construction mocks");
    }

    private boolean isComingFromJDK(Class<?> type) {
        // Comes from the manifest entry :
        // Implementation-Title: Java Runtime Environment
        // This entry is not necessarily present in every jar of the JDK
        return (type.getPackage() != null
                        && "Java Runtime Environment"
                                .equalsIgnoreCase(type.getPackage().getImplementationTitle()))
                || type.getName().startsWith("java.")
                || type.getName().startsWith("javax.");
    }

    private static void assertVisibility(Class<?> type) {
        if (!Modifier.isPublic(type.getModifiers())) {
            throw new MockitoException(
                    join(
                            "Cannot create mock for " + type,
                            "",
                            "The type is not public and its mock class is loaded by a different class loader.",
                            "This can have multiple reasons:",
                            " - You are mocking a class with additional interfaces of another class loader",
                            " - Mockito is loaded by a different class loader than the mocked type (e.g. with OSGi)",
                            " - The thread's context class loader is different than the mock's class loader"));
        }
    }
}
