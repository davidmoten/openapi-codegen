package org.davidmoten.oa3.codegen.test.main.schema;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
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
import org.davidmoten.oa3.codegen.util.Util;
import org.springframework.boot.context.properties.ConstructorBinding;

@JsonInclude(Include.NON_ABSENT)
@JsonAutoDetect(
        fieldVisibility = JsonAutoDetect.Visibility.ANY,
        creatorVisibility = JsonAutoDetect.Visibility.ANY,
        setterVisibility = JsonAutoDetect.Visibility.ANY)
@Generated(value = "com.github.davidmoten:openapi-codegen-runtime:0.1.13-SNAPSHOT")
public final class Car implements HasWheels, Vehicle {

    @JsonProperty("vehicleType")
    private final String vehicleType;

    @JsonProperty("wheelsType")
    private final String wheelsType;

    @ConstructorBinding
    @JsonCreator
    private Car(
            @JsonProperty("vehicleType") String vehicleType,
            @JsonProperty("wheelsType") String wheelsType) {
        Preconditions.checkEquals(DiscriminatorHelper.value(String.class, "car"), vehicleType, "vehicleType");
        this.vehicleType = vehicleType;
        Preconditions.checkEquals(DiscriminatorHelper.value(String.class, "four"), wheelsType, "wheelsType");
        this.wheelsType = wheelsType;
    }

    @Override
    public String vehicleType() {
        return DiscriminatorHelper.value(vehicleType);
    }

    @Override
    public String wheelsType() {
        return DiscriminatorHelper.value(wheelsType);
    }

    Map<String, Object> _internal_properties() {
        return Maps
                .put("vehicleType", (Object) vehicleType)
                .put("wheelsType", (Object) wheelsType)
                .build();
    }

    private static final Car INSTANCE = 
            new Car(DiscriminatorHelper.value(String.class, "car"), DiscriminatorHelper.value(String.class, "four"));

    public static Car instance() {
        return INSTANCE;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Car other = (Car) o;
        return 
            Objects.equals(this.vehicleType, other.vehicleType) && 
            Objects.equals(this.wheelsType, other.wheelsType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(vehicleType, wheelsType);
    }

    @Override
    public String toString() {
        return Util.toString(Car.class, "vehicleType", vehicleType, "wheelsType", wheelsType);
    }
}
