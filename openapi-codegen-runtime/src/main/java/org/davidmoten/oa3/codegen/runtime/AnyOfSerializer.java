package org.davidmoten.oa3.codegen.runtime;

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

public class AnyOfSerializer<T> extends StdSerializer<T> {

    private static final long serialVersionUID = -8290019952172548639L;
    
    private final Class<T> cls;
    private final ObjectMapper mapper;

    protected AnyOfSerializer(Config config, Class<T> cls, Class<?>... classes) {
        super(cls);
        this.cls = cls;
        this.mapper = config.mapper();
    }

    @Override
    public void serialize(T value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        // loop through fields
        List<Optional<?>> values = Arrays.stream(cls.getFields()) //
                .map(f -> {
                    try {
                        return (Optional<?>) f.get(value);
                    } catch (IllegalArgumentException | IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                }) //
                .collect(Collectors.toList());
        JsonNode node = values.stream() //
                .filter(Optional::isPresent) //
                .map(x -> {
                    try {
                        return mapper.writeValueAsString(x);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                }) //
                .map(x -> {
                    try {
                        return mapper.readTree(x);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                }) //
                .collect(Collectors.reducing((a, b) -> merge(a, b))).get();
        gen.writeTree(node);
    }

    /**
     * Merges b into a. Only works properly with anyOf serialization
     * 
     * @param a input that will be mutated to contain merged result
     * @param b node to merge into a
     */
    private static JsonNode merge(JsonNode a, JsonNode b) {
        if (a == null || b == null || !a.isArray() && !a.isObject()) {
            return a;
        }
        if (a.isArray()) {
            if (!b.isArray()) {
                // shouldn't happen because identical paths are supposed to have the
                // same values (but possibly objects that are extended with other properties)
                throw new IllegalStateException("unexpected");
            }
            ArrayNode x = (ArrayNode) a;
            ArrayNode y = (ArrayNode) b;
            if (x.size() != y.size()) {
                // shouldn't happen
                throw new IllegalStateException("array lengths don't match");
            }
            for (int i = 0; i < x.size(); i++) {
                merge(x.get(i), y.get(i));
            }
        }
        if (a.isObject() && b.isObject()) {
            Iterator<String> fieldNames = b.fieldNames();
            while (fieldNames.hasNext()) {
                String fieldName = fieldNames.next();
                JsonNode node = a.get(fieldName);
                // if field exists and is an embedded objects
                if (node != null) {
                    merge(node, b.get(fieldName));
                } else {
                    // Overwrite field
                    JsonNode value = b.get(fieldName);
                    ((ObjectNode) a).replace(fieldName, value);
                }
            }
        }
        return a;
    }
}
