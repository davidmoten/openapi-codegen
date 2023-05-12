package org.davidmoten.oa3.codegen.test.main.schema;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.annotation.Generated;

import java.lang.Override;
import java.lang.String;
import java.util.Objects;

import org.davidmoten.oa3.codegen.util.Util;
import org.springframework.boot.context.properties.ConstructorBinding;

@JsonInclude(Include.NON_NULL)
@JsonAutoDetect(fieldVisibility = Visibility.ANY, creatorVisibility = Visibility.ANY)
@Generated(value = "com.github.davidmoten:openapi-codegen-runtime0.1-alpha-7-SNAPSHOT")
public final class Car implements HasWheels, Vehicle {

    @JsonProperty("vehicleType")
    private final String vehicleType;

    @JsonProperty("wheelsType")
    private final String wheelsType;

    @JsonCreator
    private Car(
            @JsonProperty("vehicleType") String vehicleType,
            @JsonProperty("wheelsType") String wheelsType) {
        this.vehicleType = vehicleType;
        this.wheelsType = wheelsType;
    }

    @ConstructorBinding
    public Car() {
        this.vehicleType = "car";
        this.wheelsType = "four";
    }

    @Override
    public String vehicleType() {
        return vehicleType;
    }

    @Override
    public String wheelsType() {
        return wheelsType;
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