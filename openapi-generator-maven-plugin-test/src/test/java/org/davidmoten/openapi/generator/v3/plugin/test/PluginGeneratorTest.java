package org.davidmoten.openapi.generator.v3.plugin.test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

import org.davidmoten.openapi.v3.runtime.Mapper;
import org.davidmoten.openapi.v3.runtime.Util;
import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.ValueInstantiationException;
import com.github.davidmoten.guavamini.Lists;

import generated.model.ArrayOfComplexType;
import generated.model.ArrayOfComplexType.ArrayOfComplexTypeItem;
import generated.model.ArrayOfOneOf;
import generated.model.ArrayOfOneOf.ArrayOfOneOfItem;
import generated.model.ArrayOfOneOfString;
import generated.model.ArrayOfOneOfString.ArrayOfOneOfStringItem;
import generated.model.Bike;
import generated.model.ObjectAllOptionalFields;
import generated.model.ObjectNoOptionalFields;
import generated.model.Ref;
import generated.model.Shape;
import generated.model.Shape2;
import generated.model.SimpleBinary;
import generated.model.SimpleBoolean;
import generated.model.SimpleByteArray;
import generated.model.SimpleDate;
import generated.model.SimpleDateTime;
import generated.model.SimpleDouble;
import generated.model.SimpleFloat;
import generated.model.SimpleInt;
import generated.model.SimpleInteger;
import generated.model.SimpleIntegerArray;
import generated.model.SimpleLong;
import generated.model.SimpleString;
import generated.model.SimpleTime;
import generated.model.Square;
import generated.model.Square2;
import generated.model.Vehicle;

public class PluginGeneratorTest {

    private static final ObjectMapper m = Mapper.instance();

    @Test
    public void testSimpleLong() throws JsonMappingException, JsonProcessingException {
        String json = Long.MAX_VALUE + "";
        SimpleLong a = m.readValue(json, SimpleLong.class);
        assertReturns(SimpleLong.class, "value", long.class);
        assertEquals(json, m.writeValueAsString(a));
    }
    
    public static void assertReturns(Class<?> cls, String methodName, Class<?> returnClass) {
        Method method;
        try {
            method = cls.getMethod(methodName);
        } catch (NoSuchMethodException | SecurityException e) {
            throw new RuntimeException(e);
        }
        assertEquals(returnClass, method.getReturnType());
    }

    @Test
    public void testSimpleInt() throws JsonMappingException, JsonProcessingException {
        String json = "123";
        SimpleInt a = m.readValue(json, SimpleInt.class);
        assertReturns(SimpleInt.class, "value", int.class);
        assertEquals(123, a.value());
        assertEquals(json, m.writeValueAsString(a));
    }

    @Test
    public void testSimpleIntegerDefaultsToLong() throws JsonMappingException, JsonProcessingException {
        String json = "123";
        SimpleInteger a = m.readValue(json, SimpleInteger.class);
        assertReturns(SimpleInteger.class, "value", long.class);
        assertEquals(123, a.value());
        assertEquals(json, m.writeValueAsString(a));
    }

    @Test
    public void testSimpleFloat() throws JsonMappingException, JsonProcessingException {
        String json = "123.4";
        SimpleFloat a = m.readValue(json, SimpleFloat.class);
        assertReturns(SimpleFloat.class, "value", float.class);
        assertEquals(123.4, a.value(), 0.00001);
        assertEquals(json, m.writeValueAsString(a));
    }

    @Test
    public void testSimpleDouble() throws JsonMappingException, JsonProcessingException {
        String json = "123.4";
        SimpleDouble a = m.readValue(json, SimpleDouble.class);
        assertReturns(SimpleDouble.class, "value", double.class);
        assertEquals(123.4, a.value(), 0.00001);
        assertEquals(json, m.writeValueAsString(a));
    }

    @Test
    public void testSimpleString() throws JsonMappingException, JsonProcessingException {
        String json = "\"abc\"";
        SimpleString a = m.readValue(json, SimpleString.class);
        assertTrue(a.value() instanceof String);
        assertEquals("abc", a.value());
        assertEquals(json, m.writeValueAsString(a));
        // test constructor
        assertEquals(json, m.writeValueAsString(new SimpleString("abc")));
        shouldThrowIAE(() -> new SimpleString(null));
    }

    private static void shouldThrowIAE(Runnable r) {
        try {
            r.run();
            fail();
        } catch (IllegalArgumentException e) {
            // all good
        }
    }

    @Test
    public void testSimpleDateTime() throws JsonMappingException, JsonProcessingException {
        String s = "2018-03-20T09:12:28Z";
        String json = "\"" + s + "\"";
        SimpleDateTime a = m.readValue(json, SimpleDateTime.class);
        assertTrue(a.value() instanceof OffsetDateTime);
        assertEquals(OffsetDateTime.parse(s), a.value());
        assertEquals(json, m.writeValueAsString(a));
        // test constructor
        assertEquals(json, m.writeValueAsString(new SimpleDateTime(OffsetDateTime.parse(s))));
        shouldThrowIAE(() -> new SimpleDateTime(null));
    }

    @Test
    public void testSimpleDate() throws JsonMappingException, JsonProcessingException {
        String s = "2018-03-20";
        String json = "\"" + s + "\"";
        SimpleDate a = m.readValue(json, SimpleDate.class);
        assertTrue(a.value() instanceof LocalDate);
        assertEquals(LocalDate.parse(s), a.value());
        assertEquals(json, m.writeValueAsString(a));
        // test constructor
        assertEquals(json, m.writeValueAsString(new SimpleDate(LocalDate.parse(s))));
        shouldThrowIAE(() -> new SimpleDate(null));
    }

    @Test
    public void testSimpleTime() throws JsonMappingException, JsonProcessingException {
        String s = "09:12:28Z";
        String json = "\"" + s + "\"";
        SimpleTime a = m.readValue(json, SimpleTime.class);
        assertTrue(a.value() instanceof OffsetTime);
        assertEquals(OffsetTime.parse(s), a.value());
        assertEquals(json, m.writeValueAsString(a));
        // test constructor
        assertEquals(json, m.writeValueAsString(new SimpleTime(OffsetTime.parse(s))));
        shouldThrowIAE(() -> new SimpleTime(null));
    }

    @Test
    public void testSimpleBoolean() throws JsonMappingException, JsonProcessingException {
        String json = "true";
        SimpleBoolean a = m.readValue(json, SimpleBoolean.class);
        assertReturns(SimpleBoolean.class, "value", boolean.class);
        assertTrue(a.value());
        assertEquals(json, m.writeValueAsString(a));
        // test constructor
        assertEquals(json, m.writeValueAsString(new SimpleBoolean(true)));
    }

    @Test
    public void testSimpleByteArrayUsingBase64Encoding() throws JsonMappingException, JsonProcessingException {
        byte[] bytes = "abc".getBytes(StandardCharsets.UTF_8);
        String json = "\"" + Base64.getEncoder().encodeToString(bytes) + "\"";
        SimpleByteArray a = m.readValue(json, SimpleByteArray.class);
        assertTrue(a.value() instanceof byte[]);
        assertArrayEquals(bytes, a.value());
        assertEquals(json, m.writeValueAsString(a));
        // test constructor
        assertEquals(json, m.writeValueAsString(new SimpleByteArray(bytes)));
        shouldThrowIAE(() -> new SimpleByteArray(null));
    }

    @Test
    public void testSimpleBinaryUsingOctetEncoding() throws JsonMappingException, JsonProcessingException {
        byte[] bytes = "abc".getBytes(StandardCharsets.UTF_8);
        String json = "\"" + Util.encodeOctets(bytes) + "\"";
        SimpleBinary a = m.readValue(json, SimpleBinary.class);
        assertTrue(a.value() instanceof byte[]);
        assertArrayEquals(bytes, a.value());
        assertEquals(json, m.writeValueAsString(a));
        // test constructor
        assertEquals(json, m.writeValueAsString(new SimpleBinary(bytes)));
        shouldThrowIAE(() -> new SimpleBinary(null));
    }

    @Test
    public void testSimpleIntegerArray() throws JsonMappingException, JsonProcessingException {
        List<Long> list = Lists.newArrayList(1L, 2L, 3L);
        String json = "[1,2,3]";
        SimpleIntegerArray a = m.readValue(json, SimpleIntegerArray.class);
        assertEquals(list, a.value());
        assertEquals(json, m.writeValueAsString(a));
        // test constructor
        assertEquals(json, m.writeValueAsString(new SimpleIntegerArray(list)));
        // TODO should null be passable to List<Long>?
        // shouldThrowIAE(() -> new SimpleIntegerArray(null));
    }

    @Test
    public void testArrayOfComplexType() throws JsonMappingException, JsonProcessingException {
        String json = "[{\"name\":\"Fred\"},{\"name\":\"Sam\"}]";
        ArrayOfComplexType a = m.readValue(json, ArrayOfComplexType.class);
        assertEquals(2, a.value().size());
        assertEquals("Fred", a.value().get(0).name());
        assertEquals("Sam", a.value().get(1).name());
        assertEquals(json, m.writeValueAsString(a));
        // test constructor
        assertEquals(json, m.writeValueAsString(new ArrayOfComplexType(
                Arrays.asList(new ArrayOfComplexTypeItem("Fred"), new ArrayOfComplexTypeItem("Sam")))));
    }

    @Test
    public void testArrayOfOneOf() throws JsonMappingException, JsonProcessingException {
        String json = "[true,123]";
        ArrayOfOneOf a = m.readValue(json, ArrayOfOneOf.class);
        assertTrue((boolean) a.value().get(0).value());
        assertEquals(123, a.value().get(1).value());
        assertEquals(json, m.writeValueAsString(a));
        assertEquals(json, m.writeValueAsString(
                new ArrayOfOneOf(Arrays.asList(new ArrayOfOneOfItem(true), new ArrayOfOneOfItem(123)))));
    }

    @Test
    public void testArrayOfOneOfString() throws JsonMappingException, JsonProcessingException {
        String json = "[\"hello\",123]";
        ArrayOfOneOfString a = m.readValue(json, ArrayOfOneOfString.class);
        assertEquals("hello", a.value().get(0).value());
        assertEquals(123, a.value().get(1).value());
        assertEquals(json, m.writeValueAsString(a));
        assertEquals(json, m.writeValueAsString(new ArrayOfOneOfString(
                Arrays.asList(new ArrayOfOneOfStringItem("hello"), new ArrayOfOneOfStringItem(123)))));
    }

    @Test
    public void testObjectWithAllOptionalFields() throws JsonMappingException, JsonProcessingException {
        String json = "{\"str\":\"hello\",\"num\":123}";
        ObjectAllOptionalFields a = m.readValue(json, ObjectAllOptionalFields.class);
        assertEquals("hello", a.str().get());
        assertEquals(123, (int) a.num().get());
        assertEquals("{}", m.writeValueAsString(new ObjectAllOptionalFields(Optional.empty(), Optional.empty())));
        ObjectAllOptionalFields b = m.readValue("{}", ObjectAllOptionalFields.class);
        assertFalse(b.str().isPresent());
        assertFalse(b.num().isPresent());
        assertEquals(1, b.getClass().getConstructors().length);
        shouldThrowIAE(() -> new ObjectAllOptionalFields(null, Optional.of(123)));
        shouldThrowIAE(() -> new ObjectAllOptionalFields(Optional.of("hello"), null));
    }

    @Test
    public void testObjectWithNoOptionalFields() throws JsonMappingException, JsonProcessingException {
        String json = "{\"label\":\"hello\",\"num\":123}";
        ObjectNoOptionalFields a = m.readValue(json, ObjectNoOptionalFields.class);
        assertEquals("hello", a.label());
        assertEquals(123, (int) a.num());
        // test constructor
        assertEquals(json, m.writeValueAsString(new ObjectNoOptionalFields("hello", 123)));
        assertEquals(1, ObjectNoOptionalFields.class.getConstructors().length);
        try {
            m.readValue("{}", ObjectNoOptionalFields.class);
            Assert.fail();
        } catch (ValueInstantiationException e) {
            // expected
        }
        shouldThrowIAE(() -> new ObjectNoOptionalFields(null, 123));
    }

    @Test
    public void testMultipleDiscriminatedPolymorphism() throws JsonMappingException, JsonProcessingException {
        String json = "{\"vehicleType\":\"bike\",\"wheelsType\":\"two\",\"colour\":\"red\"}";
        Vehicle v = m.readValue(json, Vehicle.class);
        assertEquals("bike", v.vehicleType());
        assertTrue(v instanceof Bike);
        Bike b = new Bike("red");
        assertEquals(json, m.writeValueAsString(b));
        assertEquals(1, Bike.class.getConstructors().length);
        shouldThrowIAE(() -> new Bike(null));
    }

    @Test
    public void testSingleDiscriminationPolymorphism() throws JsonMappingException, JsonProcessingException {
        String json = "{\"shapeType\":\"square\"}";
        Shape s = m.readValue(json, Shape.class);
        assertEquals("square", s.shapeType());
        assertEquals(json, m.writeValueAsString(s));
        assertEquals(json, m.writeValueAsString(new Square()));
    }

    @Test
    public void testDiscriminatorWithoutMapping() throws JsonMappingException, JsonProcessingException {
        String json = "{\"shapeType\":\"Square2\"}";
        Shape2 s = m.readValue(json, Shape2.class);
        assertEquals("Square2", s.shapeType());
        assertEquals(json, m.writeValueAsString(s));
        assertEquals(json, m.writeValueAsString(new Square2()));
    }

    @Test
    public void testRef() throws JsonMappingException, JsonProcessingException {
        String json = "123";
        Ref r = m.readValue(json, Ref.class);
        assertEquals(123, r.value().value());
        assertEquals(json, m.writeValueAsString(r));
        assertEquals(json, m.writeValueAsString(new Ref(new SimpleInteger(123))));
        shouldThrowIAE(() -> new Ref(null));
    }
}
