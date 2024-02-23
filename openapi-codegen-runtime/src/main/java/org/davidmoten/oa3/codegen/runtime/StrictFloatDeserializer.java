package org.davidmoten.oa3.codegen.runtime;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import java.io.IOException;

public final class StrictFloatDeserializer extends StdDeserializer<Float> {

    private static final long serialVersionUID = 2207323065789635630L;

    public StrictFloatDeserializer() {
        super(Float.class);
    }

    @Override
    public Float deserialize(JsonParser p, DeserializationContext ctxt)
            throws IOException, JacksonException {
        JsonToken t = p.currentToken();
        if (t == JsonToken.VALUE_NUMBER_INT) {
            return p.getFloatValue();
        } else if (t == JsonToken.VALUE_NUMBER_FLOAT) {
            return p.getFloatValue();
        } else {
            return (Float) ctxt.handleUnexpectedToken(Float.class, p);
        }
    }
}
