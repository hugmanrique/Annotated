package me.hugmanrique.annotated.transformer;

import java.lang.annotation.ElementType;
import java.lang.reflect.Field;

/**
 * Provides utilities to modify field annotations at runtime.
 * Supports annotations with a {@link ElementType#FIELD} target.
 *
 * @author Hugo Manrique
 * @since 20/10/2018
 */
public final class FieldAnnotationTransformer extends DeclaredFieldAnnotationTransformer<Field> {
    private static final Field declaredAnnotationsField;

    static {
        try {
            declaredAnnotationsField = Field.class.getDeclaredField("declaredAnnotations");
            declaredAnnotationsField.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw new IllegalStateException(e);
        }
    }

    public FieldAnnotationTransformer() {
        super(declaredAnnotationsField);
    }
}
