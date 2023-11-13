package org.davidmoten.oa3.codegen.runtime;

import java.io.IOException;

import org.davidmoten.oa3.codegen.util.Util;
import org.openapitools.jackson.nullable.JsonNullable;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

public class JsonNullableOctetsSerializer extends StdSerializer<JsonNullable<byte[]>> {

    private static final long serialVersionUID = -3456469151056995154L;

    @SuppressWarnings("unchecked")
    public JsonNullableOctetsSerializer() {
        super((Class<JsonNullable<byte[]>>) (Class<?>) JsonNullable.class);
    }

    @Override
    public void serialize(JsonNullable<byte[]> value, JsonGenerator gen, SerializerProvider provider)
            throws IOException {
        if (value.isPresent()) {
            if (value.get() == null) {
                gen.writeNull();
            } else {
                gen.writeString(Util.encodeOctets(value.get()));
            }
        } else {
            gen.writeOmittedField(gen.getOutputContext().getCurrentName());
        }
    }
}
