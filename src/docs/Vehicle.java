package org.davidmoten.oa3.codegen.test.main.schema;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

import jakarta.annotation.Generated;

import java.lang.String;

@JsonTypeInfo(use = Id.NAME, property = "vehicleType", include = As.EXISTING_PROPERTY, visible = true)
@JsonSubTypes({
        @Type(value = Car.class, name = "car"), 
        @Type(value = Bike.class, name = "bike")})
@Generated(value = "com.github.davidmoten:openapi-codegen-runtime:0.1.8-SNAPSHOT")
public interface Vehicle {

    String vehicleType();
}
