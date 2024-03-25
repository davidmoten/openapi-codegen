package org.davidmoten.oa3.codegen.runtime;

import java.io.IOException;
import java.util.Optional;

import org.davidmoten.oa3.codegen.util.Util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

public final class OptionalMustBePresentOctetsSerializer extends StdSerializer<Optional<byte[]>> {

    private static final long serialVersionUID = -5523809393110583112L;

    @SuppressWarnings("unchecked")
    public OptionalMustBePresentOctetsSerializer() {
        super((Class<Optional<byte[]>>) (Class<?>) Optional.class);
    }

    @Override
    public void serialize(Optional<byte[]> value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        if (value.isPresent()) {
            gen.writeString(Util.encodeOctets(value.get()));
        } else {
            provider.reportMappingProblem("optional value must be present (required writeOnly property)");
        }
    }

}
