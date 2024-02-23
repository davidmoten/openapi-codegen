package org.davidmoten.oa3.codegen.runtime;

import java.util.function.Predicate;

import org.openapitools.jackson.nullable.JsonNullableModule;

import com.fasterxml.jackson.core.StreamReadConstraints;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public final class Config {

    private final ObjectMapper mapper;
    private final Predicate<? super Class<?>> validateInConstructor;
    private final Predicate<? super String> validateInControllerMethod;

    // Use a builder so we can add fields without making a breaking change
    private Config(ObjectMapper mapper, Predicate<? super Class<?>> validateInConstructor,
            Predicate<? super String> validateInControllerMethod) {
        this.mapper = mapper;
        this.validateInConstructor = validateInConstructor;
        this.validateInControllerMethod = validateInControllerMethod;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private ObjectMapper mapper = createObjectMapper();

        private static ObjectMapper createObjectMapper() {
            ObjectMapper mapper = JsonMapper //
                    .builder() //
                    .disable(MapperFeature.ALLOW_COERCION_OF_SCALARS) //
                    .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS) //
                    .build() //
                    .registerModule(new JavaTimeModule()) //
                    .registerModule(new Jdk8Module()) //
                    .registerModule(new JsonNullableModule()) //
                    .registerModule(Modules.STRICT_DESERIALIZERS);
            // in Jackson 2.15 a 5MB limit on streams was introduced. Configure this off
            StreamReadConstraints streamReadConstraints = StreamReadConstraints.builder()
                    .maxStringLength(Integer.MAX_VALUE).build();
            mapper.getFactory().setStreamReadConstraints(streamReadConstraints);
            return mapper;
        }

        private Predicate<? super Class<?>> validateInConstructor = x -> true;
        private Predicate<? super String> validateInControllerMethod = x -> true;

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

        public Builder validateInControllerMethod(Predicate<? super String> validateInControllerMethod) {
            this.validateInControllerMethod = validateInControllerMethod;
            return this;
        }

        public Config build() {
            return new Config(mapper, validateInConstructor, validateInControllerMethod);
        }
    }

    public ObjectMapper mapper() {
        return mapper;
    }

    public Predicate<? super Class<?>> validateInConstructor() {
        return validateInConstructor;
    }

    public Predicate<? super String> validateInControllerMethod() {
        return validateInControllerMethod;
    }

}
