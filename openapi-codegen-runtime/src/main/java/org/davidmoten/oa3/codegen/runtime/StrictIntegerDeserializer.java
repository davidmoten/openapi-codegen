package org.davidmoten.oa3.codegen.runtime;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import java.io.IOException;

public final class StrictIntegerDeserializer extends StdDeserializer<Integer> {

    private static final long serialVersionUID = 6079282945607228350L;

    public StrictIntegerDeserializer() {
        super(Integer.class);
    }

    @Override
    public Integer deserialize(JsonParser p, DeserializationContext ctxt)
            throws IOException, JacksonException {
        JsonToken t = p.currentToken();
        if (t == JsonToken.VALUE_NUMBER_INT) {
            return p.getIntValue();
        } else {
            return (Integer) ctxt.handleUnexpectedToken(Integer.class, p);
        }
    }
}
