package org.davidmoten.oa3.codegen.test;

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

import org.davidmoten.oa3.codegen.runtime.Config;
import org.davidmoten.oa3.codegen.runtime.internal.Util;
import org.davidmoten.oa3.codegen.test.generated.Globals;
import org.davidmoten.oa3.codegen.test.generated.schema.ArrayInProperty;
import org.davidmoten.oa3.codegen.test.generated.schema.ArrayInProperty.Counts;
import org.davidmoten.oa3.codegen.test.generated.schema.ArrayOfComplexType;
import org.davidmoten.oa3.codegen.test.generated.schema.ArrayOfComplexType.ArrayOfComplexTypeItem;
import org.davidmoten.oa3.codegen.test.generated.schema.ArrayOfOneOf;
import org.davidmoten.oa3.codegen.test.generated.schema.ArrayOfOneOf.ArrayOfOneOfItem;
import org.davidmoten.oa3.codegen.test.generated.schema.ArrayOfOneOfString;
import org.davidmoten.oa3.codegen.test.generated.schema.ArrayOfOneOfString.ArrayOfOneOfStringItem;
import org.davidmoten.oa3.codegen.test.generated.schema.Bike;
import org.davidmoten.oa3.codegen.test.generated.schema.Breeding;
import org.davidmoten.oa3.codegen.test.generated.schema.Broadcast;
import org.davidmoten.oa3.codegen.test.generated.schema.Circle;
import org.davidmoten.oa3.codegen.test.generated.schema.Dog;
import org.davidmoten.oa3.codegen.test.generated.schema.Dog.Object1.Breed;
import org.davidmoten.oa3.codegen.test.generated.schema.Dog2;
import org.davidmoten.oa3.codegen.test.generated.schema.DogBreed;
import org.davidmoten.oa3.codegen.test.generated.schema.EnumCollision;
import org.davidmoten.oa3.codegen.test.generated.schema.EnumRepeated;
import org.davidmoten.oa3.codegen.test.generated.schema.ExclusiveMinMaxInteger;
import org.davidmoten.oa3.codegen.test.generated.schema.External;
import org.davidmoten.oa3.codegen.test.generated.schema.Geometry;
import org.davidmoten.oa3.codegen.test.generated.schema.Latitude;
import org.davidmoten.oa3.codegen.test.generated.schema.Longitude;
import org.davidmoten.oa3.codegen.test.generated.schema.MetBroadcast;
import org.davidmoten.oa3.codegen.test.generated.schema.MetBroadcastArea;
import org.davidmoten.oa3.codegen.test.generated.schema.MinMaxDouble;
import org.davidmoten.oa3.codegen.test.generated.schema.MinMaxInteger;
import org.davidmoten.oa3.codegen.test.generated.schema.MinMaxItems;
import org.davidmoten.oa3.codegen.test.generated.schema.MinMaxLength;
import org.davidmoten.oa3.codegen.test.generated.schema.Msi;
import org.davidmoten.oa3.codegen.test.generated.schema.MsiId;
import org.davidmoten.oa3.codegen.test.generated.schema.NamesWithSpaces;
import org.davidmoten.oa3.codegen.test.generated.schema.ObjectAllOptionalFields;
import org.davidmoten.oa3.codegen.test.generated.schema.ObjectNoOptionalFields;
import org.davidmoten.oa3.codegen.test.generated.schema.Pet;
import org.davidmoten.oa3.codegen.test.generated.schema.PropertyAnonymous;
import org.davidmoten.oa3.codegen.test.generated.schema.PropertyNotRequired;
import org.davidmoten.oa3.codegen.test.generated.schema.PropertyRef;
import org.davidmoten.oa3.codegen.test.generated.schema.PropertyRefOptional;
import org.davidmoten.oa3.codegen.test.generated.schema.Ref;
import org.davidmoten.oa3.codegen.test.generated.schema.Shape;
import org.davidmoten.oa3.codegen.test.generated.schema.Shape2;
import org.davidmoten.oa3.codegen.test.generated.schema.SimpleBinary;
import org.davidmoten.oa3.codegen.test.generated.schema.SimpleBoolean;
import org.davidmoten.oa3.codegen.test.generated.schema.SimpleByteArray;
import org.davidmoten.oa3.codegen.test.generated.schema.SimpleDate;
import org.davidmoten.oa3.codegen.test.generated.schema.SimpleDateTime;
import org.davidmoten.oa3.codegen.test.generated.schema.SimpleDouble;
import org.davidmoten.oa3.codegen.test.generated.schema.SimpleEnum;
import org.davidmoten.oa3.codegen.test.generated.schema.SimpleFloat;
import org.davidmoten.oa3.codegen.test.generated.schema.SimpleInt;
import org.davidmoten.oa3.codegen.test.generated.schema.SimpleInteger;
import org.davidmoten.oa3.codegen.test.generated.schema.SimpleIntegerArray;
import org.davidmoten.oa3.codegen.test.generated.schema.SimpleLong;
import org.davidmoten.oa3.codegen.test.generated.schema.SimpleString;
import org.davidmoten.oa3.codegen.test.generated.schema.SimpleTime;
import org.davidmoten.oa3.codegen.test.generated.schema.Square;
import org.davidmoten.oa3.codegen.test.generated.schema.Square2;
import org.davidmoten.oa3.codegen.test.generated.schema.Status;
import org.davidmoten.oa3.codegen.test.generated.schema.Table;
import org.davidmoten.oa3.codegen.test.generated.schema.Table.TableItem;
import org.davidmoten.oa3.codegen.test.generated.schema.Vehicle;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.ValueInstantiationException;
import com.github.davidmoten.guavamini.Lists;

public class PluginGeneratorTest {

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

    @Test(expected = ValueInstantiationException.class)
    public void testMinMaxLengthTooSmall() throws JsonMappingException, JsonProcessingException {
        String json = "{\"first\":\"\",\"second\":\"def\"}";
        m.readValue(json, MinMaxLength.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMinMaxLengthTooSmallPublicConstructor() throws JsonMappingException, JsonProcessingException {
        new MinMaxLength("", Optional.of("def"));
    }

    @Test(expected = ValueInstantiationException.class)
    public void testMinMaxLengthTooSmallOptional() throws JsonMappingException, JsonProcessingException {
        String json = "{\"first\":\"abc\",\"second\":\"d\"}";
        m.readValue(json, MinMaxLength.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMinMaxLengthTooSmallOptionalPublicConstructor()
            throws JsonMappingException, JsonProcessingException {
        new MinMaxLength("abc", Optional.of("d"));
    }

    @Test(expected = ValueInstantiationException.class)
    public void testMinMaxLengthTooBig() throws JsonMappingException, JsonProcessingException {
        String json = "{\"first\":\"abcdef\",\"second\":\"def\"}";
        m.readValue(json, MinMaxLength.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMinMaxLengthTooBigPublicConstructor() throws JsonMappingException, JsonProcessingException {
        new MinMaxLength("abcdef", Optional.of("def"));
    }

    @Test(expected = ValueInstantiationException.class)
    public void testMinMaxLengthTooBigOptional() throws JsonMappingException, JsonProcessingException {
        String json = "{\"abc\":\"abc\",\"second\":\"defgh\"}";
        m.readValue(json, MinMaxLength.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMinMaxLengthTooBigOptionalPublicConstructor() throws JsonMappingException, JsonProcessingException {
        new MinMaxLength("abc", Optional.of("defgh"));
    }

    @Test
    public void testTurnValidateOff() {
        Config old = Globals.config();
        try {
            Config config = Config.builder().mapper(Globals.config().mapper()).validateInConstructor(x -> false).build();
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

    @Test(expected = ValueInstantiationException.class)
    public void testMinMaxIntegerTooSmall() throws JsonMappingException, JsonProcessingException {
        String json = "1";
        m.readValue(json, MinMaxInteger.class);
    }

    @Test(expected = ValueInstantiationException.class)
    public void testMinMaxIntegerTooBig() throws JsonMappingException, JsonProcessingException {
        String json = "5";
        m.readValue(json, MinMaxInteger.class);
    }

    @Test
    public void testMinMaxDoublePasses() throws JsonMappingException, JsonProcessingException {
        String json = "2.6";
        MinMaxDouble a = m.readValue(json, MinMaxDouble.class);
        assertEquals(2.6, a.value(), 0.00001);
        assertEquals(json, m.writeValueAsString(a));
        assertEquals(json, m.writeValueAsString(new MinMaxDouble(2.6)));
    }

    @Test(expected = ValueInstantiationException.class)
    public void testMinMaxDoubleTooSmall() throws JsonMappingException, JsonProcessingException {
        String json = "1.2";
        m.readValue(json, MinMaxDouble.class);
    }

    @Test(expected = ValueInstantiationException.class)
    public void testMinMaxDoubleTooBig() throws JsonMappingException, JsonProcessingException {
        String json = "5.0";
        m.readValue(json, MinMaxDouble.class);
    }

    // TODO need to address this failure
    @Test(expected = ValueInstantiationException.class)
    @Ignore
    public void testMinMaxDoubleTooBigWithNoDecimalPart() throws JsonMappingException, JsonProcessingException {
        String json = "5";
        m.readValue(json, MinMaxDouble.class);
    }

    @Test
    public void testExclusiveMinMaxIntegerPasses() throws JsonMappingException, JsonProcessingException {
        String json = "3";
        ExclusiveMinMaxInteger a = m.readValue(json, ExclusiveMinMaxInteger.class);
        assertEquals(3, a.value());
        assertEquals(json, m.writeValueAsString(a));
        assertEquals(json, m.writeValueAsString(new ExclusiveMinMaxInteger(3)));
    }

    @Test(expected = ValueInstantiationException.class)
    public void testExclusiveMinMaxIntegerTooSmall() throws JsonMappingException, JsonProcessingException {
        String json = "2";
        m.readValue(json, ExclusiveMinMaxInteger.class);
    }

    @Test(expected = ValueInstantiationException.class)
    public void testExclusiveMinMaxIntegerTooBig() throws JsonMappingException, JsonProcessingException {
        String json = "4";
        m.readValue(json, ExclusiveMinMaxInteger.class);
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

    @Test(expected = ValueInstantiationException.class)
    public void testMinMaxItemsTooFew() throws JsonMappingException, JsonProcessingException {
        String json = "[1]";
        m.readValue(json, MinMaxItems.class);
    }

    @Test(expected = ValueInstantiationException.class)
    public void testMinMaxItemsTooMany() throws JsonMappingException, JsonProcessingException {
        String json = "[1,2,3,4,5]";
        m.readValue(json, MinMaxItems.class);
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
        Dog b = new Dog(new Pet("brown and curly"), new Dog.Object1(Optional.of(Breed.CROSS)));
        assertEquals(json, m.writeValueAsString(b));
    }

    @Test
    public void testAllOfWithRefs() throws JsonProcessingException {
        String json = "{\"description\":\"brown and curly\",\"breeder\":\"Jane's Kennels\",\"breed\":\"cross\"}";
        Dog2 a = m.readValue(json, Dog2.class);
        assertEquals("brown and curly", a.pet().description());
        assertEquals(DogBreed.CROSS, a.breeding().breed());
        Dog2 b = new Dog2(new Pet("brown and curly"), new Breeding("Jane's Kennels", DogBreed.CROSS));
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
            MetBroadcastArea mbca = new MetBroadcastArea(new Geometry(circle));
            MetBroadcast mbc = new MetBroadcast(mbca, Optional.empty());
            Broadcast broadcast = new Broadcast(mbc);
            OffsetDateTime createdTime = OffsetDateTime.parse("2023-04-05T12:15:26.025+10:00");
            OffsetDateTime startTime = OffsetDateTime.parse("2023-04-05T14:15:26.025+10:00");
            OffsetDateTime endTime = OffsetDateTime.parse("2023-04-06T12:00:26.025+10:00");
            MsiId msiId = new MsiId("8ds9f8sd98-dsfds8989");
            Msi msi = new Msi(msiId, broadcast, createdTime, Optional.empty(), startTime, endTime, Optional.empty(),
                    Status.ACTIVE, Optional.empty(), Optional.empty(), Optional.empty());
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

    private static void onePublicConstructor(Class<?> c) {
        assertEquals(1, c.getConstructors().length);
    }
    
}
