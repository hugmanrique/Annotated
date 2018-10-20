package me.hugmanrique.annotated;

import org.junit.Test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * @author Hugo Manrique
 * @since 20/10/2018
 */
public class AnnotationTransformTest {
    private static final String VALUE_ELEMENT = "value";
    private static final String BEFORE = "123";
    private static final String AFTER = "abc";

    private static final Class<TestAnnotation> ANNOTATION_CLASS = TestAnnotation.class;

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD})
    @interface TestAnnotation {
        String value();
    }

    public static class Animal {
        public String name;
    }

    public static class Dog extends Animal {
        public void woof() {}
    }

    @Test
    public void testClass() {
        final Class<?> clazz = Dog.class;
        TestAnnotation annotation = clazz.getAnnotation(ANNOTATION_CLASS);

        assertNullAnnotation(annotation);

        // Add @TestAnnotation
        Map<String, Object> elementsMap = Collections.singletonMap(VALUE_ELEMENT, BEFORE);

        Annotated.clazz().addAnnotation(clazz, ANNOTATION_CLASS, elementsMap);
        annotation = clazz.getAnnotation(ANNOTATION_CLASS);

        assertNonNullAnnotation(annotation);
        assertAnnotationValueEquals(annotation, BEFORE);

        // Remove @TestAnnotation
        annotation = Annotated.clazz().removeAnnotation(clazz, ANNOTATION_CLASS);
        assertNotNull("Removed annotation must be non-null", annotation);
        assertAnnotationValueEquals(annotation, BEFORE);

        annotation = clazz.getAnnotation(ANNOTATION_CLASS);
        assertNullAnnotation(annotation);
    }

    @Test
    public void testMethod() throws NoSuchMethodException {
        final Method method = Dog.class.getMethod("woof");
        TestAnnotation annotation = method.getAnnotation(ANNOTATION_CLASS);

        assertNullAnnotation(annotation);

        // Add @TestAnnotation
        Map<String, Object> elementsMap = Collections.singletonMap(VALUE_ELEMENT, BEFORE);

        Annotated.method().addAnnotation(method, ANNOTATION_CLASS, elementsMap);
        annotation = method.getAnnotation(ANNOTATION_CLASS);

        assertNonNullAnnotation(annotation);
        assertAnnotationValueEquals(annotation, BEFORE);

        // Remove @TestAnnotation
        annotation = Annotated.method().removeAnnotation(method, ANNOTATION_CLASS);
        assertNonNullAnnotation(annotation);
        assertAnnotationValueEquals(annotation, BEFORE);

        annotation = method.getAnnotation(ANNOTATION_CLASS);
        assertNullAnnotation(annotation);
    }

    @Test
    public void testField() throws NoSuchFieldException {
        final Field field = Dog.class.getField("name");
        TestAnnotation annotation = field.getAnnotation(ANNOTATION_CLASS);

        assertNullAnnotation(annotation);

        // Add @TestAnnotation
        Map<String, Object> elementsMap = Collections.singletonMap(VALUE_ELEMENT, BEFORE);

        Annotated.field().addAnnotation(field, TestAnnotation.class, elementsMap);
        annotation = field.getAnnotation(TestAnnotation.class);

        assertNotNull(annotation);
        assertAnnotationValueEquals(annotation, BEFORE);

        // Remove @TestAnnotation
        annotation = Annotated.field().removeAnnotation(field, ANNOTATION_CLASS);
        assertNonNullAnnotation(annotation);
        assertAnnotationValueEquals(annotation, BEFORE);

        annotation = field.getAnnotation(ANNOTATION_CLASS);
        assertNullAnnotation(annotation);
    }

    private static void assertNullAnnotation(TestAnnotation annotation) {
        assertNull("The TestAnnotation must be null", annotation);
    }

    private static void assertNonNullAnnotation(TestAnnotation annotation) {
        assertNotNull("The TestAnnotation must not be null", annotation);
    }

    private static void assertAnnotationValueEquals(TestAnnotation annotation, String value) {
        assertEquals("Annotation value must be " + value, value, annotation.value());
    }
}
