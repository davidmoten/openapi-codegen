package org.davidmoten.oa3.codegen.runtime;

import java.io.IOException;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

public class NullEnumDeserializer<T> extends StdDeserializer<T> {

    private static final long serialVersionUID = 689714537246694439L;
    
    private final Class<T> cls;
    private final T nullValue;

    public NullEnumDeserializer(Class<T> cls, T nullValue) {
        super(cls);
        this.cls = cls;
        this.nullValue = nullValue;
    }

    @Override
    public T deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
        return p.readValueAs(cls);
    }

    @Override
    public T getNullValue(DeserializationContext ctxt) throws JsonMappingException {
        return nullValue;
    }

}