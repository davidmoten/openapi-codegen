package org.davidmoten.openapi.v3.runtime;

import com.fasterxml.jackson.databind.ObjectMapper;

public final class Config {

    private ObjectMapper mapper;
    private boolean validate;

    private Config(ObjectMapper mapper, boolean validate) {
        this.mapper = mapper;
        this.validate = validate;
    }

    public static final Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private ObjectMapper mapper = Mapper.instance();
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
    
    public boolean validate() {
        return validate;
    }

}
