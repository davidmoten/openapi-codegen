package org.davidmoten.oa3.codegen.test.main;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.davidmoten.oa3.codegen.runtime.Config;
import org.davidmoten.oa3.codegen.test.main.schema.AdditionalProperties;
import org.davidmoten.oa3.codegen.test.main.schema.AdditionalPropertiesTrue;
import org.davidmoten.oa3.codegen.test.main.schema.ArrayInProperty;
import org.davidmoten.oa3.codegen.test.main.schema.ArrayInProperty.Counts;
import org.davidmoten.oa3.codegen.test.main.schema.ArrayOfComplexType;
import org.davidmoten.oa3.codegen.test.main.schema.ArrayOfComplexType.ArrayOfComplexTypeItem;
import org.davidmoten.oa3.codegen.test.main.schema.ArrayOfOneOf;
import org.davidmoten.oa3.codegen.test.main.schema.ArrayOfOneOf.ArrayOfOneOfItem;
import org.davidmoten.oa3.codegen.test.main.schema.ArrayOfOneOfString;
import org.davidmoten.oa3.codegen.test.main.schema.ArrayOfOneOfString.ArrayOfOneOfStringItem;
import org.davidmoten.oa3.codegen.test.main.schema.Bike;
import org.davidmoten.oa3.codegen.test.main.schema.Breeding;
import org.davidmoten.oa3.codegen.test.main.schema.Broadcast;
import org.davidmoten.oa3.codegen.test.main.schema.Circle;
import org.davidmoten.oa3.codegen.test.main.schema.Dog;
import org.davidmoten.oa3.codegen.test.main.schema.Dog.Object1.Breed;
import org.davidmoten.oa3.codegen.test.main.schema.Dog2;
import org.davidmoten.oa3.codegen.test.main.schema.DogBreed;
import org.davidmoten.oa3.codegen.test.main.schema.EnumCollision;
import org.davidmoten.oa3.codegen.test.main.schema.EnumRepeated;
import org.davidmoten.oa3.codegen.test.main.schema.ExclusiveMinMaxInteger;
import org.davidmoten.oa3.codegen.test.main.schema.External;
import org.davidmoten.oa3.codegen.test.main.schema.Geometry;
import org.davidmoten.oa3.codegen.test.main.schema.Latitude;
import org.davidmoten.oa3.codegen.test.main.schema.Longitude;
import org.davidmoten.oa3.codegen.test.main.schema.MetBroadcast;
import org.davidmoten.oa3.codegen.test.main.schema.MetBroadcastArea;
import org.davidmoten.oa3.codegen.test.main.schema.MinMaxDouble;
import org.davidmoten.oa3.codegen.test.main.schema.MinMaxInteger;
import org.davidmoten.oa3.codegen.test.main.schema.MinMaxItems;
import org.davidmoten.oa3.codegen.test.main.schema.MinMaxItemsObjectRef;
import org.davidmoten.oa3.codegen.test.main.schema.MinMaxLength;
import org.davidmoten.oa3.codegen.test.main.schema.Msi;
import org.davidmoten.oa3.codegen.test.main.schema.MsiId;
import org.davidmoten.oa3.codegen.test.main.schema.NamesWithSpaces;
import org.davidmoten.oa3.codegen.test.main.schema.ObjectAllOptionalFields;
import org.davidmoten.oa3.codegen.test.main.schema.ObjectNoOptionalFields;
import org.davidmoten.oa3.codegen.test.main.schema.Pet;
import org.davidmoten.oa3.codegen.test.main.schema.PropertyAnonymous;
import org.davidmoten.oa3.codegen.test.main.schema.PropertyNotRequired;
import org.davidmoten.oa3.codegen.test.main.schema.PropertyRef;
import org.davidmoten.oa3.codegen.test.main.schema.PropertyRefOptional;
import org.davidmoten.oa3.codegen.test.main.schema.Ref;
import org.davidmoten.oa3.codegen.test.main.schema.Shape;
import org.davidmoten.oa3.codegen.test.main.schema.Shape2;
import org.davidmoten.oa3.codegen.test.main.schema.SimpleBinary;
import org.davidmoten.oa3.codegen.test.main.schema.SimpleBoolean;
import org.davidmoten.oa3.codegen.test.main.schema.SimpleByteArray;
import org.davidmoten.oa3.codegen.test.main.schema.SimpleDate;
import org.davidmoten.oa3.codegen.test.main.schema.SimpleDateTime;
import org.davidmoten.oa3.codegen.test.main.schema.SimpleDouble;
import org.davidmoten.oa3.codegen.test.main.schema.SimpleEnum;
import org.davidmoten.oa3.codegen.test.main.schema.SimpleFloat;
import org.davidmoten.oa3.codegen.test.main.schema.SimpleInt;
import org.davidmoten.oa3.codegen.test.main.schema.SimpleInteger;
import org.davidmoten.oa3.codegen.test.main.schema.SimpleIntegerArray;
import org.davidmoten.oa3.codegen.test.main.schema.SimpleLong;
import org.davidmoten.oa3.codegen.test.main.schema.SimpleString;
import org.davidmoten.oa3.codegen.test.main.schema.SimpleTime;
import org.davidmoten.oa3.codegen.test.main.schema.SingleNotOptional;
import org.davidmoten.oa3.codegen.test.main.schema.SingleOptional;
import org.davidmoten.oa3.codegen.test.main.schema.Square;
import org.davidmoten.oa3.codegen.test.main.schema.Square2;
import org.davidmoten.oa3.codegen.test.main.schema.Status;
import org.davidmoten.oa3.codegen.test.main.schema.Table;
import org.davidmoten.oa3.codegen.test.main.schema.Table.TableItem;
import org.davidmoten.oa3.codegen.test.main.schema.UntypedObject;
import org.davidmoten.oa3.codegen.test.main.schema.Vehicle;
import org.davidmoten.oa3.codegen.util.Util;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.ValueInstantiationException;
import com.github.davidmoten.guavamini.Lists;

public class SchemasTest {

    private static final ObjectMapper m = Globals.config().mapper();

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
    public void testSimpleEnum() throws JsonMappingException, JsonProcessingException {
        String json = "\"there\"";
        SimpleEnum a = m.readValue(json, SimpleEnum.class);
        assertEquals(SimpleEnum.THERE, a);
        assertEquals("there", a.value());
        assertEquals(json, m.writeValueAsString(a));
        assertEquals(SimpleEnum.THERE, SimpleEnum.fromValue("there"));
        assertEquals(0, SimpleEnum.class.getConstructors().length);
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
        assertEquals(123, a.num());
        // test constructor
        assertEquals(json, m.writeValueAsString(new ObjectNoOptionalFields("hello", 123)));
        assertEquals(1, ObjectNoOptionalFields.class.getConstructors().length);
        try {
            m.readValue("{}", ObjectNoOptionalFields.class);
            fail();
        } catch (ValueInstantiationException e) {
            // expected
        }
        shouldThrowIAE(() -> new ObjectNoOptionalFields(null, 123));
        onePublicConstructor(ObjectNoOptionalFields.class);
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
        onePublicConstructor(Bike.class);
    }

    @Test
    public void testSingleDiscriminationPolymorphism() throws JsonMappingException, JsonProcessingException {
        String json = "{\"shapeType\":\"square\"}";
        Shape s = m.readValue(json, Shape.class);
        assertEquals("square", s.shapeType());
        assertEquals(json, m.writeValueAsString(s));
        assertEquals(json, m.writeValueAsString(new Square()));
        onePublicConstructor(Square.class);
    }

    @Test
    public void testDiscriminatorWithoutMapping() throws JsonMappingException, JsonProcessingException {
        String json = "{\"shapeType\":\"Square2\"}";
        Shape2 s = m.readValue(json, Shape2.class);
        assertEquals("Square2", s.shapeType());
        assertEquals(json, m.writeValueAsString(s));
        assertEquals(json, m.writeValueAsString(new Square2()));
        onePublicConstructor(Square2.class);
    }

    @Test
    public void testRef() throws JsonMappingException, JsonProcessingException {
        String json = "123";
        Ref r = m.readValue(json, Ref.class);
        assertEquals(123, r.value().value());
        assertEquals(json, m.writeValueAsString(r));
        assertEquals(json, m.writeValueAsString(new Ref(new SimpleInteger(123))));
        shouldThrowIAE(() -> new Ref(null));
        onePublicConstructor(Ref.class);
    }

    @Test
    public void testPropertyRef() throws JsonMappingException, JsonProcessingException {
        String json = "{\"first\":123}";
        PropertyRef r = m.readValue(json, PropertyRef.class);
        assertEquals(123, r.first().value());
        assertEquals(json, m.writeValueAsString(r));
        assertEquals(json, m.writeValueAsString(new PropertyRef(new SimpleInteger(123))));
        shouldThrowIAE(() -> new PropertyRef(null));
        onePublicConstructor(PropertyRef.class);
    }

    @Test
    public void testPropertyRefOptional() throws JsonMappingException, JsonProcessingException {
        String json = "{\"first\":123}";
        PropertyRefOptional r = m.readValue(json, PropertyRefOptional.class);
        assertEquals(123, r.first().get().value());
        assertEquals(json, m.writeValueAsString(r));
        assertEquals(json, m.writeValueAsString(new PropertyRefOptional(Optional.of(new SimpleInteger(123)))));
        shouldThrowIAE(() -> new PropertyRefOptional(null));
        onePublicConstructor(PropertyRefOptional.class);
    }

    @Test
    public void testMinMaxLengthAreOk() throws JsonMappingException, JsonProcessingException {
        String json = "{\"first\":\"abc\",\"second\":\"def\"}";
        MinMaxLength a = m.readValue(json, MinMaxLength.class);
        assertEquals("abc", a.first());
        assertEquals("def", a.second().get());
        assertEquals(json, m.writeValueAsString(a));
        assertEquals(json, m.writeValueAsString(new MinMaxLength("abc", Optional.of("def"))));
        onePublicConstructor(MinMaxLength.class);
    }

    @Test
    public void testMinMaxLengthTooSmall() throws JsonMappingException, JsonProcessingException {
        assertThrows(ValueInstantiationException.class, () -> {
            String json = "{\"first\":\"\",\"second\":\"def\"}";
            m.readValue(json, MinMaxLength.class);
        });
    }

    @Test
    public void testMinMaxLengthTooSmallPublicConstructor() throws JsonMappingException, JsonProcessingException {
        assertThrows(IllegalArgumentException.class, () -> new MinMaxLength("", Optional.of("def")));
    }

    @Test
    public void testMinMaxLengthTooSmallOptional() throws JsonMappingException, JsonProcessingException {
        assertThrows(ValueInstantiationException.class, () -> {
            String json = "{\"first\":\"abc\",\"second\":\"d\"}";
            m.readValue(json, MinMaxLength.class);
        });
    }

    @Test
    public void testMinMaxLengthTooSmallOptionalPublicConstructor()
            throws JsonMappingException, JsonProcessingException {
        assertThrows(IllegalArgumentException.class, () -> new MinMaxLength("abc", Optional.of("d")));
    }

    @Test
    public void testMinMaxLengthTooBig() throws JsonMappingException, JsonProcessingException {
        assertThrows(ValueInstantiationException.class, () -> {
            String json = "{\"first\":\"abcdef\",\"second\":\"def\"}";
            m.readValue(json, MinMaxLength.class);
        });
    }

    @Test
    public void testMinMaxLengthTooBigPublicConstructor() throws JsonMappingException, JsonProcessingException {
        assertThrows(IllegalArgumentException.class, () -> new MinMaxLength("abcdef", Optional.of("def")));
    }

    @Test
    public void testMinMaxLengthTooBigOptional() throws JsonMappingException, JsonProcessingException {
        assertThrows(ValueInstantiationException.class, () -> {
            String json = "{\"abc\":\"abc\",\"second\":\"defgh\"}";
            m.readValue(json, MinMaxLength.class);
        });
    }

    @Test
    public void testMinMaxLengthTooBigOptionalPublicConstructor() throws JsonMappingException, JsonProcessingException {
        assertThrows(IllegalArgumentException.class, () -> new MinMaxLength("abc", Optional.of("defgh")));
    }

    @Test
    public void testMinMaxObjectRefMinItemsIsOk() throws JsonMappingException, JsonProcessingException {
        MinMaxItemsObjectRef.List list = new MinMaxItemsObjectRef.List(
                Arrays.asList(new MinMaxInteger(2), new MinMaxInteger(2)));
        new MinMaxItemsObjectRef(Optional.of(list));
    }

    @Test
    public void testMinMaxObjectRefMinItemsIsBad() throws JsonMappingException, JsonProcessingException {
        assertThrows(IllegalArgumentException.class,
                () -> new MinMaxItemsObjectRef.List(Arrays.asList(new MinMaxInteger(2))));
    }

    @Test
    public void testMinMaxObjectRefMaxItemsIsBad() throws JsonMappingException, JsonProcessingException {
        assertThrows(IllegalArgumentException.class, () -> new MinMaxItemsObjectRef.List(
                IntStream.rangeClosed(1, 5).mapToObj(i -> new MinMaxInteger(2)).collect(Collectors.toList())));
    }

    @Test
    public void testTurnValidateOff() {
        Config old = Globals.config();
        try {
            Config config = Config.builder().mapper(Globals.config().mapper()).validateInConstructor(x -> false)
                    .build();
            Globals.setConfig(config);
            new MinMaxLength("abc", Optional.of("defgh"));
        } finally {
            Globals.setConfig(old);
        }
    }

    @Test
    public void testMinMaxIntegerPasses() throws JsonMappingException, JsonProcessingException {
        String json = "2";
        MinMaxInteger a = m.readValue(json, MinMaxInteger.class);
        assertEquals(2, a.value());
        assertEquals(json, m.writeValueAsString(a));
        assertEquals(json, m.writeValueAsString(new MinMaxInteger(2)));
    }

    @Test
    public void testMinMaxIntegerTooSmall() throws JsonMappingException, JsonProcessingException {
        assertThrows(ValueInstantiationException.class, () -> {
            String json = "1";
            m.readValue(json, MinMaxInteger.class);
        });
    }

    @Test
    public void testMinMaxIntegerTooBig() throws JsonMappingException, JsonProcessingException {
        assertThrows(ValueInstantiationException.class, () -> {
            String json = "5";
            m.readValue(json, MinMaxInteger.class);
        });
    }

    @Test
    public void testMinMaxDoublePasses() throws JsonMappingException, JsonProcessingException {
        String json = "2.6";
        MinMaxDouble a = m.readValue(json, MinMaxDouble.class);
        assertEquals(2.6, a.value(), 0.00001);
        assertEquals(json, m.writeValueAsString(a));
        assertEquals(json, m.writeValueAsString(new MinMaxDouble(2.6)));
    }

    @Test
    public void testMinMaxDoubleTooSmall() throws JsonMappingException, JsonProcessingException {
        assertThrows(ValueInstantiationException.class, () -> {
            String json = "1.2";
            m.readValue(json, MinMaxDouble.class);
        });
    }

    @Test
    public void testMinMaxDoubleTooBig() throws JsonMappingException, JsonProcessingException {
        assertThrows(ValueInstantiationException.class, () -> {
            String json = "5.0";
            m.readValue(json, MinMaxDouble.class);
        });
    }

    // TODO need to address this failure
//    @Test
    public void testMinMaxDoubleTooBigWithNoDecimalPart() throws JsonMappingException, JsonProcessingException {
        assertThrows(ValueInstantiationException.class, () -> {
            String json = "5";
            m.readValue(json, MinMaxDouble.class);
        });
    }

    @Test
    public void testExclusiveMinMaxIntegerPasses() throws JsonMappingException, JsonProcessingException {
        String json = "3";
        ExclusiveMinMaxInteger a = m.readValue(json, ExclusiveMinMaxInteger.class);
        assertEquals(3, a.value());
        assertEquals(json, m.writeValueAsString(a));
        assertEquals(json, m.writeValueAsString(new ExclusiveMinMaxInteger(3)));
    }

    @Test
    public void testExclusiveMinMaxIntegerTooSmall() throws JsonMappingException, JsonProcessingException {
        assertThrows(ValueInstantiationException.class, () -> {
            String json = "2";
            m.readValue(json, ExclusiveMinMaxInteger.class);
        });
    }

    @Test
    public void testExclusiveMinMaxIntegerTooBig() throws JsonMappingException, JsonProcessingException {
        assertThrows(ValueInstantiationException.class, () -> {
            String json = "4";
            m.readValue(json, ExclusiveMinMaxInteger.class);
        });
    }

    @Test
    public void testNamesWithSpaces() throws JsonMappingException, JsonProcessingException {
        String json = "{\"the name\":\"julie\"}";
        NamesWithSpaces a = m.readValue(json, NamesWithSpaces.class);
        assertEquals("julie", a.the_name().get());
        assertEquals(json, m.writeValueAsString(a));
    }

    @Test
    public void testMinMaxItems() throws JsonMappingException, JsonProcessingException {
        String json = "[1,2,3]";
        m.readValue(json, MinMaxItems.class);
    }

    @Test
    public void testMinMaxItemsTooFew() throws JsonMappingException, JsonProcessingException {
        assertThrows(ValueInstantiationException.class, () -> {
            String json = "[1]";
            m.readValue(json, MinMaxItems.class);
        });
    }

    @Test
    public void testMinMaxItemsTooMany() throws JsonMappingException, JsonProcessingException {
        assertThrows(ValueInstantiationException.class, () -> {
            String json = "[1,2,3,4,5]";
            m.readValue(json, MinMaxItems.class);
        });
    }

    @Test
    public void testEnum() {
        assertEquals(3, EnumCollision.values().length);
    }

    @Test
    public void testEnumRepeated() {
        assertEquals(2, EnumRepeated.values().length);
    }

    @Test
    public void testAllOfWithAnonymousType() throws JsonProcessingException {
        String json = "{\"description\":\"brown and curly\",\"breed\":\"cross\"}";
        Dog a = m.readValue(json, Dog.class);
        assertEquals("brown and curly", a.pet().description());
        assertEquals(Breed.CROSS, a.object1().breed().get());
        Dog b = Dog.builder() //
                .pet(Pet.description("brown and curly")) //
                .object1(Dog.Object1.breed(Breed.CROSS)) //
                .build();
        assertEquals(json, m.writeValueAsString(b));
    }

    @Test
    public void testAllOfWithRefs() throws JsonProcessingException {
        String json = "{\"description\":\"brown and curly\",\"breeder\":\"Jane's Kennels\",\"breed\":\"cross\"}";
        Dog2 a = m.readValue(json, Dog2.class);
        assertEquals("brown and curly", a.pet().description());
        assertEquals(DogBreed.CROSS, a.breeding().breed());
        Dog2 b = Dog2.builder() //
                .pet(Pet.description("brown and curly")) //
                .breeding(Breeding.builder() //
                        .breeder("Jane's Kennels") //
                        .breed(DogBreed.CROSS) //
                        .build()) //
                .build();
        assertEquals(json, m.writeValueAsString(b));
    }

    @Test
    public void testPropertyNotRequired() throws JsonMappingException, JsonProcessingException {
        String json = "{\"name\":\"boo\"}";
        PropertyNotRequired a = m.readValue(json, PropertyNotRequired.class);
        assertEquals("boo", a.name().get());
        assertEquals(json, m.writeValueAsString(new PropertyNotRequired(Optional.of("boo"))));
        PropertyNotRequired b = m.readValue("{}", PropertyNotRequired.class);
        assertEquals("{}", m.writeValueAsString(new PropertyNotRequired(Optional.empty())));
        assertFalse(b.name().isPresent());
    }

    @Test
    public void testPropertyAnonymous() throws JsonMappingException, JsonProcessingException {
        String json = "{}";
        PropertyAnonymous a = m.readValue(json, PropertyAnonymous.class);
        assertFalse(a.name().isPresent());
        json = "{\"name\":{\"first\":\"Anne\",\"second\":\"Smith\"}}";
        PropertyAnonymous b = m.readValue(json, PropertyAnonymous.class);
        assertEquals("Anne", b.name().get().first().get());
    }

    @Test
    public void testArrayInProperty() throws JsonMappingException, JsonProcessingException {
        String json = "{\"counts\":[1,2,3]}";
        ArrayInProperty a = m.readValue(json, ArrayInProperty.class);
        assertEquals(Arrays.asList(1, 2, 3), a.counts().value());
        assertEquals(json, m.writeValueAsString(a));
        ArrayInProperty b = new ArrayInProperty(new Counts(Arrays.asList(1, 2, 3)));
        assertEquals(json, m.writeValueAsString(b));
        onePublicConstructor(ArrayInProperty.class);
        onePublicConstructor(Counts.class);
    }

    @Test
    public void testArrayOfArray() throws JsonProcessingException {
        String json = "[[1,2,3],[4,5,6]]";
        Table a = m.readValue(json, Table.class);
        assertEquals(Arrays.asList(1, 2, 3), a.value().get(0).value());
        Table b = new Table(
                Arrays.asList(new TableItem(Arrays.asList(1, 2, 3)), new TableItem(Arrays.asList(4, 5, 6))));
        assertEquals(json, m.writeValueAsString(b));
        onePublicConstructor(Table.class);
        onePublicConstructor(TableItem.class);
    }

    @Test
    public void testMsi() throws JsonProcessingException {
        String json = "{\"id\":\"8ds9f8sd98-dsfds8989\",\"broadcast\":{\"area\":{\"lat\":25.1,\"lon\":-33.1,\"radiusNm\":1.0}},\"createdTime\":\"2023-04-05T12:15:26.025+10:00\",\"startTime\":\"2023-04-05T14:15:26.025+10:00\",\"endTime\":\"2023-04-06T12:00:26.025+10:00\",\"status\":\"ACTIVE\"}\n";
        {
            Msi a = m.readValue(json, Msi.class);
            MetBroadcast b = (MetBroadcast) a.broadcast().value();
            Geometry g = (Geometry) b.area().value();
            Circle c = (Circle) g.value();
            assertEquals(25.1, c.lat().value(), 0.0001);
        }
        {
            Circle circle = new Circle(new Latitude(25.1F), new Longitude(-33.1F), 1);
            MetBroadcastArea mbca = MetBroadcastArea.of(Geometry.of(circle));
            MetBroadcast mbc = MetBroadcast.builder().area(mbca).build();
            Broadcast broadcast = Broadcast.of(mbc);
            OffsetDateTime createdTime = OffsetDateTime.parse("2023-04-05T12:15:26.025+10:00");
            OffsetDateTime startTime = OffsetDateTime.parse("2023-04-05T14:15:26.025+10:00");
            OffsetDateTime endTime = OffsetDateTime.parse("2023-04-06T12:00:26.025+10:00");
            MsiId msiId = new MsiId("8ds9f8sd98-dsfds8989");
            Msi msi = Msi.builder() //
                    .id(msiId) //
                    .broadcast(broadcast) //
                    .createdTime(createdTime) //
                    .startTime(startTime) //
                    .endTime(endTime) //
                    .status(Status.ACTIVE) //
                    .build();
            assertEquals(m.readTree(json), m.readTree(m.writeValueAsString(msi)));
        }
    }

    @Test
    public void testExternalRefIsResolved() {
        new External("hello");
        try {
            new External("hellotherehowareyou");
            fail();
        } catch (IllegalArgumentException e) {
            // all good
        }
    }

    @Test
    public void testBuilderOptionalPrimitive() throws NoSuchMethodException, SecurityException {
        SingleOptional.class.getMethod("single", int.class);
        assertEquals(1, SingleOptional.single(1).single().get());
        assertEquals(2, SingleOptional.single(Optional.of(2)).single().get());
        assertFalse(SingleOptional.single(Optional.empty()).single().isPresent());
    }

    @Test
    public void testBuilderNotOptionalPrimitive() throws NoSuchMethodException, SecurityException {
        SingleNotOptional.class.getMethod("single", int.class);
        assertEquals(1, SingleNotOptional.single(1).single());
    }

    @Test
    public void testAdditionalProperties() throws JsonProcessingException {
        AdditionalProperties a = AdditionalProperties.builder() //
                .add("hello", 1L) //
                .add("there", 23L) //
                .buildMap() //
                .age(21) //
                .name("fred") //
                .build();
        String json = m.writeValueAsString(a);
        JsonNode tree = m.readTree(json);
        assertEquals("fred", tree.get("name").asText());
        assertEquals(21, tree.get("age").asInt());
        assertEquals(1L, a.map().get("hello"));
        assertEquals(23L, a.map().get("there"));
        AdditionalProperties b = m.readValue(json, AdditionalProperties.class);
        assertEquals("fred", b.name().get());
        assertEquals(21, b.age().get());
        assertEquals(1L, b.map().get("hello"));
        assertEquals(23L, b.map().get("there"));
        Geometry g = Geometry.of(Circle.builder() //
                .lat(Latitude.value(-35f)) //
                .lon(Longitude.value(142f)) //
                .radiusNm(20) //
                .build());
    }

    @Test
    public void testAdditionalPropertiesTrue() throws JsonProcessingException {
        Circle c = Circle.builder().lat(Latitude.value(11f)).lon(Longitude.value(123f)).radiusNm(123).build();
        AdditionalPropertiesTrue a = AdditionalPropertiesTrue.builder() //
                .add("hello", 1L) //
                .add("there", c) //
                .buildMap() //
                .age(21) //
                .name("fred") //
                .build();
        String json = m.writeValueAsString(a);
        System.out.println(json);
        JsonNode tree = m.readTree(json);
        assertEquals("fred", tree.get("name").asText());
        assertEquals(21, tree.get("age").asInt());
        assertEquals(1L, a.map().get("hello"));
        assertEquals(c, a.map().get("there"));
        AdditionalPropertiesTrue b = m.readValue(json, AdditionalPropertiesTrue.class);
        assertEquals("fred", b.name().get());
        assertEquals(21, b.age().get());
        assertEquals(1, b.map().get("hello"));
        @SuppressWarnings("unchecked")
        Map<String, Object> circle = (Map<String, Object>) b.map().get("there");
        assertEquals(11.0, (Double) circle.get("lat"), 0.0001);
        assertEquals(123.0, (Double) circle.get("lon"), 0.0001);
    }
    
    @Test
    public void testUntypedObjectHasAdditionalProperties() {
        UntypedObject o = new UntypedObject();
        // TODO should have map method
    }

    private static void onePublicConstructor(Class<?> c) {
        assertEquals(1, c.getConstructors().length);
    }

}
