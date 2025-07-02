package org.davidmoten.oa3.codegen.test.main.schema;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.davidmoten.guavamini.Maps;

import jakarta.annotation.Generated;
import jakarta.annotation.Nonnull;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.davidmoten.oa3.codegen.runtime.Preconditions;
import org.davidmoten.oa3.codegen.test.main.Globals;
import org.davidmoten.oa3.codegen.util.Util;

@JsonInclude(Include.NON_ABSENT)
@JsonAutoDetect(
        fieldVisibility = JsonAutoDetect.Visibility.ANY,
        creatorVisibility = JsonAutoDetect.Visibility.ANY,
        setterVisibility = JsonAutoDetect.Visibility.ANY)
@Generated(value = "com.github.davidmoten:openapi-codegen-runtime:0.2.2-SNAPSHOT")
public final class PetByAge {

    @JsonProperty("age")
    private final long age;

    @JsonProperty("nickname")
    private final Optional<String> nickname;

    @JsonCreator
    public PetByAge(
            @Nonnull @JsonProperty("age") long age,
            @Nonnull @JsonProperty("nickname") Optional<String> nickname) {
        if (Globals.config().validateInConstructor().test(PetByAge.class)) {
            Preconditions.checkNotNull(nickname, "nickname");
        }
        this.age = age;
        this.nickname = nickname;
    }

    public @Nonnull long age() {
        return age;
    }

    public @Nonnull Optional<String> nickname() {
        return nickname;
    }

    Map<String, Object> _internal_properties() {
        return Maps
                .put("age", (Object) age)
                .put("nickname", (Object) nickname)
                .build();
    }

    public @Nonnull PetByAge withAge(@Nonnull long age) {
        return new PetByAge(age, nickname);
    }

    public @Nonnull PetByAge withNickname(@Nonnull Optional<String> nickname) {
        return new PetByAge(age, nickname);
    }

    public @Nonnull PetByAge withNickname(@Nonnull String nickname) {
        return new PetByAge(age, Optional.of(nickname));
    }

    public static @Nonnull Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private long age;
        private Optional<String> nickname = Optional.empty();

        Builder() {
        }

        public @Nonnull BuilderWithAge age(@Nonnull long age) {
            this.age = age;
            return new BuilderWithAge(this);
        }
    }

    public static final class BuilderWithAge {

        private final Builder b;

        BuilderWithAge(Builder b) {
            this.b = b;
        }

        public @Nonnull BuilderWithAge nickname(@Nonnull String nickname) {
            this.b.nickname = Optional.of(nickname);
            return this;
        }

        public @Nonnull BuilderWithAge nickname(@Nonnull Optional<String> nickname) {
            this.b.nickname = nickname;
            return this;
        }

        public @Nonnull PetByAge build() {
            return new PetByAge(this.b.age, this.b.nickname);
        }
    }

    public static @Nonnull BuilderWithAge age(@Nonnull long age) {
        return builder().age(age);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PetByAge other = (PetByAge) o;
        return 
            Objects.deepEquals(this.age, other.age) && 
            Objects.deepEquals(this.nickname, other.nickname);
    }

    @Override
    public int hashCode() {
        return Objects.hash(age, nickname);
    }

    @Override
    public @Nonnull String toString() {
        return Util.toString(PetByAge.class, "age", age, "nickname", nickname);
    }
}
