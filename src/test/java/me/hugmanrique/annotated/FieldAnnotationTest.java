package me.hugmanrique.annotated;

import org.junit.Test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;

import static junit.framework.TestCase.fail;
import static org.junit.Assert.*;

/**
 * @author Hugo Manrique
 * @since 19/10/2018
 */
public class FieldAnnotationTest {

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface DummyAnnotation {
        String value();
    }

    class TestClass {
        @DummyAnnotation("123") int value;
        String text;
    }

    private Field getField(String fieldName) {
        try {
            return TestClass.class.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            fail();
            throw new AssertionError(e);
        }
    }

    private DummyAnnotation getAnnotation(Field field) {
        return field.getAnnotation(DummyAnnotation.class);
    }

    @Test
    public void testAnnotationInjection() {
        Field targetField = getField("text");
        DummyAnnotation annotation = getAnnotation(targetField);

        assertNull("Field annotation before injection must be null", annotation);

        Field originField = getField("value");
        DummyAnnotation newAnnotation = getAnnotation(originField);

        Annotated.addAnnotation(targetField, DummyAnnotation.class, newAnnotation);

        annotation = getAnnotation(targetField);

        assertNotNull("Field annotation after injection must not be null", annotation);
        assertEquals("Annotation value must be \"123\"", "123", annotation.value());
    }
}
