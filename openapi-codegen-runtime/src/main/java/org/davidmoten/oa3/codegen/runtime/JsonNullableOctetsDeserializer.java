package org.davidmoten.oa3.codegen.runtime;


import java.io.IOException;

import org.davidmoten.oa3.codegen.util.Util;
import org.openapitools.jackson.nullable.JsonNullable;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

public class JsonNullableOctetsDeserializer extends StdDeserializer<JsonNullable<byte[]>> {

    private static final long serialVersionUID = 1L;

    public JsonNullableOctetsDeserializer() {
        super(JsonNullable.class);
    }

    @Override
    public JsonNullable<byte[]> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonToken t = p.getCurrentToken();
        if (t == JsonToken.VALUE_STRING) {
            String str = p.getText().trim();
            if (str.isEmpty()) {
                return JsonNullable.undefined();
            } else {
                byte[] bytes = Util.decodeOctets(str);
                return JsonNullable.of(bytes);
            }
        } else if (t == JsonToken.VALUE_NULL) {
            return JsonNullable.of(null);
        } else {
            throw new RuntimeException("unexpected");
        }
    }

    @Override
    public Object getAbsentValue(DeserializationContext ctxt) {
        return JsonNullable.undefined();
    }

    @Override
    public JsonNullable<byte[]> getNullValue(DeserializationContext ctxt) {
        return JsonNullable.of(null);
    }

    @Override
    public Object getEmptyValue(DeserializationContext ctxt) {
        return JsonNullable.undefined();
    }

    @Override
    public Boolean supportsUpdate(DeserializationConfig config) {
        // yes; regardless of value deserializer reference itself may be updated
        return Boolean.TRUE;
    }

}