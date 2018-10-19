package me.hugmanrique.annotated;

import org.junit.Test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * @author Hugo Manrique
 * @since 19/10/2018
 */
public class ClassAnnotationTest {
    
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @interface DummyAnnotation {
        String value();
    }

    @DummyAnnotation("123")
    class OriginAnnotationClass {}

    class DummyClass {}

    class ElementsClass {}

    private DummyAnnotation getDummyAnnotation(Class<?> clazz) {
        return clazz.getAnnotation(DummyAnnotation.class);
    }

    @Test
    public void testAnnotationInjection() {
        DummyAnnotation annotation = getDummyAnnotation(DummyClass.class);
        assertNull("Class annotation before injection must be null", annotation);

        DummyAnnotation newAnnotation = getDummyAnnotation(OriginAnnotationClass.class);
        Annotated.putAnnotation(DummyClass.class, DummyAnnotation.class, newAnnotation);

        annotation = getDummyAnnotation(DummyClass.class);

        assertNotNull("Class annotation after injection must not be null", annotation);
        assertEquals("Annotation value must be \"123\"", "123", annotation.value());
    }

    @Test
    public void testElementsMap() {
        DummyAnnotation annotation = getDummyAnnotation(ElementsClass.class);

        Map<String, Object> elements = new HashMap<>();
        elements.put("value", "abc");

        Annotated.putAnnotation(ElementsClass.class, DummyAnnotation.class, elements);

        annotation = getDummyAnnotation(ElementsClass.class);

        assertNotNull("Class annotation after injection must not be null", annotation);
        assertEquals("Annotation value must be \"abc\"", "abc", annotation.value());
    }
}
