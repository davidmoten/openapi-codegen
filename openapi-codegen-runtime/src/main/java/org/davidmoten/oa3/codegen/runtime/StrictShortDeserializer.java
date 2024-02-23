package org.davidmoten.oa3.codegen.runtime;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import java.io.IOException;

public final class StrictShortDeserializer extends StdDeserializer<Short> {

    private static final long serialVersionUID = 2162877248512421L;

    public StrictShortDeserializer() {
        super(Short.class);
    }

    @Override
    public Short deserialize(JsonParser p, DeserializationContext ctxt)
            throws IOException, JacksonException {
        JsonToken t = p.currentToken();
        if (t == JsonToken.VALUE_NUMBER_INT) {
            return p.getShortValue();
        } else {
            return (Short) ctxt.handleUnexpectedToken(Short.class, p);
        }
    }
}
