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
import com.github.davidmoten.guavamini.annotations.VisibleForTesting;

public class AnyOfSerializer<T> extends StdSerializer<T> {

    private static final long serialVersionUID = -8290019952172548639L;

    private final Class<T> cls;
    private final ObjectMapper mapper;

    protected AnyOfSerializer(Config config, Class<T> cls) {
        super(cls);
        this.cls = cls;
        this.mapper = config.mapper();
    }

    @Override
    public void serialize(T value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        // loop through fields
        List<Optional<?>> values = Arrays.stream(cls.getDeclaredFields()) //
                // ignore any weird fields (jacoco makes a $jacocoData field for example
                .filter(f -> !f.getName().startsWith("$")) //
                .map(f -> {
                    try {
                        f.setAccessible(true);
                        return f.get(value);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                }) //
                .filter(x -> x instanceof Optional) //
                .map(x -> (Optional<?>) x) //
                .collect(Collectors.toList());
        Optional<JsonNode> node = values.stream() //
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
                .collect(Collectors.reducing((a, b) -> merge(a, b)));
        if (!node.isPresent()) {
            throw new IllegalStateException("at least one of the anyOf members must be present");
        } else {
            gen.writeTree(node.get());
        }
    }

    /**
     * Merges b into a and returns modified a. Designed specifically for use with anyOf serialization.
     * 
     * @param a input that will be mutated to contain merged result
     * @param b node to merge into a
     */
    @VisibleForTesting
    static JsonNode merge(JsonNode a, JsonNode b) {
        // do a deep equals check
        if (a.equals(b)) {
            return a;
        } else if (a.getNodeType() != b.getNodeType()) {
            throw new IllegalArgumentException("merge error: mismatching node types: " + a + ", " + b);
        } else if (a.isArray()) {
            ArrayNode x = (ArrayNode) a;
            ArrayNode y = (ArrayNode) b;
            if (x.size() != y.size()) {
                throw new IllegalArgumentException("merge error: array lengths don't match, " + a + ", " + b);
            }
            for (int i = 0; i < x.size(); i++) {
                merge(x.get(i), y.get(i));
            }
        } else if (a.isObject()) {
            Iterator<String> it = b.fieldNames();
            while (it.hasNext()) {
                String fieldName = it.next();
                JsonNode node = a.get(fieldName);
                if (node != null) {
                    merge(node, b.get(fieldName));
                } else {
                    // Overwrite field
                    JsonNode value = b.get(fieldName);
                    ((ObjectNode) a).replace(fieldName, value);
                }
            }
        } else {
            // a not equal to b and are primitives
            // TODO test can merge 1 and 1.0?
            throw new IllegalArgumentException("merge error, fields not equal: " + a + ", " + b);
        }
        return a;
    }
}
