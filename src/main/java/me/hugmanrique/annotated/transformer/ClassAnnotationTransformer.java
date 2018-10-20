package me.hugmanrique.annotated.transformer;

import me.hugmanrique.annotated.AnnotationMap;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * Provides utilities to modify class annotations at runtime.
 * Supports annotations with a {@link ElementType#TYPE} target.
 *
 * @author Hugo Manrique
 * @since 20/10/2018
 */
public final class ClassAnnotationTransformer extends AbstractAnnotationTransformer<Class<?>> {
    private static final Constructor<?> annotationDataConstructor; // Class
    private static final Method annotationDataMethod; // Class
    private static final Field classRedefinedCountField; // Class
    public static final Field annotationsField; // Class.AnnotationData
    public static final Field declaredAnnotationsField; // Class.AnnotationData
    private static final Method casAnnotationDataMethod; // Class.Atomic
    private static final Class<?> atomicClass;

    static {
        try {
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
            classRedefinedCountField = Class.class.getDeclaredField("classRedefinedCount");
            classRedefinedCountField.setAccessible(true);

            // Class.Atomic
            atomicClass = Class.forName("java.lang.Class$Atomic");
            casAnnotationDataMethod = atomicClass.getDeclaredMethod("casAnnotationData", Class.class, annotationDataClass, annotationDataClass);
            casAnnotationDataMethod.setAccessible(true);
        } catch (ClassNotFoundException | NoSuchMethodException | NoSuchFieldException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public <T extends Annotation> void addAnnotation(Class<?> clazz, T annotation) {
        try {
            while (true) { // Retry loop
                int classRedefinedCount = classRedefinedCountField.getInt(clazz);
                Object annotationData = annotationDataMethod.invoke(clazz);

                // We have a null or stale AnnotationData, let's create a new instance
                final Class<T> annotationClass = (Class<T>) annotation.annotationType();
                AnnotationMap annotationMap = new AnnotationMap(annotationData)
                        .addAnnotation(annotationClass, annotation);

                Object newAnnotationData = createAnnotationData(annotationMap, classRedefinedCount);

                if ((boolean) casAnnotationDataMethod.invoke(atomicClass, clazz, annotationData, newAnnotationData)) {
                    // Successfully installed new annotation data
                    break;
                }
            }
        } catch (IllegalAccessException | InvocationTargetException | InstantiationException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public <T extends Annotation> T removeAnnotation(Class<?> clazz, Class<T> annotationClass) {
        if (clazz.getAnnotation(annotationClass) == null) {
            return null;
        }

        try {
            while (true) { // Retry loop
                int classRedefinedCount = classRedefinedCountField.getInt(clazz);
                Object annotationData = annotationDataMethod.invoke(clazz);

                // We have a null or stale AnnotationData, let's create a new instance
                AnnotationMap.AnnotationRemoval removal = new AnnotationMap(annotationData)
                        .removeAnnotation(annotationClass);

                Object newAnnotationData = createAnnotationData(removal.getNewMap(), classRedefinedCount);

                if ((boolean) casAnnotationDataMethod.invoke(atomicClass, clazz, annotationData, newAnnotationData)) {
                    // Successfully installed new annotation data
                    return (T) removal.getRemoved();
                }
            }
        } catch (IllegalAccessException | InvocationTargetException | InstantiationException e) {
            throw new IllegalStateException(e);
        }
    }

    private static Object createAnnotationData(AnnotationMap annotationMap, int classRedefinedCount) throws IllegalAccessException, InvocationTargetException, InstantiationException {
        return annotationDataConstructor.newInstance(
            annotationMap.getAnnotations(),
            annotationMap.getDeclaredAnnotations(),
            classRedefinedCount
        );
    }
}
