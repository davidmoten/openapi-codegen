package org.davidmoten.oa3.codegen.generator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.davidmoten.oa3.codegen.runtime.AnyOfDeserializer;
import org.davidmoten.oa3.codegen.runtime.AnyOfMember;
import org.davidmoten.oa3.codegen.runtime.AnyOfSerializer;
import org.davidmoten.oa3.codegen.runtime.Config;
import org.davidmoten.oa3.codegen.runtime.NullEnumDeserializer;
import org.davidmoten.oa3.codegen.runtime.PolymorphicDeserializer;
import org.davidmoten.oa3.codegen.runtime.PolymorphicType;
import org.junit.jupiter.api.Test;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.boot.context.properties.ConstructorBinding;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
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
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.github.davidmoten.guavamini.Lists;
import com.github.davidmoten.guavamini.Preconditions;

import jakarta.annotation.Generated;

public class SerializationTest {

    private static final String CIRCLE_JSON = "{\"a\":\"thing\"}";

    private static final Config CONFIG = Config.builder().build();

    private static final ObjectMapper m = Config.builder().build().mapper();

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

        @JsonProperty("a")
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

        @JsonProperty("b")
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

    @Test
    public void testPolymorphicDeserializationThrows() throws JsonMappingException, JsonProcessingException {
        assertThrows(JsonMappingException.class, () -> {
            String json = "{\"radiusKm\":3.4}";
            m.readerFor(OneOf.class).readValue(json);
        });
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
        public static final class Deserializer extends PolymorphicDeserializer<OneOf> {

            public Deserializer() {
                super(CONFIG, PolymorphicType.ONE_OF, OneOf.class, Circle2.class, Rectangle2.class);
            }
        }
    }

    @JsonAutoDetect(fieldVisibility = Visibility.ANY)
    @JsonInclude(Include.NON_NULL)
    public static final class Circle2 {
        @JsonProperty("radiusNm")
        private final double radiusNm;
        @JsonProperty("colour")
        private final String colour;

        @JsonCreator
        private Circle2(@JsonProperty("radiusNm") double radiusNm, @JsonProperty("colour") String colour) {
            this.radiusNm = radiusNm;
            this.colour = colour;
        }

        public Circle2(double radiusNm, Optional<String> colour) {
            this(radiusNm, colour.orElse(null));
        }

        public double radiusNm() {
            return radiusNm;
        }

        public Optional<String> colour() {
            return Optional.ofNullable(colour);
        }
    }

    @JsonAutoDetect(fieldVisibility = Visibility.ANY)
    public static final class Rectangle2 {
        @JsonProperty("heightDegrees")
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

    @Test
    public void testDiscriminatorFindsCar() throws JsonMappingException, JsonProcessingException {
        Vehicle v = m.readValue("{\"vehicleType\":\"car\"}", Vehicle.class);
        assertTrue(v instanceof Car);
    }

    @Test
    public void testDiscriminatorFindsBike() throws JsonMappingException, JsonProcessingException {
        Vehicle v = m.readValue("{\"vehicleType\":\"bike\"}", Vehicle.class);
        assertTrue(v instanceof Bike);
    }

    @Test
    public void testDiscriminatorSerializeSubClass() throws JsonMappingException, JsonProcessingException {
        String json = m.writeValueAsString(new Bike("red"));
        System.out.println(json);
        assertEquals("{\"vehicleType\":\"bike\",\"colour\":\"red\"}", json);
    }

    @JsonTypeInfo(use = Id.NAME, property = "vehicleType", include = As.EXISTING_PROPERTY)
    @JsonSubTypes({ //
            @Type(value = Car.class, name = "car"), //
            @Type(value = Bike.class, name = "bike") })
    public interface Vehicle {

        String vehicleType();
    }

    @JsonInclude(Include.NON_NULL)
    @JsonAutoDetect(fieldVisibility = Visibility.ANY)
    public final static class Car implements Vehicle {

        @JsonProperty("vehicleType")
        private final String vehicleType;

        @JsonCreator
        private Car(@JsonProperty("vehicleType") String vehicleType) {
            this.vehicleType = vehicleType;
        }

        public Car() {
            this("car");
        }

        @Override
        public String vehicleType() {
            return vehicleType;
        }
    }

    @JsonInclude(Include.NON_NULL)
    @JsonAutoDetect(fieldVisibility = Visibility.ANY)
    public static final class Bike implements Vehicle {

        @JsonProperty("vehicleType")
        private final String vehicleType;

        @JsonProperty("colour")
        private final String colour;

        @JsonCreator
        private Bike(@JsonProperty("vehicleType") String vehicleType, @JsonProperty("colour") String colour) {
            this.vehicleType = vehicleType;
            this.colour = colour;
        }

        public Bike(String colour) {
            this("bike", colour);
        }

        @Override
        public String vehicleType() {
            return vehicleType;
        }

        public String colour() {
            return colour;
        }
    }

    @Test
    public void testWithMap() throws JsonProcessingException {
        Map<String, String> map = new HashMap<>();
        map.put("nickname", "fred");
        map.put("suburb", "crace");
        WithMap a = new WithMap("alf", map);
        String json = m.writeValueAsString(a);
        JsonNode tree = m.readTree(json);

        // ensure that map entries are top-level properties
        assertEquals("alf", tree.get("name").asText());
        assertEquals("fred", tree.get("nickname").asText());

        WithMap b = m.readValue(json, WithMap.class);
        assertEquals("alf", b.name);
        assertEquals("fred", b.map.get("nickname"));
        assertEquals("crace", b.map.get("suburb"));
    }

    @Test
    public void testAllOf() throws JsonMappingException, JsonProcessingException {
        String json = "{\"firstName\":\"Dave\",\"numBikes\":3,\"common\":\"abc\"}";
        ObjectMapper mapper = m.copy().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        AllOf a = mapper.readValue(json, AllOf.class);
        assertEquals("Dave", a.person.firstName);
        assertEquals(3, a.bikes.numBikes);
        assertEquals("abc", a.person.common);
        assertEquals("abc", a.bikes.common);

        // TODO serialization problematic because Jackson repeats the common field
        // (admittedly it has to resolve a conflict if there is one)
        // As a test workaround we ensure that Jackson deserialization doesn't get
        // flustered by the repeated field

        AllOf b = mapper.readValue(mapper.writeValueAsString(a), AllOf.class);
        assertEquals("Dave", b.person.firstName);
        assertEquals(3, b.bikes.numBikes);
        assertEquals("abc", b.person.common);
        assertEquals("abc", b.bikes.common);

    }

    @Test
    public void testListOfMap() throws JsonProcessingException {
        Map<String, Object> map1 = new HashMap<>();
        map1.put("hello", "there");
        map1.put("how", "are");
        Map<String, Object> map2 = new HashMap<>();
        map2.put("you", 23);
        map2.put("num", 1.23);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> list = Lists.of(map1, map2);
        ListOfMap a = new ListOfMap(list);
        String json = m.writeValueAsString(a);
        ListOfMap b = m.readValue(json, ListOfMap.class);
        assertEquals(list, b.list());
    }

    @Test
    public void testNullableRequiredIsNull() throws JsonProcessingException {
        NullableRequired a = new NullableRequired(Optional.empty());
        String json = m.writeValueAsString(a);
        NullableRequired b = m.readValue(json, NullableRequired.class);
        assertTrue(b.name.isPresent());
        assertNull(b.name.get());
    }

    @Test
    public void testNullableRequiredIsNotNull() throws JsonProcessingException {
        NullableRequired a = new NullableRequired(Optional.of("hi"));
        String json = m.writeValueAsString(a);
        NullableRequired b = m.readValue(json, NullableRequired.class);
        assertTrue(b.name.isPresent());
        assertEquals("hi", b.name.get());
    }

    @Test
    public void testNullableNotRequiredIsUndefined() throws JsonProcessingException {
        NullableNotRequired a = new NullableNotRequired(JsonNullable.undefined());
        String json = m.writeValueAsString(a);
        NullableNotRequired b = m.readValue(json, NullableNotRequired.class);
        assertFalse(b.name.isPresent());
    }

    @Test
    public void testNullableNotRequiredIsNull() throws JsonProcessingException {
        NullableNotRequired a = new NullableNotRequired(JsonNullable.of(null));
        String json = m.writeValueAsString(a);
        NullableNotRequired b = m.readValue(json, NullableNotRequired.class);
        assertTrue(b.name.isPresent());
        assertNull(b.name.get());
    }

    @Test
    public void testNullableNotRequiredIsNotNull() throws JsonProcessingException {
        NullableNotRequired a = new NullableNotRequired(JsonNullable.of("hi"));
        String json = m.writeValueAsString(a);
        NullableNotRequired b = m.readValue(json, NullableNotRequired.class);
        assertTrue(b.name.isPresent());
        assertEquals("hi", b.name.get());
    }

    @Test
    public void testNullableEnumWhenNull() throws JsonProcessingException {
        String json = m.writeValueAsString(NullableEnum.NULL_);
        assertEquals("null", json);
        assertEquals(NullableEnum.NULL_, m.readValue(json, NullableEnum.class));
    }

    @Test
    public void testNullableEnumWhenNotNull() throws JsonProcessingException {
        String json = m.writeValueAsString(NullableEnum.HELLO);
        assertEquals(NullableEnum.HELLO, m.readValue(json, NullableEnum.class));
    }

    @Test
    public void testAnyOf() throws JsonProcessingException {
        Person p = new Person();
        p.firstName = "fred";
        p.lastName = "smith";
        p.common = "something";
//        String s = m.readValue("{\"firstName\":\"fred\",\"lastName\":\"smith\",\"common\":\"something\",\"hasSeniorCard\":true}", SimpleName.class).name;
        String pJson = m.writeValueAsString(p);
        m.readValue(pJson, Person.class);
        Person2 p2 = new Person2();
        p2.firstName = "fred";
        p2.lastName = "smith";
        p2.hasSeniorCard = true;
        {
            AnyOf a = new AnyOf(Optional.empty(), Optional.of(p), Optional.of(p2));
            String json = m.writeValueAsString(a);
            AnyOf b = m.readValue(json, AnyOf.class);
            assertFalse(b.name.isPresent());
            assertTrue(b.person.isPresent());
            assertTrue(b.person2.isPresent());
            assertEquals(json, m.writeValueAsString(b));
            assertEquals("something", b.person.get().common);
            assertTrue(b.person2.get().hasSeniorCard);
        }
        {
            SimpleName nm = new SimpleName("john");
            m.readValue("\"john\"", SimpleName.class);
            AnyOf a = new AnyOf(Optional.of(nm), Optional.empty(), Optional.empty());
            String json = m.writeValueAsString(a);
            System.out.println(json);
            AnyOf b = m.readValue(json, AnyOf.class);
            assertEquals("john", b.name.get().name);
            assertFalse(b.person.isPresent());
            assertFalse(b.person2.isPresent());
        }

    }

    @JsonInclude(Include.NON_NULL)
    @JsonAutoDetect(fieldVisibility = Visibility.ANY, creatorVisibility = Visibility.ANY, setterVisibility = Visibility.ANY)
    public static final class ListOfMap {

        @JsonValue
        private final List<Map<String, Object>> list;

        @JsonCreator
        public ListOfMap(List<Map<String, Object>> list) {
            this.list = list;
        }

        public List<Map<String, Object>> list() {
            return list;
        }

    }

    @JsonInclude(Include.NON_NULL)
    @JsonDeserialize(using = AllOf.Deserializer.class)
    public static final class AllOf {

        @JsonUnwrapped
        public Person person;

        @JsonUnwrapped
        public HasBikes bikes;

        public AllOf(Person person, HasBikes bikes) {
            this.person = person;
            this.bikes = bikes;
        }

        @SuppressWarnings("serial")
        public static final class Deserializer extends PolymorphicDeserializer<AllOf> {

            protected Deserializer() {
                super(Config.builder().build(), PolymorphicType.ALL_OF, AllOf.class, Person.class, HasBikes.class);
            }
        }
    }

    @JsonDeserialize(using = AnyOf.Deserializer.class)
    @JsonSerialize(using = AnyOf.Serializer.class)
    public static final class AnyOf {

        private final Optional<SimpleName> name;
        private final Optional<Person> person;
        private final Optional<Person2> person2;

        public AnyOf(Optional<SimpleName> name, Optional<Person> person, Optional<Person2> person2) {
            this.name = name;
            this.person = person;
            this.person2 = person2;
        }

        @SuppressWarnings("serial")
        public static final class Deserializer extends AnyOfDeserializer<AnyOf> {
            protected Deserializer() {
                super(Config.builder().build(), AnyOf.class, AnyOfMember.nonNullable(SimpleName.class),
                        AnyOfMember.nonNullable(Person.class), AnyOfMember.nonNullable(Person2.class));
            }
        }

        @SuppressWarnings("serial")
        public static final class Serializer extends AnyOfSerializer<AnyOf> {
            protected Serializer() {
                super(Config.builder().build(), AnyOf.class);
            }
        }
    }
    
    public static final class SimpleName {
        @JsonValue
        public final String name;
        
        @JsonCreator
        public SimpleName(String name) {
            this.name = name;
        }
    }

    @JsonInclude(Include.NON_NULL)
    public static final class Person {
        public String firstName;
        public String lastName;
        public String common;
    }

    @JsonInclude(Include.NON_NULL)
    public static final class Person2 {
        public String firstName;
        public String lastName;
        public boolean hasSeniorCard;
    }

    @JsonInclude(Include.NON_NULL)
    public static final class HasBikes {
        public int numBikes;
        public String common;
    }

    @JsonInclude(Include.NON_NULL)
    @JsonAutoDetect(fieldVisibility = Visibility.ANY, creatorVisibility = Visibility.ANY, setterVisibility = Visibility.ANY)
    public static final class WithMap {

        @JsonProperty("name")
        private String name;

        @JsonAnyGetter
        private Map<String, String> map;

        @JsonCreator
        public WithMap(@JsonProperty("name") String name) {
            this.name = name;
            this.map = new HashMap<>();
        }

        public WithMap(String name, Map<String, String> map) {
            this.name = name;
            this.map = map;
        }

        @JsonAnySetter
        private void put(String key, String value) {
            map.put(key, value);
        }

        public String name() {
            return name;
        }

        public Map<String, String> map() {
            return Collections.unmodifiableMap(map);
        }

    }

    @JsonInclude(Include.NON_NULL)
    @JsonAutoDetect(fieldVisibility = Visibility.ANY, creatorVisibility = Visibility.ANY, setterVisibility = Visibility.ANY)
    public static final class NullableRequired {

        @JsonProperty("name")
        private final JsonNullable<String> name;

        @JsonCreator
        private NullableRequired(@JsonProperty("name") JsonNullable<String> name) {
            Preconditions.checkArgumentNotNull(name, "name");
            this.name = name;
        }

        public NullableRequired(Optional<String> name) {
            Preconditions.checkArgumentNotNull(name, "name");
            this.name = JsonNullable.of(name.orElse(null));
        }
    }

    @JsonInclude(Include.NON_NULL)
    @JsonAutoDetect(fieldVisibility = Visibility.ANY, creatorVisibility = Visibility.ANY, setterVisibility = Visibility.ANY)
    public static final class NullableNotRequired {

        @JsonProperty("name")
        private final JsonNullable<String> name;

        @JsonCreator
        public NullableNotRequired(@JsonProperty("name") JsonNullable<String> name) {
            Preconditions.checkArgumentNotNull(name, "name");
            this.name = name;
        }

    }

    @JsonDeserialize(using = NullableEnum.Deserializer.class)
    public enum NullableEnum {

        HELLO(JsonNullable.of("hello")), //
        THERE(JsonNullable.of("there")), //
        NULL_(JsonNullable.of(null));

        @JsonValue
        private final JsonNullable<String> value;

        private NullableEnum(JsonNullable<String> value) {
            this.value = value;
        }

        public Optional<String> value() {
            return Optional.ofNullable(value.get());
        }

        @JsonCreator
        public static NullableEnum fromValue(Object value) {
            for (NullableEnum x : NullableEnum.values()) {
                if (Objects.equals(value, x.value.get())) {
                    return x;
                }
            }
            throw new IllegalArgumentException("unexpected enum value: '" + value + "'");
        }

        public static class Deserializer extends NullEnumDeserializer<NullableEnum> {
            protected Deserializer() {
                super(NullableEnum.class, String.class, NULL_);
            }
        }
    }

    @JsonInclude(Include.NON_NULL)
    @JsonAutoDetect(fieldVisibility = Visibility.ANY, creatorVisibility = Visibility.ANY, setterVisibility = Visibility.ANY)
    @Generated(value = "com.github.davidmoten:openapi-codegen-runtime0.1-alpha-7-SNAPSHOT")
    public final class NullableMapProperty {

        @JsonProperty("thing")
        private final JsonNullable<Map<String, Object>> thing;

        @JsonCreator
        private NullableMapProperty(@JsonProperty("thing") JsonNullable<Map<String, Object>> thing) {
            this.thing = thing;
        }

        @ConstructorBinding
        public NullableMapProperty(@JsonProperty("thing") Optional<Map<String, Object>> thing) {
            this.thing = JsonNullable.of(thing.orElse(null));
        }
    }
}
