package org.davidmoten.oa3.codegen.runtime;

import java.time.LocalDate;
import java.time.OffsetDateTime;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.module.SimpleModule;

public final class Modules {

    public static final Module STRICT_DESERIALIZERS = createStrictDeserializersModule();

    private static Module createStrictDeserializersModule() {
        SimpleModule m = new SimpleModule();
        m.addDeserializer(Boolean.class, new StrictBooleanDeserializer());
        m.addDeserializer(Short.class, new StrictShortDeserializer());
        m.addDeserializer(Integer.class, new StrictIntegerDeserializer());
        m.addDeserializer(Long.class, new StrictLongDeserializer());
        m.addDeserializer(Float.class, new StrictFloatDeserializer());
        m.addDeserializer(Double.class, new StrictDoubleDeserializer());
        m.addDeserializer(OffsetDateTime.class, new StrictOffsetDateTimeDeserializer());
        m.addDeserializer(LocalDate.class, new StrictLocalDateDeserializer());
        m.addDeserializer(String.class, new StrictStringDeserializer());
        return m;
    }
}
