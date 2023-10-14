package org.davidmoten.oa3.codegen.test.main.schema;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.davidmoten.guavamini.Maps;

import jakarta.annotation.Generated;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Map;
import java.util.Objects;

import org.davidmoten.oa3.codegen.runtime.Preconditions;
import org.davidmoten.oa3.codegen.test.main.Globals;
import org.davidmoten.oa3.codegen.util.Util;
import org.springframework.boot.context.properties.ConstructorBinding;

@JsonInclude(Include.NON_NULL)
@JsonAutoDetect(fieldVisibility = Visibility.ANY, creatorVisibility = Visibility.ANY, setterVisibility = Visibility.ANY)
@Generated(value = "com.github.davidmoten:openapi-codegen-runtime:0.1.9-SNAPSHOT")
public final class Pet3 {

    @JsonProperty("pet_type")
    private final String pet_type;

    @JsonCreator
    @ConstructorBinding
    public Pet3(
            @JsonProperty("pet_type") String pet_type) {
        if (Globals.config().validateInConstructor().test(Pet3.class)) {
            Preconditions.checkNotNull(pet_type, "pet_type");
        }
        this.pet_type = pet_type;
    }

    public static Pet3 pet_type(String pet_type) {
        return new Pet3(pet_type);
    }

    public String pet_type() {
        return pet_type;
    }

    Map<String, Object> _internal_properties() {
        return Maps
                .put("pet_type", (Object) pet_type)
                .build();
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
            Objects.equals(this.pet_type, other.pet_type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pet_type);
    }

    @Override
    public String toString() {
        return Util.toString(Pet3.class, "pet_type", pet_type);
    }
}
