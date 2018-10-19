package me.hugmanrique.annotated;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static me.hugmanrique.annotated.ElementUtil.annotationForMap;

/**
 * Provides utilities to modify field annotations at runtime.
 *
 * @author Hugo Manrique
 * @since 19/10/2018
 */
@SuppressWarnings("unchecked")
public class FieldAnnotations {
    private static final Field declaredAnnotationsField;

    static {
        try {
            declaredAnnotationsField = Field.class.getDeclaredField("declaredAnnotations");
            declaredAnnotationsField.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw new IllegalStateException(e);
        }
    }

    private FieldAnnotations() {}

    /**
     * Creates an annotation with the passed elements and adds it to the passed field.
     *
     * @param field the field the annotation will be added to
     * @param annotationClass the annotation type
     * @param elementsMap the named elements key-value representation
     * @throws IllegalStateException if a reflection exception occurs
     */
    public static <T extends Annotation> void addAnnotation(Field field, Class<T> annotationClass, Map<String, Object> elementsMap) {
        addAnnotation(field, annotationForMap(annotationClass, elementsMap));
    }

    /**
     * Adds the annotation to the passed field.
     *
     * @param field the field the annotation will be added to
     * @param annotation the annotation instance
     * @throws IllegalStateException if a reflection exception occurs
     */
    public static <T extends Annotation> void addAnnotation(Field field, T annotation) {
        forceMapInitialization(field);

        try {
            Map<Class<? extends Annotation>, Annotation> annotations = (Map<Class<? extends Annotation>, Annotation>) declaredAnnotationsField.get(field);

            if (isEmptyMap(annotations)) {
                annotations = new HashMap<>();

                declaredAnnotationsField.set(field, annotation);
            }

            annotations.put(annotation.annotationType(), annotation);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Removes the annotation with the {@code annotationClass} type from the passed field.
     *
     * @param field the field the annotation will be removed from
     * @param annotationClass the annotation type to be removed
     * @return the removed annotation, or {@code null} if not present
     */
    public static <T extends Annotation> T removeAnnotation(Field field, Class<T> annotationClass) {
        if (field.getAnnotation(annotationClass) == null) {
            return null;
        }

        forceMapInitialization(field);

        try {
            Map<Class<? extends Annotation>, Annotation> annotations = (Map<Class<? extends Annotation>, Annotation>) declaredAnnotationsField.get(field);

            return (T) annotations.remove(annotationClass);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Forces the initialization of the declaredAnnotations map of this field.
     */
    private static void forceMapInitialization(Field field) {
        field.getAnnotation(Annotation.class);
    }

    private static boolean isEmptyMap(Map<?, ?> map) {
        return map.getClass() == Collections.EMPTY_MAP.getClass();
    }
}
