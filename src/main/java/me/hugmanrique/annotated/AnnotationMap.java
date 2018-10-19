package me.hugmanrique.annotated;

import java.lang.annotation.Annotation;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Immutable representation of annotation maps of a class.
 *
 * @author Hugo Manrique
 * @since 19/10/2018
 */
public class AnnotationMap {
    private final Map<Class<? extends Annotation>, Annotation> annotations;
    private final Map<Class<? extends Annotation>, Annotation> declaredAnnotations;

    @SuppressWarnings("unchecked")
    public AnnotationMap(final Object annotationData) throws IllegalAccessException {
        Objects.requireNonNull(annotationData, "Annotation data");

        this.annotations = (Map<Class<? extends Annotation>, Annotation>) ClassAnnotations.annotationsField.get(annotationData);
        this.declaredAnnotations = (Map<Class<? extends Annotation>, Annotation>) ClassAnnotations.declaredAnnotationsField.get(annotationData);
    }

    public AnnotationMap(Map<Class<? extends Annotation>, Annotation> annotations, Map<Class<? extends Annotation>, Annotation> declaredAnnotations) {
        this.annotations = Objects.requireNonNull(annotations, "annotations");
        this.declaredAnnotations = Objects.requireNonNull(declaredAnnotations, "declared annotations");
    }

    public <T extends Annotation> AnnotationMap addAnnotation(Class<T> annotationClass, T annotation) {
        Map<Class<? extends Annotation>, Annotation> newDeclaredAnnotations = new LinkedHashMap<>(annotations);
        newDeclaredAnnotations.put(annotationClass, annotation);

        Map<Class<? extends Annotation>, Annotation> newAnnotations;

        if (declaredAnnotations == annotations) {
            newAnnotations = newDeclaredAnnotations;
        } else {
            newAnnotations = new LinkedHashMap<>(annotations);
            newAnnotations.put(annotationClass, annotation);
        }

        return new AnnotationMap(newAnnotations, declaredAnnotations);
    }

    @SuppressWarnings("unchecked")
    public <T extends Annotation> AnnotationRemoval removeAnnotation(Class<T> annotationClass) {
        Map<Class<? extends Annotation>, Annotation> newDeclaredAnnotations = new LinkedHashMap<>(annotations);
        T removed = (T) newDeclaredAnnotations.remove(annotationClass);

        Map<Class<? extends Annotation>, Annotation> newAnnotations;

        if (declaredAnnotations == annotations) {
            newAnnotations = newDeclaredAnnotations;
        } else {
            newAnnotations = new LinkedHashMap<>(annotations);
            newAnnotations.remove(annotationClass);
        }

        return new AnnotationRemoval(
            new AnnotationMap(newAnnotations, newDeclaredAnnotations),
            removed
        );
    }

    public Map<Class<? extends Annotation>, Annotation> getAnnotations() {
        return annotations;
    }

    public Map<Class<? extends Annotation>, Annotation> getDeclaredAnnotations() {
        return declaredAnnotations;
    }

    public static class AnnotationRemoval {
        private final AnnotationMap newMap;
        private final Annotation removed;

        private AnnotationRemoval(AnnotationMap newMap, Annotation removed) {
            this.newMap = newMap;
            this.removed = removed;
        }

        public AnnotationMap getNewMap() {
            return newMap;
        }

        public Annotation getRemoved() {
            return removed;
        }
    }
}
