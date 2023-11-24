package org.davidmoten.oa3.codegen.test.main.schema;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.github.davidmoten.guavamini.Maps;

import jakarta.annotation.Generated;

import java.lang.Boolean;
import java.lang.IllegalArgumentException;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.davidmoten.oa3.codegen.runtime.PolymorphicDeserializer;
import org.davidmoten.oa3.codegen.runtime.PolymorphicType;
import org.davidmoten.oa3.codegen.runtime.Preconditions;
import org.davidmoten.oa3.codegen.test.main.Globals;
import org.davidmoten.oa3.codegen.util.Util;

@JsonDeserialize(using = Dog3._Deserializer.class)
@JsonInclude(Include.NON_ABSENT)
@JsonAutoDetect(
        fieldVisibility = JsonAutoDetect.Visibility.ANY,
        creatorVisibility = JsonAutoDetect.Visibility.ANY,
        setterVisibility = JsonAutoDetect.Visibility.ANY)
@Generated(value = "com.github.davidmoten:openapi-codegen-runtime:0.1.15-SNAPSHOT")
public final class Dog3 {

    @JsonUnwrapped
    private final Pet3 pet3;

    @JsonUnwrapped
    private final Detail detail;

    public Dog3(
            Pet3 pet3,
            Detail detail) {
        if (Globals.config().validateInConstructor().test(Dog3.class)) {
            Preconditions.checkNotNull(pet3, "pet3");
            Preconditions.checkNotNull(detail, "detail");
        }
        this.pet3 = pet3;
        this.detail = detail;
    }

    public Pet3 asPet3() {
        return pet3;
    }

    public Detail asDetail() {
        return detail;
    }

    public String petType() {
        return pet3.petType();
    }

    public Optional<Boolean> bark() {
        return detail.bark();
    }

    public Optional<Detail.Breed> breed() {
        return detail.breed();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private Pet3 pet3;
        private Detail detail;

        Builder() {
        }

        public BuilderWithPet3 pet3(Pet3 pet3) {
            this.pet3 = pet3;
            return new BuilderWithPet3(this);
        }
    }

    public static final class BuilderWithPet3 {

        private final Builder b;

        BuilderWithPet3(Builder b) {
            this.b = b;
        }

        public BuilderWithDetail detail(Detail detail) {
            this.b.detail = detail;
            return new BuilderWithDetail(this.b);
        }
    }

    public static final class BuilderWithDetail {

        private final Builder b;

        BuilderWithDetail(Builder b) {
            this.b = b;
        }

        public Dog3 build() {
            return new Dog3(this.b.pet3, this.b.detail);
        }
    }

    public static BuilderWithPet3 pet3(Pet3 pet3) {
        return builder().pet3(pet3);
    }

    @SuppressWarnings("serial")
    public static final class _Deserializer extends PolymorphicDeserializer<Dog3> {

        public _Deserializer() {
            super(Globals.config(), PolymorphicType.ALL_OF, Dog3.class, Pet3.class, Detail.class);
        }
    }

    @JsonInclude(Include.NON_ABSENT)
    @JsonAutoDetect(
            fieldVisibility = JsonAutoDetect.Visibility.ANY,
            creatorVisibility = JsonAutoDetect.Visibility.ANY,
            setterVisibility = JsonAutoDetect.Visibility.ANY)
    public static final class Detail {

        @JsonProperty("bark")
        private final Optional<Boolean> bark;

        @JsonProperty("breed")
        private final Optional<Detail.Breed> breed;

        @JsonCreator
        public Detail(
                @JsonProperty("bark") Optional<Boolean> bark,
                @JsonProperty("breed") Optional<Detail.Breed> breed) {
            if (Globals.config().validateInConstructor().test(Detail.class)) {
                Preconditions.checkNotNull(bark, "bark");
                Preconditions.checkNotNull(breed, "breed");
            }
            this.bark = bark;
            this.breed = breed;
        }

        public Optional<Boolean> bark() {
            return bark;
        }

        public Optional<Detail.Breed> breed() {
            return breed;
        }

        Map<String, Object> _internal_properties() {
            return Maps
                    .put("bark", (Object) bark)
                    .put("breed", (Object) breed)
                    .build();
        }

        public Detail withBark(Optional<Boolean> bark) {
            return new Detail(bark, breed);
        }

        public Detail withBark(boolean bark) {
            return new Detail(Optional.of(bark), breed);
        }

        public Detail withBreed(Optional<Detail.Breed> breed) {
            return new Detail(bark, breed);
        }

        public Detail withBreed(Detail.Breed breed) {
            return new Detail(bark, Optional.of(breed));
        }

        public static Builder builder() {
            return new Builder();
        }

        public static final class Builder {

            private Optional<Boolean> bark = Optional.empty();
            private Optional<Detail.Breed> breed = Optional.empty();

            Builder() {
            }

            public Builder bark(boolean bark) {
                this.bark = Optional.of(bark);
                return this;
            }

            public Builder bark(Optional<Boolean> bark) {
                this.bark = bark;
                return this;
            }

            public Builder breed(Detail.Breed breed) {
                this.breed = Optional.of(breed);
                return this;
            }

            public Builder breed(Optional<Detail.Breed> breed) {
                this.breed = breed;
                return this;
            }

            public Detail build() {
                return new Detail(this.bark, this.breed);
            }
        }

        public enum Breed {

            DINGO("Dingo"),
            HUSKY("Husky"),
            RETRIEVER("Retriever"),
            SHEPHERD("Shepherd");

            @JsonValue
            private final String value;

            Breed(
                    String value) {
                if (Globals.config().validateInConstructor().test(Breed.class)) {
                    Preconditions.checkNotNull(value, "value");
                }
                this.value = value;
            }

            public String value() {
                return value;
            }

            @JsonCreator
            public static Breed fromValue(Object value) {
                for (Breed x: Breed.values()) {
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
            Detail other = (Detail) o;
            return 
                Objects.deepEquals(this.bark, other.bark) && 
                Objects.deepEquals(this.breed, other.breed);
        }

        @Override
        public int hashCode() {
            return Objects.hash(bark, breed);
        }

        @Override
        public String toString() {
            return Util.toString(Detail.class, "bark", bark, "breed", breed);
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
        Dog3 other = (Dog3) o;
        return 
            Objects.deepEquals(this.pet3, other.pet3) && 
            Objects.deepEquals(this.detail, other.detail);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pet3, detail);
    }

    @Override
    public String toString() {
        return Util.toString(Dog3.class, "pet3", pet3, "detail", detail);
    }
}
