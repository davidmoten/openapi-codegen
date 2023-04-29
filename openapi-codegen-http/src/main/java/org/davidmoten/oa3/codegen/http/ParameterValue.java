package org.davidmoten.oa3.codegen.http;

import java.util.Optional;

public final class ParameterValue {

    private final String name;
    private final Optional<Object> value;
    private final ParameterType type;
    private final Optional<String> contentType;

    public ParameterValue(String name, Optional<Object> value, ParameterType type, Optional<String> contentType) {
        this.name = name;
        this.value = value;
        this.type = type;
        this.contentType = contentType;
    }
    
    public static ParameterValue query(String name, Object value) {
        return new ParameterValue(name, Optional.ofNullable(value), ParameterType.QUERY, Optional.empty());
    }
    
    public static ParameterValue path(String name, Object value) {
        return new ParameterValue(name, Optional.ofNullable(value), ParameterType.PATH, Optional.empty());
    }
    
    public static ParameterValue cookie(String name, Object value) {
        return new ParameterValue(name, Optional.ofNullable(value), ParameterType.COOKIE, Optional.empty());
    }

    public static ParameterValue header(String name, Object value) {
        return new ParameterValue(name, Optional.ofNullable(value), ParameterType.HEADER, Optional.empty());
    }
    
    public String name() {
        return name;
    }

    public Optional<Object> value() {
        return value;
    }

    public ParameterType type() {
        return type;
    }

    public Optional<String> contentType() {
        return contentType;
    }
}
