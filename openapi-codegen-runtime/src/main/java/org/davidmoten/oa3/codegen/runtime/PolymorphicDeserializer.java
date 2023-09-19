package org.davidmoten.oa3.codegen.runtime;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.github.davidmoten.guavamini.Sets;

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

    private static <T> T deserializePoly(ObjectMapper mapper, JsonParser p, DeserializationContext ctxt,
            List<Class<?>> classes, Class<T> cls, PolymorphicType type) throws IOException {
        TreeNode tree = p.getCodec().readTree(p);
        // TODO don't have to generate json because can use tree.traverse to get a
        // parser to read value, perf advantage and can stop plugging in ObjectMapper
        String json = mapper.writeValueAsString(tree);
        if (type == PolymorphicType.ANY_OF) {
            return deserializeAnyOf(mapper, json, classes, cls, ctxt);
        } else if (type == PolymorphicType.ONE_OF) {
            return deserializeOneOf(mapper, json, classes, cls, ctxt);
        } else {
            return deserializeAllOf(mapper, json, classes, cls);
        }
    }

    private static <T> T deserializeOneOf(ObjectMapper mapper, String json, List<Class<?>> classes, Class<T> cls,
            DeserializationContext ctxt) throws JsonProcessingException {
        T v = null;
        int count = 0;
        for (Class<?> c : classes) {
            // try to deserialize with each of the member classes
            // @formatter:off
            try {
                // Jackson very permissive with readValue so we will tighten things up a bit
                if (!c.equals(String.class) || json.startsWith("\"") && json.endsWith("\"")) {
                    Object o = mapper.readValue(json, c);
                    v = newInstance(cls, o);
                    count++;
                }
            } catch (DatabindException e) {} // NOPMD
            // @formatter:on
        }
        if (count == 1) {
            return v;
        } else if (count > 1) {
            throw JsonMappingException.from(ctxt,
                    "json matched more than one of the possible classes: " + classes + ", json=\n" + json);
        }
        throw JsonMappingException.from(ctxt,
                "json did not match any of the possible classes: " + classes + ", json=\n" + json);
    }

    private static <T> T deserializeAllOf(ObjectMapper mapper, String json, List<Class<?>> classes, Class<T> cls)
            throws JsonMappingException, JsonProcessingException {
        ObjectMapper m = mapper.copy().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        List<Object> list = new ArrayList<>();
        for (Class<?> c : classes) {
            list.add(m.readValue(json, c));
        }
        try {
            Constructor<T> con = cls.getDeclaredConstructor(classes.toArray(new Class<?>[] {}));
            con.setAccessible(true);
            return con.newInstance(list.toArray());
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
                | NoSuchMethodException | SecurityException e) {
            throw new RuntimeException(e);
        }
    }

    private static <T> T deserializeAnyOf(ObjectMapper mapper, String json, List<Class<?>> classes, Class<T> cls,
            DeserializationContext ctxt) throws JsonMappingException, JsonProcessingException {
        ObjectMapper m = mapper.copy().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        List<Object> list = new ArrayList<>();
        for (Class<?> c : classes) {
            try {
                final Object o;
                // TODO check isObject more efficiently
                if (json.trim().startsWith("{") && isObject(c)) {
                    o = m.readValue(json, c);
                } else {
                    o = mapper.readValue(json, c);
                }
                list.add(Optional.of(o));
            } catch (JacksonException | IllegalArgumentException e) {
                list.add(Optional.empty());
            }
        }
        try {
            Constructor<T> con = cls.getDeclaredConstructor(classes //
                    .stream() //
                    .map(c -> Optional.class) //
                    .collect(Collectors.toList()) //
                    .toArray(new Class<?>[] {}));
            con.setAccessible(true);
            return con.newInstance(list.toArray());
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
                | NoSuchMethodException | SecurityException e) {
            throw new RuntimeException(e);
        }
    }
    
    private static final Set<Class<?>> NON_OBJECT_CLASSES = Sets.newHashSet(List.class, Map.class, String.class, Short.class, Integer.class, Float.class, Double.class, byte[].class, Byte.class, BigInteger.class);

    private static boolean isObject(Class<?> c) {
        if (c.isPrimitive() || NON_OBJECT_CLASSES.contains(c)) {
            return false;
        }
        for (Field f : c.getDeclaredFields()) {
            if (f.isAnnotationPresent(JsonValue.class)) {
                return false;
            }
        }
        return true;
    }

    private static <T> T newInstance(Class<T> cls, Object parameter) {
        try {
            Constructor<T> con = cls.getDeclaredConstructor(Object.class);
            con.setAccessible(true);
            return con.newInstance(parameter);
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
                | NoSuchMethodException | SecurityException e) {
            throw new RuntimeException(e);
        }
    }
}