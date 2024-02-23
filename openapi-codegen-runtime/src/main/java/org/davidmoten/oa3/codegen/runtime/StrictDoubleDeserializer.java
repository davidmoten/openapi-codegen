package org.davidmoten.oa3.codegen.runtime;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import java.io.IOException;

public final class StrictDoubleDeserializer extends StdDeserializer<Double> {

    private static final long serialVersionUID = 5500822592284739392L;

    public StrictDoubleDeserializer() {
        super(Double.class);
    }

    @Override
    public Double deserialize(JsonParser p, DeserializationContext ctxt)
            throws IOException, JacksonException {
        JsonToken t = p.currentToken();
        if (t == JsonToken.VALUE_NUMBER_INT) {
            return p.getDoubleValue();
        } else if (t == JsonToken.VALUE_NUMBER_FLOAT) {
            return p.getDoubleValue();
        } else {
            return (Double) ctxt.handleUnexpectedToken(Double.class, p);
        }
    }
}
