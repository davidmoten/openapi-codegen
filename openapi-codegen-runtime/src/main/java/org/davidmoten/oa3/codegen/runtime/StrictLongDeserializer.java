package org.davidmoten.oa3.codegen.runtime;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import java.io.IOException;

public final class StrictLongDeserializer extends StdDeserializer<Long> {

    private static final long serialVersionUID = -2721538755854421632L;

    public StrictLongDeserializer() {
        super(Long.class);
    }

    @Override
    public Long deserialize(JsonParser p, DeserializationContext ctxt)
            throws IOException, JacksonException {
        JsonToken t = p.currentToken();
        if (t == JsonToken.VALUE_NUMBER_INT) {
            return p.getLongValue();
        } else {
            return (Long) ctxt.handleUnexpectedToken(Long.class, p);
        }
    }
}
