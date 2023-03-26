package org.davidmoten.openapi.v3;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

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
import com.github.davidmoten.guavamini.Preconditions;

public class SerializationTest {

    private static final Circle CIRCLE = new Circle("Circle", 12.0f, 50.5f, 4f);
    private static final String CIRCLE_JSON = "{\"geometryType\":\"Circle\",\"lat\":12.0,\"lon\":50.5,\"radiusNm\":4.0}";

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

    @Test
    public void testSerializeOneOfMember() throws JsonProcessingException {
        ObjectMapper m = new ObjectMapper();
        m.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
        assertEquals(CIRCLE_JSON, m.writeValueAsString(CIRCLE));
    }

    @Test
    public void testSerializeGeometry() throws JsonProcessingException {
        ObjectMapper m = new ObjectMapper();
        m.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
        Geometry g = CIRCLE;
        assertEquals(CIRCLE_JSON, m.writeValueAsString(g));
    }

    @Test
    public void testDeserializeGeometry() throws JsonMappingException, JsonProcessingException {
        ObjectMapper m = new ObjectMapper();
        m.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
        Object g = m.readerFor(Geometry.class).readValue(CIRCLE_JSON);
        assertTrue(g instanceof Circle);
        Circle c = (Circle) g;
        assertEquals(12.0f, c.lat(), 0.000001);
    }

    @JsonTypeInfo(use = Id.DEDUCTION)
    @JsonSubTypes({
            @Type(Rectangle.class), 
            @Type(Circle.class)})
    public interface Geometry {

    }

    public static final class Circle implements Geometry {

        private final String geometryType;
        private final float lat;
        private final float lon;
        private final float radiusNm;

        @JsonCreator
        public Circle(@JsonProperty("geometryType") String geometryType, @JsonProperty("lat") float lat,
                @JsonProperty("lon") float lon, @JsonProperty("radiusNm") float radiusNm) {
            this.geometryType = Preconditions.checkNotNull(geometryType);
            this.lat = lat;
            this.lon = lon;
            this.radiusNm = radiusNm;
        }

        public String geometryType() {
            return geometryType;
        }

        public float lat() {
            return lat;
        }

        public float lon() {
            return lon;
        }

        public float radiusNm() {
            return radiusNm;
        }
    }

    public static final class Rectangle implements Geometry {

        private final String geometryType;
        private final float minLat;
        private final float leftLon;
        private final float heightDegrees;
        private final float widthDegrees;

        @JsonCreator
        public Rectangle(@JsonProperty("geometryType") String geometryType, @JsonProperty("minLat") float minLat,
                @JsonProperty("leftLon") float leftLon, @JsonProperty("heightDegrees") float heightDegrees,
                @JsonProperty("widthDegrees") float widthDegrees) {
            this.geometryType = Preconditions.checkNotNull(geometryType);
            this.minLat = Preconditions.checkNotNull(minLat);
            this.leftLon = Preconditions.checkNotNull(leftLon);
            this.heightDegrees = heightDegrees;
            this.widthDegrees = widthDegrees;
        }

        public String geometryType() {
            return geometryType;
        }

        public float minLat() {
            return minLat;
        }

        public float leftLon() {
            return leftLon;
        }

        public float heightDegrees() {
            return heightDegrees;
        }

        public float widthDegrees() {
            return widthDegrees;
        }
    }

}
