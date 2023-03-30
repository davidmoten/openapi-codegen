package org.davidmoten.openapi.v3.runtime;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

public class PolymorphicDeserializer<T> extends StdDeserializer<T> {

    private static final long serialVersionUID = -4953059872205916149L;

    private final PolymorphicType type;
    private final List<Class<?>> classes;
    private final Class<T> cls;

    private static final ObjectMapper m = new ObjectMapper();

    protected PolymorphicDeserializer(PolymorphicType type, Class<T> cls, Class<?>... classes) {
        super(cls);
        this.type = type;
        this.classes = Arrays.asList(classes);
        this.cls = cls;
    }

    @Override
    public T deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        return deserializePoly(p, ctxt, classes, cls, type);
    }

    @SuppressWarnings("unchecked")
    private static <T> T deserializePoly(JsonParser p, DeserializationContext ctxt, List<Class<?>> classes,
            Class<T> cls, PolymorphicType type) throws IOException {
        TreeNode tree = p.getCodec().readTree(p);
        String json = m.writeValueAsString(tree);
        if (type == PolymorphicType.ANY_OF) {
            for (Class<?> c : classes) {
                // try to deserialize with each of the member classes
                try {
                    Object o = m.readValue(json, (Class<Object>) c);
                    return newInstance(cls, o);
                } catch (DatabindException e) {
                    // ignore because does not match
                }
            }
        } else if (type == PolymorphicType.ONE_OF) {
            T v = null;
            int count = 0;
            System.out.println(classes);
            for (Class<?> c : classes) {
                // try to deserialize with each of the member classes
                try {
                    // Jackson very permissive with readValue so we will tighten things up a bit
                    if (!c.equals(String.class) || (json.startsWith("\"") && json.endsWith("\""))) {
                        Object o = m.readValue(json, c);
                        v = newInstance(cls, o);
                        count++;
                    }
                } catch (DatabindException e) {
                    // ignore because does not match
                }
            }
            if (count == 1) {
                return v;
            } else if (count > 1) {
                throw JsonMappingException.from(ctxt,
                        "json matched more than one of the possible classes: " + classes + ", json=\n" + json);
            }
        }
        throw JsonMappingException.from(ctxt,
                "json did not match any of the possible classes: " + classes + ", json=\n" + json);
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