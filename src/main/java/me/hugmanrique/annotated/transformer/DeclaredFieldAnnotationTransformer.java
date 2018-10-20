package me.hugmanrique.annotated.transformer;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Provides a generic implementation for {@link Member} types that have
 * an internal {@code declaredAnnotations} field.
 *
 * @param <K> the annotated element to be transformed
 * @author Hugo Manrique
 * @since 20/10/2018
 */
@SuppressWarnings("unchecked")
public class DeclaredFieldAnnotationTransformer<K extends AnnotatedElement> extends AbstractAnnotationTransformer<K> {
    private final Field declaredAnnotationsField;

    protected DeclaredFieldAnnotationTransformer(Field declaredAnnotationsField) {
        this.declaredAnnotationsField = Objects.requireNonNull(declaredAnnotationsField);
    }

    @Override
    public <T extends Annotation> void addAnnotation(K member, T annotation) {
        forceMapInitialization(member);

        try {
            Map<Class<? extends Annotation>, Annotation> annotations = (Map<Class<? extends Annotation>, Annotation>) declaredAnnotationsField.get(member);

            if (isEmptyMap(annotations)) {
                annotations = new HashMap<>();

                declaredAnnotationsField.set(member, annotations);
            }

            annotations.put(annotation.annotationType(), annotation);
            System.out.println();
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public <T extends Annotation> T removeAnnotation(K member, Class<T> annotationClass) {
        if (member.getAnnotation(annotationClass) == null) {
            return null;
        }

        forceMapInitialization(member);

        try {
            Map<Class<? extends Annotation>, Annotation> annotations = (Map<Class<? extends Annotation>, Annotation>) declaredAnnotationsField.get(member);

            return (T) annotations.remove(annotationClass);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Forces the initialization of the declaredAnnotations map of this member
     */
    private void forceMapInitialization(K member) {
        member.getAnnotation(Annotation.class);
    }

    private static boolean isEmptyMap(Map<?, ?> map) {
        return map.getClass() == Collections.EMPTY_MAP.getClass();
    }
}
