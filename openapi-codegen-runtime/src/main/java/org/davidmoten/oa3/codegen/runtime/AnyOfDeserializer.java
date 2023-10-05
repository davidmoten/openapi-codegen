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

import org.openapitools.jackson.nullable.JsonNullable;

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

public class AnyOfDeserializer<T> extends StdDeserializer<T> {

    private static final long serialVersionUID = -4477865921927841241L;
    
    private final List<AnyOfMember> members;
    private final Class<T> cls;
    private final ObjectMapper mapper;

    protected AnyOfDeserializer(Config config, Class<T> cls, AnyOfMember... members) {
        super(cls);
        this.members = Arrays.asList(members);
        this.cls = cls;
        this.mapper = config.mapper();
    }
    
    @Override
    public T deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
        TreeNode tree = p.getCodec().readTree(p);
        String json = mapper.writeValueAsString(tree);
        return deserializeAnyOf(mapper, json, members, cls);
    }
    
    private static <T> T deserializeAnyOf(ObjectMapper mapper, String json, List<AnyOfMember> members, Class<T> cls) throws JsonMappingException, JsonProcessingException {
        ObjectMapper m = mapper.copy().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        List<Object> list = new ArrayList<>();
        for (AnyOfMember member : members) {
            try {
                Class<?> c = member.cls();
                final Object o;
                // TODO check isObject more efficiently
                if (json.trim().startsWith("{") && isObject(c)) {
                    o = m.readValue(json, c);
                } else {
                    o = mapper.readValue(json, c);
                }
                if (member.nullable()) {
                    list.add(JsonNullable.of(o));
                } else {
                    list.add(Optional.of(o));
                }
            } catch (DatabindException e) {
                list.add(Optional.empty());
            }
        }
        try {
            Constructor<T> con = cls.getDeclaredConstructor(members //
                    .stream() //
                    .map(member -> {
                        if (member.nullable()) {
                            return JsonNullable.class;
                        } else {
                            return Optional.class;
                        }
                    }) //
                    .collect(Collectors.toList()) //
                    .toArray(new Class<?>[] {}));
            con.setAccessible(true);
            return con.newInstance(list.toArray());
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
                | NoSuchMethodException | SecurityException e) {
            throw new RuntimeException(e);
        }
    }
    
    private static final Set<Class<?>> NON_OBJECT_CLASSES = Sets.of(List.class, Map.class, String.class, Short.class,
            Integer.class, Float.class, Double.class, byte[].class, Byte.class, BigInteger.class);

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

}
