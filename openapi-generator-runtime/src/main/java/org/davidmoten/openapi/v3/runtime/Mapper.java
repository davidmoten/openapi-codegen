package org.davidmoten.openapi.v3.runtime;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class Mapper {

    private static ObjectMapper MAPPER = JsonMapper //
            .builder() //
            .disable(MapperFeature.ALLOW_COERCION_OF_SCALARS) //
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS) //
            .build() //
            .registerModule(new JavaTimeModule());

    public static final ObjectMapper instance() {
        return MAPPER;
    }

}
