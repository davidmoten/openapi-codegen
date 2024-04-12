package org.davidmoten.oa3.codegen.runtime;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.function.Function;

import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.ValueInstantiator;

public class MoreAcceptingValueInstantiator extends ValueInstantiator {

    private final Class<?> cls;
    
    private Constructor<?> doubleConstructor;
    private Constructor<?> integerConstructor;
    private Constructor<?> longConstructor;
    private Constructor<?> floatConstructor;
    private Constructor<?> bigIntegerConstructor;
    private Constructor<?> bigDecimalConstructor;

    public MoreAcceptingValueInstantiator(Class<?> cls) {
        this.cls = cls;
        for (Constructor<?> c : cls.getDeclaredConstructors()) {
            if (c.getParameterCount() == 1) {
                Class<?> parameterType = c.getParameterTypes()[0];
                if (parameterType.equals(double.class) || parameterType.equals(Double.class)) {
                    doubleConstructor = access(c);
                } else if (parameterType.equals(int.class) || parameterType.equals(Integer.class)) {
                    integerConstructor = access(c);
                } else if (parameterType.equals(long.class) || parameterType.equals(Long.class)) {
                    longConstructor = access(c);
                } else if (parameterType.equals(float.class) || parameterType.equals(Float.class)) {
                    floatConstructor = access(c);
                } else if (parameterType.equals(BigInteger.class)) {
                    bigIntegerConstructor = access(c);
                } else if (parameterType.equals(BigDecimal.class)) {
                    bigDecimalConstructor = access(c);
                }
            }
        }
    }
    

    @Override
    protected final void finalize() throws Throwable {
        // spotbugs because constructor throws
    }

    private static Constructor<?> access(Constructor<?> c) {
        c.setAccessible(true);
        return c;
    }

    @Override
    public Object createFromInt(DeserializationContext ctxt, int value) throws IOException {
        if (integerConstructor != null) {
            return createInstance(integerConstructor, value, v -> v, ctxt);
        }
        if (longConstructor != null) {
            return createInstance(longConstructor, value, v -> Long.valueOf(value), ctxt);
        }
        if (doubleConstructor != null) {
            return createInstance(doubleConstructor, value, v -> Double.valueOf(value), ctxt);
        }
        if (bigIntegerConstructor != null) {
            return createInstance(bigIntegerConstructor, value, v -> BigInteger.valueOf(value), ctxt);
        }
        if (bigDecimalConstructor != null) {
            return createInstance(bigDecimalConstructor, value, v -> BigDecimal.valueOf(value), ctxt);
        }
        
        // could lose precision
        
        if (floatConstructor != null) {
            return createInstance(floatConstructor, value, v -> Float.valueOf(value), ctxt);
        }
        return super.createFromInt(ctxt, value);
    }
    
    @Override
    public Object createFromLong(DeserializationContext ctxt, long value) throws IOException {
        if (longConstructor != null) {
            return createInstance(longConstructor, value, v -> Long.valueOf(value), ctxt);
        }
        if (bigIntegerConstructor != null) {
            return createInstance(bigIntegerConstructor, value, v -> BigInteger.valueOf(value), ctxt);
        }
        if (bigDecimalConstructor != null) {
            return createInstance(bigDecimalConstructor, value, v -> BigDecimal.valueOf(value), ctxt);
        }
        
        // could lose precision
        
        if (doubleConstructor != null) {
            return createInstance(doubleConstructor, value, v -> Double.valueOf(value), ctxt);
        }
        if (integerConstructor != null) {
            return createInstance(integerConstructor, value, v -> v, ctxt);
        }
        if (floatConstructor != null) {
            return createInstance(floatConstructor, value, v -> Float.valueOf(value), ctxt);
        }
        return super.createFromLong(ctxt, value);
    }

    private Object createInstance(Constructor<?> constructor, Object arg, Function<Object, Object> argTransformer,
            DeserializationContext ctxt) throws IOException {
        Object a = argTransformer.apply(arg);
        try {
            return constructor.newInstance(a);
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException e) {
            return ctxt.handleInstantiationProblem(cls, arg, e);
        }
    }
}
