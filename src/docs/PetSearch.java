package org.davidmoten.oa3.codegen.test.main.schema;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import jakarta.annotation.Generated;
import jakarta.annotation.Nonnull;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.Objects;
import java.util.Optional;

import org.davidmoten.oa3.codegen.runtime.AnyOfDeserializer;
import org.davidmoten.oa3.codegen.runtime.AnyOfMember;
import org.davidmoten.oa3.codegen.runtime.AnyOfSerializer;
import org.davidmoten.oa3.codegen.runtime.Preconditions;
import org.davidmoten.oa3.codegen.runtime.RuntimeUtil;
import org.davidmoten.oa3.codegen.test.main.Globals;
import org.davidmoten.oa3.codegen.util.Util;

@JsonDeserialize(using = PetSearch._Deserializer.class)
@JsonSerialize(using = PetSearch._Serializer.class)
@Generated(value = "com.github.davidmoten:openapi-codegen-runtime:0.2.2-SNAPSHOT")
public final class PetSearch {

    private final Optional<PetByAge> petByAge;

    private final Optional<PetByType> petByType;

    private PetSearch(
            @Nonnull Optional<PetByAge> petByAge,
            @Nonnull Optional<PetByType> petByType) {
        if (Globals.config().validateInConstructor().test(PetSearch.class)) {
            Preconditions.checkNotNull(petByAge, "petByAge");
            Preconditions.checkNotNull(petByType, "petByType");
        }
        this.petByAge = petByAge;
        this.petByType = petByType;
    }

    public static @Nonnull PetSearch of(
            @Nonnull Optional<PetByAge> petByAge,
            @Nonnull Optional<PetByType> petByType) {
        PetSearch $o = new PetSearch(petByAge, petByType);
        RuntimeUtil.checkCanSerialize(Globals.config(), $o);
        return $o;
    }

    public @Nonnull Optional<PetByAge> petByAge() {
        return petByAge;
    }

    public @Nonnull Optional<PetByType> petByType() {
        return petByType;
    }

    public static @Nonnull Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private Optional<PetByAge> petByAge = Optional.empty();
        private Optional<PetByType> petByType = Optional.empty();

        Builder() {
        }

        public @Nonnull Builder petByAge(@Nonnull PetByAge petByAge) {
            this.petByAge = Optional.of(petByAge);
            return this;
        }

        public @Nonnull Builder petByAge(@Nonnull Optional<PetByAge> petByAge) {
            this.petByAge = petByAge;
            return this;
        }

        public @Nonnull Builder petByType(@Nonnull PetByType petByType) {
            this.petByType = Optional.of(petByType);
            return this;
        }

        public @Nonnull Builder petByType(@Nonnull Optional<PetByType> petByType) {
            this.petByType = petByType;
            return this;
        }

        public @Nonnull PetSearch build() {
            return PetSearch.of(this.petByAge, this.petByType);
        }
    }

    @SuppressWarnings("serial")
    public static final class _Deserializer extends AnyOfDeserializer<PetSearch> {

        public _Deserializer() {
            super(Globals.config(), PetSearch.class, AnyOfMember.nonNullable(PetByAge.class), AnyOfMember.nonNullable(PetByType.class));
        }
    }

    @SuppressWarnings("serial")
    public static final class _Serializer extends AnyOfSerializer<PetSearch> {

        public _Serializer() {
            super(Globals.config(), PetSearch.class);
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
        PetSearch other = (PetSearch) o;
        return 
            Objects.deepEquals(this.petByAge, other.petByAge) && 
            Objects.deepEquals(this.petByType, other.petByType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(petByAge, petByType);
    }

    @Override
    public @Nonnull String toString() {
        return Util.toString(PetSearch.class, "petByAge", petByAge, "petByType", petByType);
    }
}
