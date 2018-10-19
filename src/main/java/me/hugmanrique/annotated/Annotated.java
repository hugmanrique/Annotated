package me.hugmanrique.annotated;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Provides utilities to redefine annotations at runtime.
 * Please note this implementation only supports Java 8.
 *
 * @author Hugo Manrique
 * @since 19/10/2018
 */
public final class Annotated {
    private static final Constructor<?> annotationInvocationHandlerConstructor;
    private static final Constructor<?> annotationDataConstructor;

    private static final Method annotationDataMethod; // Class
    private static final Field classRedefinedCountMethod; // Class
    private static final Field executableDeclaredAnnotationsField; // Executable
    private static final Field fieldDeclaredAnnotationsField; // Field
    private static final Field annotationsField; // AnnotationData
    private static final Field declaredAnnotationsField; // AnnotationData
    private static final Method casAnnotationDataMethod; // Atomic
    private static final Class<?> atomicClass;

    static {
        // Static initialization of reflection objects
        try {
            // AnnotationInvocationHandler
            Class<?> annotationInvocationHandlerClass = Class.forName("sun.reflect.annotation.AnnotationInvocationHandler");
            annotationInvocationHandlerConstructor = annotationInvocationHandlerClass.getDeclaredConstructor(Class.class, Map.class);
            annotationInvocationHandlerConstructor.setAccessible(true);

            // Class.AnnotationData
            Class<?> annotationDataClass = Class.forName("java.lang.Class$AnnotationData");
            annotationDataConstructor = annotationDataClass.getDeclaredConstructor(Map.class, Map.class, int.class);
            annotationDataConstructor.setAccessible(true);

            annotationsField = annotationDataClass.getDeclaredField("annotations");
            annotationsField.setAccessible(true);
            declaredAnnotationsField = annotationDataClass.getDeclaredField("declaredAnnotations");
            declaredAnnotationsField.setAccessible(true);

            // Class
            annotationDataMethod = Class.class.getDeclaredMethod("annotationData");
            annotationDataMethod.setAccessible(true);
            classRedefinedCountMethod = Class.class.getDeclaredField("classRedefinedCount");
            classRedefinedCountMethod.setAccessible(true);

            // Class.Atomic
            atomicClass = Class.forName("java.lang.Class$Atomic");
            casAnnotationDataMethod = atomicClass.getDeclaredMethod("casAnnotationData", Class.class, annotationDataClass, annotationDataClass);
            casAnnotationDataMethod.setAccessible(true);

            // Executable
            executableDeclaredAnnotationsField = Executable.class.getDeclaredField("declaredAnnotations");
            executableDeclaredAnnotationsField.setAccessible(true);

            // Field
            fieldDeclaredAnnotationsField = Field.class.getDeclaredField("declaredAnnotations");
            fieldDeclaredAnnotationsField.setAccessible(true);
        } catch (ClassNotFoundException | NoSuchMethodException | NoSuchFieldException e) {
            throw new IllegalStateException(e);
        }
    }

    private Annotated() {}

    /**
     * Adds a new {@link Annotation} of type {@code annotationClass} to the passed class.
     *
     * @param clazz the class whose annotations will be modified
     * @param annotationClass the new annotation's type
     * @param elementsMap the named elements {@link Map} representation
     */
    public static <T extends Annotation> void addAnnotation(Class<?> clazz, Class<T> annotationClass, Map<String, Object> elementsMap) {
        addAnnotation(clazz, annotationClass, annotationForMap(annotationClass, elementsMap));
    }

    /**
     * Adds a new {@link Annotation} of type {@code annotationClass} to the passed class.
     *
     * @param clazz the class whose annotations will be modified
     * @param annotationClass the new annotation's type
     * @param annotation the annotation instance that needs to be added to {@code class}
     */
    public static <T extends Annotation> void addAnnotation(Class<?> clazz, Class<T> annotationClass, T annotation) {
        try {
            while (true) { // retry loop
                int classRedefinedCount = classRedefinedCountMethod.getInt(clazz);
                Object annotationData = annotationDataMethod.invoke(clazz);

                // We have a null or stale annotationData, let's create a new instance
                Object newAnnotationData = createAnnotationData(annotationData, annotationClass, annotation, classRedefinedCount);

                if ((boolean) casAnnotationDataMethod.invoke(atomicClass, clazz, annotationData, newAnnotationData)) {
                    // Successfully installed new AnnotationData
                    break;
                }
            }
        } catch (IllegalAccessException | InvocationTargetException | InstantiationException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Adds a new {@link Annotation} of type {@code annotationClass} to the passed field.
     *
     * @param field the field whose annotations will be modified
     * @param annotationClass the new annotation's type
     * @param elementsMap the annotation instance that needs to be added to {@code field}
     */
    public static <T extends Annotation> void addAnnotation(Field field, Class<T> annotationClass, Map<String, Object> elementsMap) {
        addAnnotation(field, annotationClass, annotationForMap(annotationClass, elementsMap));
    }

    /**
     * Adds a new {@link Annotation} of type {@code annotationClass} to the passed field.
     *
     * @param field the field whose annotations will be modified
     * @param annotationClass the new annotation's type
     * @param annotation the annotation instance that needs to be added to {@code field}
     */
    @SuppressWarnings({"unchecked", "ReflectionForUnavailableAnnotation"})
    public static <T extends Annotation> void addAnnotation(Field field, Class<T> annotationClass, T annotation) {
        // Force initialization of declaredAnnotations
        field.getAnnotation(Annotation.class);

        try {
            Map<Class<? extends Annotation>, Annotation> annotations = (Map<Class<? extends Annotation>, Annotation>) fieldDeclaredAnnotationsField.get(field);

            if (annotations.getClass() == Collections.EMPTY_MAP.getClass()) {
                annotations = new HashMap<>();

                fieldDeclaredAnnotationsField.set(field, annotations);
            }

            annotations.put(annotationClass, annotation);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    private static <T extends Annotation> T annotationForMap(final Class<T> annotationClass, final Map<String, Object> elementsMap) {
        return (T) AccessController.doPrivileged((PrivilegedAction<Annotation>) () -> {
            InvocationHandler handler;

            try {
                handler = (InvocationHandler) annotationInvocationHandlerConstructor.newInstance(annotationClass, new HashMap<>(elementsMap));
            } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                throw new IllegalStateException(e);
            }

            return (Annotation) Proxy.newProxyInstance(annotationClass.getClassLoader(), new Class[] { annotationClass }, handler);
        });
    }


    @SuppressWarnings("unchecked")
    private static <T extends Annotation> Object createAnnotationData(Object annotationData, Class<T> annotationClass, T annotation, int classRedefinedCount) throws IllegalAccessException, InvocationTargetException, InstantiationException {
        Map<Class<? extends Annotation>, Annotation> annotations = (Map<Class<? extends Annotation>, Annotation>) annotationsField.get(annotationData);
        Map<Class<? extends Annotation>, Annotation> declaredAnnotations = (Map<Class<? extends Annotation>, Annotation>) declaredAnnotationsField.get(annotationData);

        Map<Class<? extends Annotation>, Annotation> newDeclaredAnnotations = new LinkedHashMap<>(annotations);
        newDeclaredAnnotations.put(annotationClass, annotation);

        Map<Class<? extends Annotation>, Annotation> newAnnotations;

        if (declaredAnnotations == annotations) {
            newAnnotations = newDeclaredAnnotations;
        } else {
            newAnnotations = new LinkedHashMap<>(annotations);
            newAnnotations.put(annotationClass, annotation);
        }

        return annotationDataConstructor.newInstance(newAnnotations, newDeclaredAnnotations, classRedefinedCount);
    }
}
