package me.hugmanrique.annotated.transformer;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Hugo Manrique
 * @since 20/10/2018
 */
abstract class AbstractAnnotationTransformer<K extends AnnotatedElement> implements AnnotationTransformer<K> {
    private static final Constructor<?> annotationInvocationHandlerConstructor;

    static {
        try {
            Class<?> annotationInvocationHandlerClass = Class.forName("sun.reflect.annotation.AnnotationInvocationHandler");

            annotationInvocationHandlerConstructor = annotationInvocationHandlerClass.getDeclaredConstructor(Class.class, Map.class);
            annotationInvocationHandlerConstructor.setAccessible(true);
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public <T extends Annotation> void addAnnotation(K element, Class<T> annotationClass, Map<String, Object> elementsMap) {
        addAnnotation(element, annotationForMap(annotationClass, elementsMap));
    }

    @SuppressWarnings("unchecked")
    private static <T extends Annotation> T annotationForMap(final Class<T> annotationClass, final Map<String, Object> elementsMap) {
        return (T) AccessController.doPrivileged((PrivilegedAction<Annotation>) () -> {
            InvocationHandler handler;

            try {
                handler = (InvocationHandler) annotationInvocationHandlerConstructor.newInstance(annotationClass, new HashMap<>(elementsMap));
            } catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
                throw new IllegalStateException(e);
            }

            return (Annotation) Proxy.newProxyInstance(annotationClass.getClassLoader(), new Class[] { annotationClass }, handler);
        });
    }
}
