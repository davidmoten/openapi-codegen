package org.davidmoten.oa3.codegen.runtime;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

public final class StrictLocalDateDeserializer extends StdDeserializer<LocalDate> {

    private static final long serialVersionUID = 6014987192625841276L;

    public StrictLocalDateDeserializer() {
        super(LocalDate.class);
    }

    @Override
    public LocalDate deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
        if (p.currentToken() == JsonToken.VALUE_STRING) {
            String text = p.getText();
            try {
                return LocalDate.parse(text, DateTimeFormatter.ISO_LOCAL_DATE);
            } catch (DateTimeParseException e) {
                return (LocalDate) ctxt.handleWeirdStringValue(LocalDate.class, text, e.getMessage());
            }
        } else {
            return (LocalDate) ctxt.handleUnexpectedToken(LocalDate.class, p);
        }
    }
}
