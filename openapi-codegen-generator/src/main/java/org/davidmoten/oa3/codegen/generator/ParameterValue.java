package org.davidmoten.oa3.codegen.generator;

import java.util.Optional;

public class ParameterValue {

    private final String name;
    private final Optional<Object> value;
    private final ParamType type;

    public ParameterValue(String name, Optional<Object> value, ParamType type) {
        this.name = name;
        this.value = value;
        this.type = type;
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
}
