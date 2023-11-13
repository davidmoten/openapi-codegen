package org.davidmoten.oa3.codegen.runtime;


import java.io.IOException;
import java.util.Optional;

import org.davidmoten.oa3.codegen.util.Util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

public class OptionalOctetsDeserializer extends StdDeserializer<Optional<byte[]>> {

    private static final long serialVersionUID = 1L;

    public OptionalOctetsDeserializer() {
        super(Optional.class);
    }
    
    @Override
    public Optional<byte[]> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        return Optional.ofNullable(p.getText()).map(Util::decodeOctets);
    }

    @Override
    public Object getAbsentValue(DeserializationContext ctxt) {
        return Optional.empty();
    }

    @Override
    public Optional<byte[]> getNullValue(DeserializationContext ctxt) {
        return Optional.empty();
    }

    @Override
    public Object getEmptyValue(DeserializationContext ctxt) {
        return Optional.empty();
    }

}