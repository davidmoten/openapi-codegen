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

import org.davidmoten.oa3.codegen.runtime.Preconditions;
import org.davidmoten.oa3.codegen.test.main.Globals;
import org.davidmoten.oa3.codegen.util.Util;

@JsonInclude(Include.NON_ABSENT)
@JsonAutoDetect(
        fieldVisibility = JsonAutoDetect.Visibility.ANY,
        creatorVisibility = JsonAutoDetect.Visibility.ANY,
        setterVisibility = JsonAutoDetect.Visibility.ANY)
@Generated(value = "com.github.davidmoten:openapi-codegen-runtime:0.2.2-SNAPSHOT")
public final class Pet3 {

    @JsonProperty("petType")
    private final String petType;

    @JsonCreator
    public Pet3(
            @Nonnull @JsonProperty("petType") String petType) {
        if (Globals.config().validateInConstructor().test(Pet3.class)) {
            Preconditions.checkNotNull(petType, "petType");
        }
        this.petType = petType;
    }

    public @Nonnull String petType() {
        return petType;
    }

    Map<String, Object> _internal_properties() {
        return Maps
                .put("petType", (Object) petType)
                .build();
    }

    public static @Nonnull Pet3 petType(@Nonnull String petType) {
        return new Pet3(petType);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Pet3 other = (Pet3) o;
        return 
            Objects.deepEquals(this.petType, other.petType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(petType);
    }

    @Override
    public @Nonnull String toString() {
        return Util.toString(Pet3.class, "petType", petType);
    }
}
