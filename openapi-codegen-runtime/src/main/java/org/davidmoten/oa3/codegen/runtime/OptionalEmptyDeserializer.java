package org.davidmoten.oa3.codegen.runtime;

import java.io.IOException;
import java.util.Optional;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

public final class OptionalEmptyDeserializer<T> extends StdDeserializer<Optional<T>> {

    private static final long serialVersionUID = 2761605799620605387L;
    
    protected OptionalEmptyDeserializer() {
        super(Optional.class);
    }

    @Override
    public Optional<T> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
        return Optional.empty();
    }

    @Override
    public Optional<T> getNullValue(DeserializationContext ctxt) throws JsonMappingException {
        return Optional.empty();
    }

    @Override
    public Object getAbsentValue(DeserializationContext ctxt) throws JsonMappingException {
        return Optional.empty();
    }

    @Override
    public Object getEmptyValue(DeserializationContext ctxt) throws JsonMappingException {
        return Optional.empty();
    }
}
