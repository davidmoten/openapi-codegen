package org.davidmoten.openapi.v3.runtime;

import java.util.HashMap;
import java.util.Map;

public final class Classes {
    
    public static Builder add(String name, Class<?> cls) {
        return new Builder().add(name, cls);
    }

    public static final class Builder {

        private final Map<String, Class<?>> map = new HashMap<>();

        private Builder() {
            
        }
        
        public Builder add(String name, Class<?> cls) {
            map.put(name, cls);
            return this;
        }
        
        public Map<String, Class<?>> build() {
            return map;
        }
        
    }
}
