package org.davidmoten.openapi.v3;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.davidmoten.openapi.v3.runtime.Classes;
import org.davidmoten.openapi.v3.runtime.OneOfDeserializer;
import org.junit.Ignore;
import org.junit.Test;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.github.davidmoten.guavamini.Preconditions;

public class SerializationTest {

    private static final String CIRCLE_JSON = "{\"a\":\"thing\"}";

    private static final ObjectMapper m = new ObjectMapper().setVisibility(PropertyAccessor.FIELD, Visibility.ANY);

    @Test
    public void testEnumSerializeAndDeserialize() throws JsonProcessingException {
        ObjectMapper m = new ObjectMapper();
        assertEquals("\"urgencia\"", m.writeValueAsString(Priority.URGENCY));
        assertEquals(Priority.URGENCY, m.readerFor(Priority.class).readValue("\"urgencia\""));
    }

    enum Priority {

        SAFETY("seguridad"), URGENCY("urgencia"), DISTRESS("socorro");

        @JsonValue
        private final String value;

        private Priority(String value) {
            this.value = Preconditions.checkNotNull(value);
        }

        String value() {
            return value;
        }
    }

    // Tests using an interface and sub-classes

    @Test
    public void testSerializeGeometrySubClass() throws JsonProcessingException {
        assertEquals(CIRCLE_JSON, m.writeValueAsString(new Circle("thing")));
    }

    @Test
    public void testSerializeGeometry() throws JsonProcessingException {
        Geometry g = new Circle("thing");
        assertEquals(CIRCLE_JSON, m.writeValueAsString(g));
    }

    @Test
    public void testDeserializeGeometry() throws JsonMappingException, JsonProcessingException {
        Object g = m.readerFor(Geometry.class).readValue(CIRCLE_JSON);
        assertTrue(g instanceof Circle);
        Circle c = (Circle) g;
        assertEquals("thing", c.a());
    }

    @JsonTypeInfo(use = Id.DEDUCTION)
    @JsonSubTypes({ @Type(Rectangle.class), @Type(Circle.class) })
    public interface Geometry {

    }

    public static final class Circle implements Geometry {

        private String a;

        @JsonCreator
        public Circle(@JsonProperty("a") String a) {
            this.a = a;
        }

        public String a() {
            return a;
        }

    }

    public static final class Rectangle implements Geometry {

        private final int b;

        @JsonCreator
        public Rectangle(@JsonProperty("b") int b) {
            this.b = b;
        }

        public int b() {
            return b;
        }
    }

    // Tests using an interface and sub-classes

    @Test
    public void testSerializeGeometryMember() throws JsonProcessingException {
        assertEquals(CIRCLE_JSON, m.writeValueAsString(new Circle2("thing")));
    }

    @Test
    public void testSerializeGeometry2() throws JsonProcessingException {
        Geometry2 g = new Geometry2(new Circle2("thing"));
        assertEquals(CIRCLE_JSON, m.writeValueAsString(g));
    }

    @Test
    @Ignore
    public void testDeserializeGeometry2() throws JsonMappingException, JsonProcessingException {
        Object g = m.readerFor(Geometry2.class).readValue(CIRCLE_JSON);
        assertTrue(g instanceof Circle);
        Circle c = (Circle) g;
        assertEquals("thing", c.a());
    }

//    @JsonTypeInfo(use = Id.DEDUCTION)
//    @JsonSubTypes({ @Type(Rectangle2.class), @Type(Circle2.class) })
    public static final class Geometry2 {

        @JsonValue
        private final Object value;

        @JsonCreator
        public Geometry2(Circle2 value) {
            this.value = value;
        }

        @JsonCreator
        public Geometry2(Rectangle2 value) {
            this.value = value;
        }

    }

    public static final class Circle2 {

        private String a;

        @JsonCreator
        public Circle2(@JsonProperty("a") String a) {
            this.a = a;
        }

        public String a() {
            return a;
        }

    }

    public static final class Rectangle2 {

        private final int b;

        @JsonCreator
        public Rectangle2(@JsonProperty("b") int b) {
            this.b = b;
        }

        public int b() {
            return b;
        }
    }

    @Test
    public void testCustomPolymorphicDeserialization() throws JsonMappingException, JsonProcessingException {
        String json = "{\"radiusNm\":3.4}";
        OneOf g = m.readerFor(OneOf.class).readValue(json);
        assertTrue(g.value instanceof Circle3);
        assertEquals(3.4, ((Circle3) g.value).radiusNm, 0.00001);
        assertEquals(json, m.writeValueAsString(g));
    }

    @JsonDeserialize(using = OneOf.Deserializer.class)
    @JsonAutoDetect(fieldVisibility = Visibility.ANY, creatorVisibility = Visibility.ANY)
    public static final class OneOf {

        @JsonValue
        private final Object value;

        @JsonCreator
        OneOf(Object value) {
            this.value = value;
        }
        
        public OneOf(Circle circle) {
            this.value = circle;
        }
        
        public OneOf(Rectangle rectangle) {
            this.value = rectangle;
        }
        
        @SuppressWarnings("serial")
        public static final class Deserializer extends OneOfDeserializer<OneOf> {

            public Deserializer() {
                super(OneOf.class, Classes //
                        .add("radiusNm", Circle3.class) //
                        .add("heightDegrees", Rectangle3.class) //
                        .build());
            }
        }
    }

    public static final class Circle3 {
        private final double radiusNm;

        @JsonCreator
        public Circle3(@JsonProperty("radiusNm") double radiusNm) {
            this.radiusNm = radiusNm;
        }

        public double radiusNm() {
            return radiusNm;
        }

    }

    public static final class Rectangle3 {
        private final double heightDegrees;

        @JsonCreator
        public Rectangle3(@JsonProperty("heightDegrees") double heightDegrees) {
            this.heightDegrees = heightDegrees;
        }

        public double heightDegrees() {
            return heightDegrees;
        }

    }

}
