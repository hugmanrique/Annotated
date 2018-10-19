package me.hugmanrique.annotated;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Hugo Manrique
 * @since 19/10/2018
 */
class ElementUtil {
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

    @SuppressWarnings("unchecked")
    public static <T extends Annotation> T annotationForMap(final Class<T> annotationClass, final Map<String, Object> elementsMap) {
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
