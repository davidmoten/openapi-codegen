package org.davidmoten.oa3.codegen.runtime;

import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public final class RuntimeUtil {
    
    private static final ObjectMapper MAPPER = new ObjectMapper();
    
    private RuntimeUtil() {
        // prevent instantiation
    }
    
    public static void checkCanSerialize(Config config, Object o) {
        try {
            config.mapper().writeValueAsString(o);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e);
        }
    }
    
    @SuppressWarnings("unchecked")
    public static Map<String, Object> toMap(String json) {
        try {
            return MAPPER.readValue(json, Map.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
