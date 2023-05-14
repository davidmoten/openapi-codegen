package org.davidmoten.oa3.codegen.runtime;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public final class DiscriminatorHelper {

    @SuppressWarnings("unchecked")
    public static <T> T value(Class<?> cls, String value) {
        if (cls.isEnum()) {
            for (Object o : cls.getEnumConstants()) {
                if (value.equals(value(o))) {
                    return (T) o;
                }
            }
            throw new IllegalArgumentException(
                    "value could not be converted to enum " + cls.getName() + ": '" + value + "'");
        } else {
            return (T) value;
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T value(Object o) {
        Class<?> cls = o.getClass();
        if (cls.isEnum()) {
            try {
                Method m = cls.getMethod("value");
                return (T) m.invoke(o);
            } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
                    | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        } else {
            return (T) o;
        }
    }

}
