package org.davidmoten.oa3.codegen.runtime;

import java.io.IOException;

import org.davidmoten.oa3.codegen.util.Util;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

public class OctetsDeserializer extends StdDeserializer<Object> {
    
    public OctetsDeserializer() {
        super(Object.class);
    }

    private static final long serialVersionUID = 6046219949568656810L;

    @Override
    public byte[] deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
        return Util.decodeOctets(p.getText());
    }

}
