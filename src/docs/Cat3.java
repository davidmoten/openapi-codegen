package org.davidmoten.oa3.codegen.test.main.schema;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.github.davidmoten.guavamini.Maps;

import jakarta.annotation.Generated;

import java.lang.Boolean;
import java.lang.Long;
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
import org.springframework.boot.context.properties.ConstructorBinding;

@JsonDeserialize(using = Cat3._Deserializer.class)
@JsonInclude(Include.NON_NULL)
@JsonAutoDetect(fieldVisibility = Visibility.ANY, creatorVisibility = Visibility.ANY, setterVisibility = Visibility.ANY)
@Generated(value = "com.github.davidmoten:openapi-codegen-runtime:0.1.9-SNAPSHOT")
public final class Cat3 {

    @JsonUnwrapped
    private final Pet3 pet3;

    @JsonUnwrapped
    private final Detail Detail;

    public Cat3(
            Pet3 pet3,
            Detail Detail) {
        if (Globals.config().validateInConstructor().test(Cat3.class)) {
            Preconditions.checkNotNull(pet3, "pet3");
            Preconditions.checkNotNull(Detail, "Detail");
        }
        this.pet3 = pet3;
        this.Detail = Detail;
    }

    public Pet3 asPet3() {
        return pet3;
    }

    public Detail asDetail() {
        return Detail;
    }

    public String pet_type() {
        return pet3.pet_type();
    }

    public Optional<Boolean> hunts() {
        return Detail.hunts();
    }

    public Optional<Long> age() {
        return Detail.age();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private Pet3 pet3;
        private Detail Detail;

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

        public BuilderWithDetail Detail(Detail Detail) {
            this.b.Detail = Detail;
            return new BuilderWithDetail(this.b);
        }
    }

    public static final class BuilderWithDetail {

        private final Builder b;

        BuilderWithDetail(Builder b) {
            this.b = b;
        }

        public Cat3 build() {
            return new Cat3(this.b.pet3, this.b.Detail);
        }
    }

    public static BuilderWithPet3 pet3(Pet3 pet3) {
        return builder().pet3(pet3);
    }

    @SuppressWarnings("serial")
    public static final class _Deserializer extends PolymorphicDeserializer<Cat3> {

        public _Deserializer() {
            super(Globals.config(), PolymorphicType.ALL_OF, Cat3.class, Pet3.class, Detail.class);
        }
    }

    @JsonInclude(Include.NON_NULL)
    @JsonAutoDetect(fieldVisibility = Visibility.ANY, creatorVisibility = Visibility.ANY, setterVisibility = Visibility.ANY)
    public static final class Detail {

        @JsonProperty("hunts")
        private final Boolean hunts;

        @JsonProperty("age")
        private final Long age;

        @JsonCreator
        private Detail(
                @JsonProperty("hunts") Boolean hunts,
                @JsonProperty("age") Long age) {
            this.hunts = hunts;
            this.age = age;
        }

        @ConstructorBinding
        public Detail(
                Optional<Boolean> hunts,
                Optional<Long> age) {
            if (Globals.config().validateInConstructor().test(Detail.class)) {
                Preconditions.checkNotNull(hunts, "hunts");
                Preconditions.checkNotNull(age, "age");
            }
            this.hunts = hunts.orElse(null);
            this.age = age.orElse(null);
        }

        public Optional<Boolean> hunts() {
            return Optional.ofNullable(hunts);
        }

        public Optional<Long> age() {
            return Optional.ofNullable(age);
        }

        Map<String, Object> _internal_properties() {
            return Maps
                    .put("hunts", (Object) hunts)
                    .put("age", (Object) age)
                    .build();
        }

        public Detail withHunts(Optional<Boolean> hunts) {
            return new Detail(hunts, Optional.ofNullable(age));
        }

        public Detail withHunts(boolean hunts) {
            return new Detail(Optional.of(hunts), Optional.ofNullable(age));
        }

        public Detail withAge(Optional<Long> age) {
            return new Detail(Optional.ofNullable(hunts), age);
        }

        public Detail withAge(long age) {
            return new Detail(Optional.ofNullable(hunts), Optional.of(age));
        }

        public static Builder builder() {
            return new Builder();
        }

        public static final class Builder {

            private Optional<Boolean> hunts = Optional.empty();
            private Optional<Long> age = Optional.empty();

            Builder() {
            }

            public Builder hunts(boolean hunts) {
                this.hunts = Optional.of(hunts);
                return this;
            }

            public Builder hunts(Optional<Boolean> hunts) {
                this.hunts = hunts;
                return this;
            }

            public Builder age(long age) {
                this.age = Optional.of(age);
                return this;
            }

            public Builder age(Optional<Long> age) {
                this.age = age;
                return this;
            }

            public Detail build() {
                return new Detail(this.hunts, this.age);
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
                Objects.equals(this.hunts, other.hunts) && 
                Objects.equals(this.age, other.age);
        }

        @Override
        public int hashCode() {
            return Objects.hash(hunts, age);
        }

        @Override
        public String toString() {
            return Util.toString(Detail.class, "hunts", hunts, "age", age);
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
        Cat3 other = (Cat3) o;
        return 
            Objects.equals(this.pet3, other.pet3) && 
            Objects.equals(this.Detail, other.Detail);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pet3, Detail);
    }

    @Override
    public String toString() {
        return Util.toString(Cat3.class, "pet3", pet3, "Detail", Detail);
    }
}
