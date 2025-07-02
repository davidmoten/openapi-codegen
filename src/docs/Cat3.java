package org.davidmoten.oa3.codegen.test.main.schema;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.github.davidmoten.guavamini.Maps;

import jakarta.annotation.Generated;
import jakarta.annotation.Nonnull;

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

@JsonDeserialize(using = Cat3._Deserializer.class)
@JsonInclude(Include.NON_ABSENT)
@JsonAutoDetect(
        fieldVisibility = JsonAutoDetect.Visibility.ANY,
        creatorVisibility = JsonAutoDetect.Visibility.ANY,
        setterVisibility = JsonAutoDetect.Visibility.ANY)
@Generated(value = "com.github.davidmoten:openapi-codegen-runtime:0.2.2-SNAPSHOT")
public final class Cat3 {

    @JsonUnwrapped
    private final Pet3 pet3;

    @JsonUnwrapped
    private final Detail detail;

    public Cat3(
            @Nonnull Pet3 pet3,
            @Nonnull Detail detail) {
        if (Globals.config().validateInConstructor().test(Cat3.class)) {
            Preconditions.checkNotNull(pet3, "pet3");
            Preconditions.checkNotNull(detail, "detail");
        }
        this.pet3 = pet3;
        this.detail = detail;
    }

    public @Nonnull Pet3 asPet3() {
        return pet3;
    }

    public @Nonnull Detail asDetail() {
        return detail;
    }

    public @Nonnull String petType() {
        return pet3.petType();
    }

    public @Nonnull Optional<Boolean> hunts() {
        return detail.hunts();
    }

    public @Nonnull Optional<Long> age() {
        return detail.age();
    }

    public static @Nonnull Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private Pet3 pet3;
        private Detail detail;

        Builder() {
        }

        public @Nonnull BuilderWithPet3 pet3(@Nonnull Pet3 pet3) {
            this.pet3 = pet3;
            return new BuilderWithPet3(this);
        }
    }

    public static final class BuilderWithPet3 {

        private final Builder b;

        BuilderWithPet3(Builder b) {
            this.b = b;
        }

        public @Nonnull BuilderWithDetail detail(@Nonnull Detail detail) {
            this.b.detail = detail;
            return new BuilderWithDetail(this.b);
        }
    }

    public static final class BuilderWithDetail {

        private final Builder b;

        BuilderWithDetail(Builder b) {
            this.b = b;
        }

        public @Nonnull Cat3 build() {
            return new Cat3(this.b.pet3, this.b.detail);
        }
    }

    public static @Nonnull BuilderWithPet3 pet3(@Nonnull Pet3 pet3) {
        return builder().pet3(pet3);
    }

    @SuppressWarnings("serial")
    public static final class _Deserializer extends PolymorphicDeserializer<Cat3> {

        public _Deserializer() {
            super(Globals.config(), PolymorphicType.ALL_OF, Cat3.class, Pet3.class, Detail.class);
        }
    }

    @JsonInclude(Include.NON_ABSENT)
    @JsonAutoDetect(
            fieldVisibility = JsonAutoDetect.Visibility.ANY,
            creatorVisibility = JsonAutoDetect.Visibility.ANY,
            setterVisibility = JsonAutoDetect.Visibility.ANY)
    public static final class Detail {

        @JsonProperty("hunts")
        private final Optional<Boolean> hunts;

        @JsonProperty("age")
        private final Optional<Long> age;

        @JsonCreator
        public Detail(
                @Nonnull @JsonProperty("hunts") Optional<Boolean> hunts,
                @Nonnull @JsonProperty("age") Optional<Long> age) {
            if (Globals.config().validateInConstructor().test(Detail.class)) {
                Preconditions.checkNotNull(hunts, "hunts");
                Preconditions.checkNotNull(age, "age");
            }
            this.hunts = hunts;
            this.age = age;
        }

        public @Nonnull Optional<Boolean> hunts() {
            return hunts;
        }

        public @Nonnull Optional<Long> age() {
            return age;
        }

        Map<String, Object> _internal_properties() {
            return Maps
                    .put("hunts", (Object) hunts)
                    .put("age", (Object) age)
                    .build();
        }

        public @Nonnull Detail withHunts(@Nonnull Optional<Boolean> hunts) {
            return new Detail(hunts, age);
        }

        public @Nonnull Detail withHunts(@Nonnull boolean hunts) {
            return new Detail(Optional.of(hunts), age);
        }

        public @Nonnull Detail withAge(@Nonnull Optional<Long> age) {
            return new Detail(hunts, age);
        }

        public @Nonnull Detail withAge(@Nonnull long age) {
            return new Detail(hunts, Optional.of(age));
        }

        public static @Nonnull Builder builder() {
            return new Builder();
        }

        public static final class Builder {

            private Optional<Boolean> hunts = Optional.empty();
            private Optional<Long> age = Optional.empty();

            Builder() {
            }

            public @Nonnull Builder hunts(@Nonnull boolean hunts) {
                this.hunts = Optional.of(hunts);
                return this;
            }

            public @Nonnull Builder hunts(@Nonnull Optional<Boolean> hunts) {
                this.hunts = hunts;
                return this;
            }

            public @Nonnull Builder age(@Nonnull long age) {
                this.age = Optional.of(age);
                return this;
            }

            public @Nonnull Builder age(@Nonnull Optional<Long> age) {
                this.age = age;
                return this;
            }

            public @Nonnull Detail build() {
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
                Objects.deepEquals(this.hunts, other.hunts) && 
                Objects.deepEquals(this.age, other.age);
        }

        @Override
        public int hashCode() {
            return Objects.hash(hunts, age);
        }

        @Override
        public @Nonnull String toString() {
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
            Objects.deepEquals(this.pet3, other.pet3) && 
            Objects.deepEquals(this.detail, other.detail);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pet3, detail);
    }

    @Override
    public @Nonnull String toString() {
        return Util.toString(Cat3.class, "pet3", pet3, "detail", detail);
    }
}
