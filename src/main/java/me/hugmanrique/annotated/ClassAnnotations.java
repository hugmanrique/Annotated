package me.hugmanrique.annotated;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import static me.hugmanrique.annotated.ElementUtil.annotationForMap;

/**
 * Provides utilities to modify class annotations at runtime.
 *
 * @author Hugo Manrique
 * @since 19/10/2018
 */
public class ClassAnnotations {
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

    private ClassAnnotations() {}

    /**
     * Creates an annotation with the passed elements and adds it to the passed class.
     *
     * @param clazz the class the annotation will be added to
     * @param annotationClass the anotation type
     * @param elementsMap the named elements key-value representation
     * @throws IllegalStateException if a reflection exception occurs
     */
    public static <T extends Annotation> void addAnnotation(Class<?> clazz, Class<T> annotationClass, Map<String, Object> elementsMap) {
        addAnnotation(clazz, annotationForMap(annotationClass, elementsMap));

    }

    /**
     * Adds the annotation to the passed class.
     *
     * @param clazz the class the annotation will be added to
     * @param annotation the annotation instance
     * @throws IllegalStateException if a reflection exception occurs
     */
    public static <T extends Annotation> void addAnnotation(Class<?> clazz, T annotation) {
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

    /**
     * Removes the annotation with the {@code annotationClass} type from the passed class.
     *
     * @param clazz the class the annotation will be removed from
     * @param annotationClass the annotation type to be removed
     * @return the removed annotation, or {@code null} if not present
     */
    @SuppressWarnings("unchecked")
    public static <T extends Annotation> T removeAnnotation(Class<?> clazz, Class<T> annotationClass) {
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
