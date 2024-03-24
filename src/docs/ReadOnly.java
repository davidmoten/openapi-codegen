package org.davidmoten.oa3.codegen.test.main.schema;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.github.davidmoten.guavamini.Maps;

import jakarta.annotation.Generated;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.davidmoten.oa3.codegen.runtime.OptionalMustBePresentConverter;
import org.davidmoten.oa3.codegen.runtime.OptionalPresentOctetsDeserializer;
import org.davidmoten.oa3.codegen.runtime.Preconditions;
import org.davidmoten.oa3.codegen.test.main.Globals;
import org.davidmoten.oa3.codegen.util.Util;

@JsonInclude(Include.NON_ABSENT)
@JsonAutoDetect(
        fieldVisibility = JsonAutoDetect.Visibility.ANY,
        creatorVisibility = JsonAutoDetect.Visibility.ANY,
        setterVisibility = JsonAutoDetect.Visibility.ANY)
@Generated(value = "com.github.davidmoten:openapi-codegen-runtime:0.1.18-SNAPSHOT")
public final class ReadOnly {

    @JsonProperty("name")
    private final String name;

    @JsonIgnore
    private final Optional<String> readOnly;

    @JsonIgnore
    private final Optional<String> readOnlyOptional;

    @JsonIgnore
    private final Optional<byte[]> readOnlyOctets;

    @JsonCreator
    public ReadOnly(
            @JsonProperty("name") String name,
            @JsonProperty("readOnly") @JsonDeserialize(converter = OptionalMustBePresentConverter.class) Optional<String> readOnly,
            @JsonProperty("readOnlyOptional") Optional<String> readOnlyOptional,
            @JsonProperty("readOnlyOctets") @JsonDeserialize(using = OptionalPresentOctetsDeserializer.class) Optional<byte[]> readOnlyOctets) {
        if (Globals.config().validateInConstructor().test(ReadOnly.class)) {
            Preconditions.checkNotNull(name, "name");
            Preconditions.checkNotNull(readOnly, "readOnly");
            Preconditions.checkNotNull(readOnlyOptional, "readOnlyOptional");
            Preconditions.checkNotNull(readOnlyOctets, "readOnlyOctets");
        }
        this.name = name;
        this.readOnly = readOnly;
        this.readOnlyOptional = readOnlyOptional;
        this.readOnlyOctets = readOnlyOctets;
    }

    public String name() {
        return name;
    }

    public Optional<String> readOnly() {
        return readOnly;
    }

    public Optional<String> readOnlyOptional() {
        return readOnlyOptional;
    }

    public Optional<byte[]> readOnlyOctets() {
        return readOnlyOctets;
    }

    Map<String, Object> _internal_properties() {
        return Maps
                .put("name", (Object) name)
                .put("readOnly", (Object) readOnly)
                .put("readOnlyOptional", (Object) readOnlyOptional)
                .put("readOnlyOctets", (Object) readOnlyOctets)
                .build();
    }

    public ReadOnly withName(String name) {
        return new ReadOnly(name, readOnly, readOnlyOptional, readOnlyOctets);
    }

    public ReadOnly withReadOnly(Optional<String> readOnly) {
        return new ReadOnly(name, readOnly, readOnlyOptional, readOnlyOctets);
    }

    public ReadOnly withReadOnlyOptional(Optional<String> readOnlyOptional) {
        return new ReadOnly(name, readOnly, readOnlyOptional, readOnlyOctets);
    }

    public ReadOnly withReadOnlyOptional(String readOnlyOptional) {
        return new ReadOnly(name, readOnly, Optional.of(readOnlyOptional), readOnlyOctets);
    }

    public ReadOnly withReadOnlyOctets(Optional<byte[]> readOnlyOctets) {
        return new ReadOnly(name, readOnly, readOnlyOptional, readOnlyOctets);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private String name;
        private Optional<String> readOnly = Optional.empty();
        private Optional<String> readOnlyOptional = Optional.empty();
        private Optional<byte[]> readOnlyOctets = Optional.empty();

        Builder() {
        }

        public BuilderWithName name(String name) {
            this.name = name;
            return new BuilderWithName(this);
        }
    }

    public static final class BuilderWithName {

        private final Builder b;

        BuilderWithName(Builder b) {
            this.b = b;
        }

        public BuilderWithName readOnly(String readOnly) {
            this.b.readOnly = Optional.of(readOnly);
            return this;
        }

        public BuilderWithName readOnly(Optional<String> readOnly) {
            this.b.readOnly = readOnly;
            return this;
        }

        public BuilderWithName readOnlyOptional(String readOnlyOptional) {
            this.b.readOnlyOptional = Optional.of(readOnlyOptional);
            return this;
        }

        public BuilderWithName readOnlyOptional(Optional<String> readOnlyOptional) {
            this.b.readOnlyOptional = readOnlyOptional;
            return this;
        }

        public BuilderWithName readOnlyOctets(byte[] readOnlyOctets) {
            this.b.readOnlyOctets = Optional.of(readOnlyOctets);
            return this;
        }

        public BuilderWithName readOnlyOctets(Optional<byte[]> readOnlyOctets) {
            this.b.readOnlyOctets = readOnlyOctets;
            return this;
        }

        public ReadOnly build() {
            return new ReadOnly(this.b.name, this.b.readOnly, this.b.readOnlyOptional, this.b.readOnlyOctets);
        }
    }

    public static BuilderWithName name(String name) {
        return builder().name(name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ReadOnly other = (ReadOnly) o;
        return 
            Objects.deepEquals(this.name, other.name) && 
            Objects.deepEquals(this.readOnly, other.readOnly) && 
            Objects.deepEquals(this.readOnlyOptional, other.readOnlyOptional) && 
            Objects.deepEquals(this.readOnlyOctets, other.readOnlyOctets);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                name, 
                readOnly, 
                readOnlyOptional, 
                readOnlyOctets);
    }

    @Override
    public String toString() {
        return Util.toString(ReadOnly.class,
                "name", name,
                "readOnly", readOnly,
                "readOnlyOptional", readOnlyOptional,
                "readOnlyOctets", readOnlyOctets);
    }
}
