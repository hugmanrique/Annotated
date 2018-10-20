package me.hugmanrique.annotated.transformer;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.Map;

/**
 * Provides utilities to modify annotations at runtime.
 *
 * @param <K> the annotated element to be transformed
 * @author Hugo Manrique
 * @since 20/10/2018
 */
interface AnnotationTransformer<K extends AnnotatedElement> {

    /**
     * Creates an annotation with the passed elements and adds it to the passed element.
     *
     * @param element the element the annotation will be added to
     * @param annotationClass the annotation type
     * @param elementsMap the named elements key-value representation
     * @throws IllegalStateException if a reflection exception occurs
     */
    <T extends Annotation> void addAnnotation(K element, Class<T> annotationClass, Map<String, Object> elementsMap);

    /**
     * Adds the annotation to the passed element.
     *
     * @param element the element the annotation will be added to
     * @param annotation the annotation type
     * @throws IllegalStateException if a reflection exception occurs
     */
    <T extends Annotation> void addAnnotation(K element, T annotation);

    /**
     * Removes the annotation with type {@code annotationClass} from the passed element.
     *
     * @param element the element the annotation will be removed from
     * @param annotationClass the annotation type to be removed
     * @return the removed annotation, or {@code null} if not present
     * @throws IllegalStateException if a reflection exception occurs
     */
    <T extends Annotation> T removeAnnotation(K element, Class<T> annotationClass);
}
