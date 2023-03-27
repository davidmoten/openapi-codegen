package org.davidmoten.openapi.v3.runtime;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

public class OneOfDeserializer<T> extends StdDeserializer<T> {
    private static final long serialVersionUID = -4953059872205916149L;
    private final List<Class<?>> classes;
    private final Class<T> cls;

    private static final ObjectMapper m = new ObjectMapper();

    protected OneOfDeserializer(Class<T> cls, List<Class<?>> classes) {
        super(cls);
        this.classes = classes;
        this.cls = cls;
    }

    @Override
    public T deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        return deserializeOneOf(p, ctxt, classes, cls);
    }

    @SuppressWarnings("unchecked")
    private static <T> T deserializeOneOf(JsonParser p, DeserializationContext ctxt, List<Class<?>> classes,
            Class<T> cls) throws IOException {
        TreeNode tree = p.getCodec().readTree(p);
        String json = m.writeValueAsString(tree);
        for (Class<?> c : classes) {
            // try to deserialize with each of the oneOf member classes
            try {
                Object o = m.readValue(json, (Class<Object>) c);
                return newInstance(cls, o);
            } catch (DatabindException e) {
                // does not match
            }
        }
        throw JsonMappingException.from(ctxt, "json did not match any of the possible classes: " + classes);

    }

    @SuppressWarnings("unchecked")
    private static <T> T newInstance(Class<?> cls, Object parameter) {
        try {
            Constructor<?> con = cls.getDeclaredConstructor(Object.class);
            con.setAccessible(true);
            return (T) con.newInstance(parameter);
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
                | NoSuchMethodException | SecurityException e) {
            throw new RuntimeException(e);
        }
    }
}