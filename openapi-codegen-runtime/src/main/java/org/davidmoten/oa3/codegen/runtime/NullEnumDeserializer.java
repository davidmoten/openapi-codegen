package org.davidmoten.oa3.codegen.runtime;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;

public class NullEnumDeserializer<T> extends JsonDeserializer<T> {

    private final Class<T> enumCls;
    private final Class<?> valueCls;
    private final T nullValue;

    public NullEnumDeserializer(Class<T> enumCls, Class<?> valueCls, T nullValue) {
        super();
        this.enumCls = enumCls;
        this.valueCls = valueCls;
        this.nullValue = nullValue;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
        try {
            Object o = p.readValueAs(valueCls);
            Method m = enumCls.getMethod("fromValue", Object.class);
            return (T) m.invoke(null, o);
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public T getNullValue(DeserializationContext ctxt) throws JsonMappingException {
        return nullValue;
    }

}