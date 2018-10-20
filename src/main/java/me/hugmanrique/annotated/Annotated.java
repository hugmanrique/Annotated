package me.hugmanrique.annotated;

import me.hugmanrique.annotated.transformer.ClassAnnotationTransformer;
import me.hugmanrique.annotated.transformer.ExecutableAnnotationTransformer;
import me.hugmanrique.annotated.transformer.FieldAnnotationTransformer;

/**
 * Provides utilities to redefine annotations at runtime.
 * Please note this implementation only supports Java 8.
 *
 * @author Hugo Manrique
 * @since 19/10/2018
 */
public final class Annotated {
    private static ClassAnnotationTransformer classTransformer;
    private static FieldAnnotationTransformer fieldTransformer;
    private static ExecutableAnnotationTransformer executableTransformer;

    private Annotated() {}

    /**
     * @return an annotation transformer to modify class annotations
     */
    public static ClassAnnotationTransformer clazz() {
        if (classTransformer == null) {
            classTransformer = new ClassAnnotationTransformer();
        }

        return classTransformer;
    }

    /**
     * @return an annotation transformer to modify field annotations
     */
    public static FieldAnnotationTransformer field() {
        if (fieldTransformer == null) {
            fieldTransformer = new FieldAnnotationTransformer();
        }

        return fieldTransformer;
    }

    /**
     * @return an annotation transformer to modify executable annotations
     * @see #method() alias method
     * @see #constructor() alias method
     */
    public static ExecutableAnnotationTransformer executable() {
        if (executableTransformer == null) {
            executableTransformer = new ExecutableAnnotationTransformer();
        }

        return executableTransformer;
    }

    /**
     * @return an annotation transformer to modify method annotations
     */
    public static ExecutableAnnotationTransformer method() {
        return executable();
    }

    /**
     * @return an annotation transformer to modify constructor annotations
     */
    public static ExecutableAnnotationTransformer constructor() {
        return executable();
    }
}
