package org.davidmoten.oa3.generator.runtime.internal;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;

import org.davidmoten.oa3.generator.runtime.Config;

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
    private final ObjectMapper mapper;

    protected PolymorphicDeserializer(Config config, PolymorphicType type, Class<T> cls, Class<?>... classes) {
        super(cls);
        this.type = type;
        this.classes = Arrays.asList(classes);
        this.cls = cls;
        this.mapper = config.mapper();
    }

    @Override
    public T deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        return deserializePoly(mapper, p, ctxt, classes, cls, type);
    }

    @SuppressWarnings("unchecked")
    private static <T> T deserializePoly(ObjectMapper mapper, JsonParser p, DeserializationContext ctxt,
            List<Class<?>> classes, Class<T> cls, PolymorphicType type) throws IOException {
        TreeNode tree = p.getCodec().readTree(p);
        String json = mapper.writeValueAsString(tree);
        if (type == PolymorphicType.ANY_OF) {
            for (Class<?> c : classes) {
                // try to deserialize with each of the member classes
                try {
                    Object o = mapper.readValue(json, (Class<Object>) c);
                    return newInstance(cls, o);
                } catch (DatabindException e) {
                    // ignore because does not match
                }
            }
        } else if (type == PolymorphicType.ONE_OF) {
            T v = null;
            int count = 0;
            for (Class<?> c : classes) {
                // try to deserialize with each of the member classes
                try {
                    // Jackson very permissive with readValue so we will tighten things up a bit
                    if (!c.equals(String.class) || (json.startsWith("\"") && json.endsWith("\""))) {
                        Object o = mapper.readValue(json, c);
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