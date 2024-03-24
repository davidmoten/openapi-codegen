package org.davidmoten.oa3.codegen.runtime;


import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Optional;

import org.davidmoten.oa3.codegen.util.Util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

public class OptionalPresentOctetsDeserializer extends StdDeserializer<Optional<byte[]>> {

    private static final long serialVersionUID = 1L;

    public OptionalPresentOctetsDeserializer() {
        super(Optional.class);
    }
    
    @Override
    public Optional<byte[]> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        return Optional.ofNullable(p.getText()).map(Util::decodeOctets);
    }

    @Override
    public Object getAbsentValue(DeserializationContext ctxt) {
        return missing(ctxt);
    }

    @Override
    public Optional<byte[]> getNullValue(DeserializationContext ctxt) {
        return missing(ctxt);
    }

    @Override
    public Object getEmptyValue(DeserializationContext ctxt) {
        return missing(ctxt);
    }
    
    @SuppressWarnings("unchecked")
    private static <T> T missing(DeserializationContext ctxt) {
        try {
            return (T) ctxt.handleWeirdStringValue(Optional.class, "nullOrEmptyOrAbsent", "Optional value must be present (required)");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

}