package org.davidmoten.oa3.codegen.runtime;

import java.io.IOException;
import java.util.Optional;

import org.davidmoten.oa3.codegen.util.Util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

public class OptionalOctetsSerializer extends StdSerializer<Optional<byte[]>> {

    private static final long serialVersionUID = 261910648881567493L;

    @SuppressWarnings("unchecked")
    public OptionalOctetsSerializer() {
        super((Class<Optional<byte[]>>) (Class<?>) Optional.class);
    }

    @Override
    public void serialize(Optional<byte[]> value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        if (value.isPresent()) {
            gen.writeString(Util.encodeOctets(value.get()));
        } else {
            gen.writeNull();
        }
    }
}
