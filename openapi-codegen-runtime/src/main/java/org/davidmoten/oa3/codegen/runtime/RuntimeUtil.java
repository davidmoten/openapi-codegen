package org.davidmoten.oa3.codegen.runtime;

import com.fasterxml.jackson.core.JsonProcessingException;

public final class RuntimeUtil {
    
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
}
