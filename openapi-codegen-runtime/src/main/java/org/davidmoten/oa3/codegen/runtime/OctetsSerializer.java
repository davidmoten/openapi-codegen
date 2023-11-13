package org.davidmoten.oa3.codegen.runtime;

import java.io.IOException;

import org.davidmoten.oa3.codegen.util.Util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

public class OctetsSerializer extends StdSerializer<Object> {

    private static final long serialVersionUID = 261910648881567493L;

    public OctetsSerializer() {
        super(Object.class);
    }

    @Override
    public void serialize(Object value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeString(Util.encodeOctets((byte[]) value));
    }

}
