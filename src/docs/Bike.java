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

import org.davidmoten.oa3.codegen.runtime.DiscriminatorHelper;
import org.davidmoten.oa3.codegen.runtime.Preconditions;
import org.davidmoten.oa3.codegen.test.main.Globals;
import org.davidmoten.oa3.codegen.util.Util;
import org.springframework.boot.context.properties.ConstructorBinding;

@JsonInclude(Include.NON_ABSENT)
@JsonAutoDetect(
        fieldVisibility = JsonAutoDetect.Visibility.ANY,
        creatorVisibility = JsonAutoDetect.Visibility.ANY,
        setterVisibility = JsonAutoDetect.Visibility.ANY)
@Generated(value = "com.github.davidmoten:openapi-codegen-runtime:0.2.2-SNAPSHOT")
public final class Bike implements HasWheels, Vehicle {

    @JsonProperty("vehicleType")
    private final String vehicleType;

    @JsonProperty("wheelsType")
    private final String wheelsType;

    @JsonProperty("colour")
    private final String colour;

    @ConstructorBinding
    @JsonCreator
    private Bike(
            @Nonnull @JsonProperty("vehicleType") String vehicleType,
            @Nonnull @JsonProperty("wheelsType") String wheelsType,
            @Nonnull @JsonProperty("colour") String colour) {
        if (Globals.config().validateInConstructor().test(Bike.class)) {
            Preconditions.checkNotNull(colour, "colour");
        }
        Preconditions.checkEquals(DiscriminatorHelper.value(String.class, "bike"), vehicleType, "vehicleType");
        this.vehicleType = vehicleType;
        Preconditions.checkEquals(DiscriminatorHelper.value(String.class, "two"), wheelsType, "wheelsType");
        this.wheelsType = wheelsType;
        this.colour = colour;
    }


    @Override
    public @Nonnull String vehicleType() {
        return DiscriminatorHelper.value(vehicleType);
    }


    @Override
    public @Nonnull String wheelsType() {
        return DiscriminatorHelper.value(wheelsType);
    }

    public @Nonnull String colour() {
        return colour;
    }

    Map<String, Object> _internal_properties() {
        return Maps
                .put("vehicleType", (Object) vehicleType)
                .put("wheelsType", (Object) wheelsType)
                .put("colour", (Object) colour)
                .build();
    }

    public @Nonnull Bike withColour(@Nonnull String colour) {
        return new Bike(vehicleType, wheelsType, colour);
    }

    public static @Nonnull Bike colour(@Nonnull String colour) {
        return new Bike(DiscriminatorHelper.value(String.class, "bike"), DiscriminatorHelper.value(String.class, "two"), colour);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Bike other = (Bike) o;
        return 
            Objects.deepEquals(this.vehicleType, other.vehicleType) && 
            Objects.deepEquals(this.wheelsType, other.wheelsType) && 
            Objects.deepEquals(this.colour, other.colour);
    }

    @Override
    public int hashCode() {
        return Objects.hash(vehicleType, wheelsType, colour);
    }

    @Override
    public @Nonnull String toString() {
        return Util.toString(Bike.class, "vehicleType", vehicleType, "wheelsType", wheelsType, "colour", colour);
    }
}
