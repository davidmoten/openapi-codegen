package org.davidmoten.oa3.codegen.runtime;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public final class MapBuilder<T, S> {
    
    private final Map<String, T> map = new HashMap<>();
    private final S returnObject;
    private final Consumer<Map<String, T>> result;
    
    public MapBuilder(S returnObject, Consumer<Map<String, T>> result) {
        this.returnObject = returnObject;
        this.result = result;
    }
    
    public MapBuilder<T, S> add(String key, T value) {
        map.put(key,  value);
        return this;
    }
    
    public MapBuilder<T, S> addAll(Map<String, T> entries){
        map.putAll(entries);
        return this;
    }
    
    public S buildMap() {
        result.accept(map);
        return returnObject;
    }

}
