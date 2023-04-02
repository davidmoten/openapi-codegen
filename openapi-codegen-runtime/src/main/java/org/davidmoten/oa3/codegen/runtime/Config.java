package org.davidmoten.oa3.codegen.runtime;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public final class Config {

    private ObjectMapper mapper;
    private boolean validateInConstructor;

    // Use a builder so we can add fields without making a breaking change
    private Config(ObjectMapper mapper, boolean validate) {
        this.mapper = mapper;
        this.validateInConstructor = validate;
    }

    public static final Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private ObjectMapper mapper = JsonMapper //
                .builder() //
                .disable(MapperFeature.ALLOW_COERCION_OF_SCALARS) //
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS) //
                .build() //
                .registerModule(new JavaTimeModule());

        private boolean validate = true;

        Builder() {

        }

        public Builder mapper(ObjectMapper mapper) {
            this.mapper = mapper;
            return this;
        }

        public Builder validate(boolean validate) {
            this.validate = validate;
            return this;
        }

        public Config build() {
            return new Config(mapper, validate);
        }
    }

    public ObjectMapper mapper() {
        return mapper;
    }

    public boolean validateInConstructor() {
        return validateInConstructor;
    }

}
