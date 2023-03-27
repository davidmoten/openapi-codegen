package org.davidmoten.openapi.v3.runtime;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Map.Entry;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

public class OneOfDeserializer<T> extends StdDeserializer<T> {
    private static final long serialVersionUID = -4953059872205916149L;
    private final Map<String, Class<?>> classes;
    private final Class<T> cls;

    private static final ObjectMapper m = new ObjectMapper();

    protected OneOfDeserializer(Class<T> cls, Map<String, Class<?>> classes) {
        super(cls);
        this.classes = classes;
        this.cls = cls;
    }

    @Override
    public T deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        return deserializeOneOf(p, ctxt, classes, cls);
    }

    @SuppressWarnings("unchecked")
    private static <T> T deserializeOneOf(JsonParser p, DeserializationContext ctxt, Map<String, Class<?>> classes,
            Class<T> cls) throws IOException {
        TreeNode tree = p.getCodec().readTree(p);
        Class<?> c = null;
        for (Entry<String, Class<?>> entry : classes.entrySet()) {
            if (tree.get(entry.getKey()) != null) {
                c = entry.getValue();
            }
        }
        if (c == null) {
            throw JsonMappingException.from(ctxt,
                    "json did not match any of the possible classes: " + classes.values());
        }
        String json = m.writeValueAsString(tree);
        Object o = m.readValue(json, (Class<Object>) c);
        try {
            Constructor<T> con = cls.getDeclaredConstructor(Object.class);
            con.setAccessible(true);
            return con.newInstance(o);
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
                | NoSuchMethodException | SecurityException e) {
            throw new RuntimeException("unexpected");
        }
    }
}