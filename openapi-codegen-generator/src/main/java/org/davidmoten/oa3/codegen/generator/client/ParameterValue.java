package org.davidmoten.oa3.codegen.generator.client;

import java.util.Optional;

import io.swagger.models.ParamType;

public final class ParameterValue {

    private final String name;
    private final Optional<Object> value;
    private final ParamType type;
    private final Optional<String> contentType;

    public ParameterValue(String name, Optional<Object> value, ParamType type, Optional<String> contentType) {
        this.name = name;
        this.value = value;
        this.type = type;
        this.contentType = contentType;
    }

    public String name() {
        return name;
    }

    public Optional<Object> value() {
        return value;
    }

    public ParamType type() {
        return type;
    }

    public Optional<String> contentType() {
        return contentType;
    }
}
