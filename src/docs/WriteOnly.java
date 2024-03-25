package org.davidmoten.oa3.codegen.test.main.schema;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.github.davidmoten.guavamini.Maps;

import jakarta.annotation.Generated;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.davidmoten.oa3.codegen.runtime.OptionalEmptyDeserializer;
import org.davidmoten.oa3.codegen.runtime.OptionalMustBePresentConverter;
import org.davidmoten.oa3.codegen.runtime.OptionalMustBePresentOctetsSerializer;
import org.davidmoten.oa3.codegen.runtime.Preconditions;
import org.davidmoten.oa3.codegen.test.main.Globals;
import org.davidmoten.oa3.codegen.util.Util;

@JsonInclude(Include.NON_ABSENT)
@JsonAutoDetect(
        fieldVisibility = JsonAutoDetect.Visibility.ANY,
        creatorVisibility = JsonAutoDetect.Visibility.ANY,
        setterVisibility = JsonAutoDetect.Visibility.ANY)
@Generated(value = "com.github.davidmoten:openapi-codegen-runtime:0.1.18-SNAPSHOT")
public final class WriteOnly {

    @JsonProperty("name")
    private final String name;

    @JsonProperty("writeOnly")
    @JsonSerialize(converter = OptionalMustBePresentConverter.class)
    private final Optional<String> writeOnly;

    @JsonProperty("writeOnlyOptional")
    private final Optional<String> writeOnlyOptional;

    @JsonProperty("writeOnlyOctets")
    @JsonSerialize(using = OptionalMustBePresentOctetsSerializer.class)
    private final Optional<byte[]> writeOnlyOctets;

    @JsonCreator
    public WriteOnly(
            @JsonProperty("name") String name,
            @JsonProperty("writeOnly") @JsonDeserialize(using = OptionalEmptyDeserializer.class) Optional<String> writeOnly,
            @JsonProperty("writeOnlyOptional") @JsonDeserialize(using = OptionalEmptyDeserializer.class) Optional<String> writeOnlyOptional,
            @JsonProperty("writeOnlyOctets") @JsonDeserialize(using = OptionalEmptyDeserializer.class) Optional<byte[]> writeOnlyOctets) {
        if (Globals.config().validateInConstructor().test(WriteOnly.class)) {
            Preconditions.checkNotNull(name, "name");
            Preconditions.checkNotNull(writeOnly, "writeOnly");
            Preconditions.checkNotNull(writeOnlyOptional, "writeOnlyOptional");
            Preconditions.checkNotNull(writeOnlyOctets, "writeOnlyOctets");
        }
        this.name = name;
        this.writeOnly = writeOnly;
        this.writeOnlyOptional = writeOnlyOptional;
        this.writeOnlyOctets = writeOnlyOctets;
    }

    public String name() {
        return name;
    }

    public Optional<String> writeOnly() {
        return writeOnly;
    }

    public Optional<String> writeOnlyOptional() {
        return writeOnlyOptional;
    }

    public Optional<byte[]> writeOnlyOctets() {
        return writeOnlyOctets;
    }

    Map<String, Object> _internal_properties() {
        return Maps
                .put("name", (Object) name)
                .put("writeOnly", (Object) writeOnly)
                .put("writeOnlyOptional", (Object) writeOnlyOptional)
                .put("writeOnlyOctets", (Object) writeOnlyOctets)
                .build();
    }

    public WriteOnly withName(String name) {
        return new WriteOnly(name, writeOnly, writeOnlyOptional, writeOnlyOctets);
    }

    public WriteOnly withWriteOnly(Optional<String> writeOnly) {
        return new WriteOnly(name, writeOnly, writeOnlyOptional, writeOnlyOctets);
    }

    public WriteOnly withWriteOnlyOptional(Optional<String> writeOnlyOptional) {
        return new WriteOnly(name, writeOnly, writeOnlyOptional, writeOnlyOctets);
    }

    public WriteOnly withWriteOnlyOptional(String writeOnlyOptional) {
        return new WriteOnly(name, writeOnly, Optional.of(writeOnlyOptional), writeOnlyOctets);
    }

    public WriteOnly withWriteOnlyOctets(Optional<byte[]> writeOnlyOctets) {
        return new WriteOnly(name, writeOnly, writeOnlyOptional, writeOnlyOctets);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private String name;
        private Optional<String> writeOnly = Optional.empty();
        private Optional<String> writeOnlyOptional = Optional.empty();
        private Optional<byte[]> writeOnlyOctets = Optional.empty();

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

        public BuilderWithName writeOnly(String writeOnly) {
            this.b.writeOnly = Optional.of(writeOnly);
            return this;
        }

        public BuilderWithName writeOnly(Optional<String> writeOnly) {
            this.b.writeOnly = writeOnly;
            return this;
        }

        public BuilderWithName writeOnlyOptional(String writeOnlyOptional) {
            this.b.writeOnlyOptional = Optional.of(writeOnlyOptional);
            return this;
        }

        public BuilderWithName writeOnlyOptional(Optional<String> writeOnlyOptional) {
            this.b.writeOnlyOptional = writeOnlyOptional;
            return this;
        }

        public BuilderWithName writeOnlyOctets(byte[] writeOnlyOctets) {
            this.b.writeOnlyOctets = Optional.of(writeOnlyOctets);
            return this;
        }

        public BuilderWithName writeOnlyOctets(Optional<byte[]> writeOnlyOctets) {
            this.b.writeOnlyOctets = writeOnlyOctets;
            return this;
        }

        public WriteOnly build() {
            return new WriteOnly(this.b.name, this.b.writeOnly, this.b.writeOnlyOptional, this.b.writeOnlyOctets);
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
        WriteOnly other = (WriteOnly) o;
        return 
            Objects.deepEquals(this.name, other.name) && 
            Objects.deepEquals(this.writeOnly, other.writeOnly) && 
            Objects.deepEquals(this.writeOnlyOptional, other.writeOnlyOptional) && 
            Objects.deepEquals(this.writeOnlyOctets, other.writeOnlyOctets);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                name, 
                writeOnly, 
                writeOnlyOptional, 
                writeOnlyOctets);
    }

    @Override
    public String toString() {
        return Util.toString(WriteOnly.class,
                "name", name,
                "writeOnly", writeOnly,
                "writeOnlyOptional", writeOnlyOptional,
                "writeOnlyOctets", writeOnlyOctets);
    }
}
