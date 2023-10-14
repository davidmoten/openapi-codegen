package org.davidmoten.oa3.codegen.test.main.schema;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.github.davidmoten.guavamini.Maps;

import jakarta.annotation.Generated;

import java.lang.Boolean;
import java.lang.IllegalArgumentException;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.davidmoten.oa3.codegen.runtime.Preconditions;
import org.davidmoten.oa3.codegen.test.main.Globals;
import org.davidmoten.oa3.codegen.util.Util;
import org.springframework.boot.context.properties.ConstructorBinding;

@JsonInclude(Include.NON_NULL)
@JsonAutoDetect(fieldVisibility = Visibility.ANY, creatorVisibility = Visibility.ANY, setterVisibility = Visibility.ANY)
@Generated(value = "com.github.davidmoten:openapi-codegen-runtime:0.1.9-SNAPSHOT")
public final class PetByType {

    @JsonProperty("pet_type")
    private final PetType pet_type;

    @JsonProperty("hunts")
    private final Boolean hunts;

    @JsonCreator
    private PetByType(
            @JsonProperty("pet_type") PetType pet_type,
            @JsonProperty("hunts") Boolean hunts) {
        this.pet_type = pet_type;
        this.hunts = hunts;
    }

    @ConstructorBinding
    public PetByType(
            PetType pet_type,
            Optional<Boolean> hunts) {
        if (Globals.config().validateInConstructor().test(PetByType.class)) {
            Preconditions.checkNotNull(pet_type, "pet_type");
            Preconditions.checkNotNull(hunts, "hunts");
        }
        this.pet_type = pet_type;
        this.hunts = hunts.orElse(null);
    }

    public PetType pet_type() {
        return pet_type;
    }

    public Optional<Boolean> hunts() {
        return Optional.ofNullable(hunts);
    }

    Map<String, Object> _internal_properties() {
        return Maps
                .put("pet_type", (Object) pet_type)
                .put("hunts", (Object) hunts)
                .build();
    }

    public PetByType withPet_type(PetType pet_type) {
        return new PetByType(pet_type, Optional.ofNullable(hunts));
    }

    public PetByType withHunts(Optional<Boolean> hunts) {
        return new PetByType(pet_type, hunts);
    }

    public PetByType withHunts(boolean hunts) {
        return new PetByType(pet_type, Optional.of(hunts));
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private PetType pet_type;
        private Optional<Boolean> hunts = Optional.empty();

        Builder() {
        }

        public BuilderWithPet_type pet_type(PetType pet_type) {
            this.pet_type = pet_type;
            return new BuilderWithPet_type(this);
        }
    }

    public static final class BuilderWithPet_type {

        private final Builder b;

        BuilderWithPet_type(Builder b) {
            this.b = b;
        }

        public BuilderWithPet_type hunts(boolean hunts) {
            this.b.hunts = Optional.of(hunts);
            return this;
        }

        public BuilderWithPet_type hunts(Optional<Boolean> hunts) {
            this.b.hunts = hunts;
            return this;
        }

        public PetByType build() {
            return new PetByType(this.b.pet_type, this.b.hunts);
        }
    }

    public static BuilderWithPet_type pet_type(PetType pet_type) {
        return builder().pet_type(pet_type);
    }

    public enum PetType {

        CAT("Cat"),
        DOG("Dog");

        @JsonValue
        private final String value;

        private PetType(
                String value) {
            this.value = value;
        }

        public String value() {
            return value;
        }

        @JsonCreator
        public static PetType fromValue(Object value) {
            for (PetType x: PetType.values()) {
                if (Objects.equals(value, x.value)) {
                    return x;
                }
            }
            throw new IllegalArgumentException("unexpected enum value: '" + value + "'");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PetByType other = (PetByType) o;
        return 
            Objects.equals(this.pet_type, other.pet_type) && 
            Objects.equals(this.hunts, other.hunts);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pet_type, hunts);
    }

    @Override
    public String toString() {
        return Util.toString(PetByType.class, "pet_type", pet_type, "hunts", hunts);
    }
}
