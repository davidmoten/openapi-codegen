package org.davidmoten.openapi.v3;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.davidmoten.openapi.v3.runtime.OneOfDeserializer;
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

    private static final ObjectMapper m = new ObjectMapper();

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

    @JsonAutoDetect(fieldVisibility = Visibility.ANY)
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

    @Test
    public void testPolymorphicDeserializationAndSerialization() throws JsonMappingException, JsonProcessingException {
        String json = "{\"radiusNm\":3.4}";
        OneOf g = m.readerFor(OneOf.class).readValue(json);
        assertTrue(g.value instanceof Circle2);
        assertEquals(3.4, ((Circle2) g.value).radiusNm, 0.00001);
        assertEquals(json, m.writeValueAsString(g));
    }

    @Test(expected = JsonMappingException.class)
    public void testPolymorphicDeserializationThrows() throws JsonMappingException, JsonProcessingException {
        String json = "{\"radiusKm\":3.4}";
        m.readerFor(OneOf.class).readValue(json);
    }

    @JsonDeserialize(using = OneOf.Deserializer.class)
    public static final class OneOf {

        @JsonValue
        private final Object value;

        @JsonCreator
        private OneOf(Object value) {
            this.value = value;
        }

        public OneOf(Circle2 circle) {
            this.value = circle;
        }

        public OneOf(Rectangle2 rectangle) {
            this.value = rectangle;
        }

        /**
         * Instance be of type Circle or Rectangle
         * 
         * @return instance
         */
        public Object value() {
            return value;
        }

        @SuppressWarnings("serial")
        public static final class Deserializer extends OneOfDeserializer<OneOf> {

            public Deserializer() {
                super(OneOf.class, Circle2.class, Rectangle2.class);
            }
        }
    }

    @JsonAutoDetect(fieldVisibility = Visibility.ANY)
    public static final class Circle2 {
        private final double radiusNm;

        @JsonCreator
        public Circle2(@JsonProperty("radiusNm") double radiusNm) {
            this.radiusNm = radiusNm;
        }

        public double radiusNm() {
            return radiusNm;
        }

    }

    @JsonAutoDetect(fieldVisibility = Visibility.ANY)
    public static final class Rectangle2 {
        private final double heightDegrees;

        @JsonCreator
        public Rectangle2(@JsonProperty("heightDegrees") double heightDegrees) {
            this.heightDegrees = heightDegrees;
        }

        public double heightDegrees() {
            return heightDegrees;
        }
    }

    @Test
    public void testArray() throws JsonMappingException, JsonProcessingException {
        ObjectMapper m = new ObjectMapper();
        List<Integer> list = Arrays.asList(1, 2, 3);
        ArraySimple a = new ArraySimple(list);
        String json = "[1,2,3]";
        assertEquals(json, m.writeValueAsString(a));
        ArraySimple b = m.readValue(json, ArraySimple.class);
        assertEquals(list, b.arraySimple());
    }

    public static final class ArraySimple {

        @JsonValue
        private final List<Integer> arraySimple;

        @JsonCreator
        public ArraySimple(List<Integer> arraySimple) {
            this.arraySimple = Preconditions.checkNotNull(arraySimple);
        }

        public List<Integer> arraySimple() {
            return arraySimple;
        }
    }

}
