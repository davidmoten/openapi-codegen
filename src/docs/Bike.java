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

import org.davidmoten.oa3.codegen.runtime.DiscriminatorHelper;
import org.davidmoten.oa3.codegen.runtime.Preconditions;
import org.davidmoten.oa3.codegen.test.main.Globals;
import org.davidmoten.oa3.codegen.util.Util;
import org.springframework.boot.context.properties.ConstructorBinding;

@JsonInclude(Include.NON_NULL)
@JsonAutoDetect(fieldVisibility = Visibility.ANY, creatorVisibility = Visibility.ANY, setterVisibility = Visibility.ANY)
@Generated(value = "com.github.davidmoten:openapi-codegen-runtime:0.1.8-SNAPSHOT")
public final class Bike implements HasWheels, Vehicle {

    @JsonProperty("vehicleType")
    private final String vehicleType;

    @JsonProperty("wheelsType")
    private final String wheelsType;

    @JsonProperty("colour")
    private final String colour;

    @JsonCreator
    private Bike(
            @JsonProperty("vehicleType") String vehicleType,
            @JsonProperty("wheelsType") String wheelsType,
            @JsonProperty("colour") String colour) {
        this.vehicleType = vehicleType;
        this.wheelsType = wheelsType;
        this.colour = colour;
    }

    @ConstructorBinding
    public Bike(
            String colour) {
        if (Globals.config().validateInConstructor().test(Bike.class)) {
            Preconditions.checkNotNull(colour, "colour");
        }
        this.vehicleType = DiscriminatorHelper.value(String.class, "bike");
        this.wheelsType = DiscriminatorHelper.value(String.class, "two");
        this.colour = colour;
    }

    public static Bike colour(String colour) {
        return new Bike(colour);
    }

    @Override
    public String vehicleType() {
        return DiscriminatorHelper.value(vehicleType);
    }

    @Override
    public String wheelsType() {
        return DiscriminatorHelper.value(wheelsType);
    }

    public String colour() {
        return colour;
    }

    Map<String, Object> _internal_properties() {
        return Maps
                .put("vehicleType", (Object) vehicleType)
                .put("wheelsType", (Object) wheelsType)
                .put("colour", (Object) colour)
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
        Bike other = (Bike) o;
        return 
            Objects.equals(this.vehicleType, other.vehicleType) && 
            Objects.equals(this.wheelsType, other.wheelsType) && 
            Objects.equals(this.colour, other.colour);
    }

    @Override
    public int hashCode() {
        return Objects.hash(vehicleType, wheelsType, colour);
    }

    @Override
    public String toString() {
        return Util.toString(Bike.class, "vehicleType", vehicleType, "wheelsType", wheelsType, "colour", colour);
    }
}
