package org.davidmoten.openapi.generator.v3.plugin.test;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class DiscriminatorTest {

    @Test
    public void test() throws JsonMappingException, JsonProcessingException {
        ObjectMapper m = new ObjectMapper();
        String json = "{\"shapeType\":\"square\"}";

        // serialization works
        Square square = new Square();
        assertEquals(json, m.writeValueAsString(square));

        // deserialization using Shape does not work, nor with Square
        Shape s = m.readValue(json, Shape.class);

        // fails. shapeType is null
        assertEquals("square", s.shapeType());
    }

    @JsonTypeInfo(use = Id.NAME, property = "shapeType", include = As.EXISTING_PROPERTY)
    @JsonSubTypes({ @Type(value = Oval.class, name = "oval"), @Type(value = Square.class, name = "square") })
    public interface Shape {

        String shapeType();
    }

    @JsonInclude(Include.NON_NULL)
    @JsonAutoDetect(fieldVisibility = Visibility.ANY, creatorVisibility = Visibility.ANY)
    public static final class Square implements Shape {

        private final String shapeType;

        @JsonCreator
        private Square(@JsonProperty("shapeType") String shapeType) {
            this.shapeType = shapeType;
            System.out.println("constructed");
        }

        public Square() {
            this.shapeType = "square";
        }

        @Override
        public String shapeType() {
            return shapeType;
        }
    }

    @JsonInclude(Include.NON_NULL)
    @JsonAutoDetect(fieldVisibility = Visibility.ANY, creatorVisibility = Visibility.ANY)
    public static final class Oval implements Shape {

        private final String shapeType;

        @JsonCreator
        private Oval(@JsonProperty("shapeType") String shapeType) {
            this.shapeType = shapeType;
        }

        public Oval() {
            this.shapeType = "oval";
        }

        @Override
        public String shapeType() {
            return shapeType;
        }
    }

}
