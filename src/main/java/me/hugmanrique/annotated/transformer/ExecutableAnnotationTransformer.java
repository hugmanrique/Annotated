package me.hugmanrique.annotated.transformer;

import java.lang.annotation.ElementType;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;

/**
 * Provides utilities to modify executable (methods and constructors) annotations at runtime.
 * Supports annotations with {@link ElementType#METHOD} or {@link ElementType#CONSTRUCTOR} targets.
 *
 * @author Hugo Manrique
 * @since 20/10/2018
 */
public final class ExecutableAnnotationTransformer extends DeclaredFieldAnnotationTransformer<Executable> {
    private static final Field declaredAnnotationsField;

    static {
        try {
            declaredAnnotationsField = Executable.class.getDeclaredField("declaredAnnotations");
            declaredAnnotationsField.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw new IllegalStateException(e);
        }
    }

    public ExecutableAnnotationTransformer() {
        super(declaredAnnotationsField);
    }
}
