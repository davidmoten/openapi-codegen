package org.davidmoten.oa3.codegen.runtime;

import java.util.function.Predicate;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public final class Config {

    private final ObjectMapper mapper;
    private final Predicate<? super Class<?>> validateInConstructor;

    // Use a builder so we can add fields without making a breaking change
    private Config(ObjectMapper mapper, Predicate<? super Class<?>> validateInConstructor) {
        this.mapper = mapper;
        this.validateInConstructor = validateInConstructor;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private ObjectMapper mapper = JsonMapper //
                .builder() //
                .disable(MapperFeature.ALLOW_COERCION_OF_SCALARS) //
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS) //
                .build() //
                .registerModule(new JavaTimeModule());

        private Predicate<? super Class<?>> validateInConstructor = x -> true;

        Builder() {

        }

        public Builder mapper(ObjectMapper mapper) {
            this.mapper = mapper;
            return this;
        }

        public Builder validateInConstructor(Predicate<? super Class<?>> validateInConstructor) {
            this.validateInConstructor = validateInConstructor;
            return this;
        }

        public Config build() {
            return new Config(mapper, validateInConstructor);
        }
    }

    public ObjectMapper mapper() {
        return mapper;
    }

    public Predicate<? super Class<?>> validateInConstructor() {
        return validateInConstructor;
    }

}
