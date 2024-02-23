package org.davidmoten.oa3.codegen.runtime;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import java.io.IOException;

public final class StrictBooleanDeserializer extends StdDeserializer<Boolean> {

    private static final long serialVersionUID = 6014987192625841276L;

    public StrictBooleanDeserializer() {
        super(Boolean.class);
    }

    @Override
    public Boolean deserialize(JsonParser p, DeserializationContext ctxt)
            throws IOException, JacksonException {
        JsonToken t = p.currentToken();
        if (t == JsonToken.VALUE_TRUE) {
            return true;
        } else if (t == JsonToken.VALUE_FALSE) {
            return false;
        } else {
            return (Boolean) ctxt.handleUnexpectedToken(Boolean.class, p);
        }
    }
}
