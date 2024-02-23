package org.davidmoten.oa3.codegen.runtime;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

public final class StrictOffsetDateTimeDeserializer extends StdDeserializer<OffsetDateTime> {

    private static final long serialVersionUID = 6014987192625841276L;

    public StrictOffsetDateTimeDeserializer() {
        super(OffsetDateTime.class);
    }

    @Override
    public OffsetDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
        if (p.currentToken() == JsonToken.VALUE_STRING) {
            String text = p.getText();
            try {
                return OffsetDateTime.parse(text, DateTimeFormatter.ISO_DATE_TIME);
            } catch (DateTimeParseException e) {
                return (OffsetDateTime) ctxt.handleWeirdStringValue(OffsetDateTime.class, text, e.getMessage());
            }
        } else {
            return (OffsetDateTime) ctxt.handleUnexpectedToken(OffsetDateTime.class, p);
        }
    }
}
