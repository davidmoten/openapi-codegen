package org.davidmoten.oa3.codegen.runtime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.exc.InputCoercionException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class StrictDeserializersTest {

    @Test
    public void testBooleanStrictDeserializer() throws JsonMappingException, JsonProcessingException {
        // demonstrate original non-strict behaviour
        {
            ObjectMapper mOriginal = new ObjectMapper();
            assertFalse(mOriginal.readValue("false", Boolean.class));
            assertTrue(mOriginal.readValue("true", Boolean.class));
            assertTrue(mOriginal.readValue("2", Boolean.class));
            assertFalse(mOriginal.readValue("0", Boolean.class));
            assertTrue(mOriginal.readValue("\"true\"", Boolean.class));
            assertFalse(mOriginal.readValue("\"false\"", Boolean.class));
            assertTrue(mOriginal.readValue("\"TRUE\"", Boolean.class));
            assertFalse(mOriginal.readValue("\"FALSE\"", Boolean.class));
        }

        // demonstrate new strict behaviour
        ObjectMapper m = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(Boolean.class, new StrictBooleanDeserializer());
        m.registerModule(module);

        assertFalse(m.readValue("false", Boolean.class));
        assertTrue(m.readValue("true", Boolean.class));
        assertThrows(MismatchedInputException.class, () -> m.readValue("2", Boolean.class));
        assertThrows(MismatchedInputException.class, () -> m.readValue("0", Boolean.class));
        assertThrows(MismatchedInputException.class, () -> m.readValue("\"true\"", Boolean.class));
        assertThrows(MismatchedInputException.class, () -> m.readValue("\"false\"", Boolean.class));
        assertThrows(MismatchedInputException.class, () -> m.readValue("\"TRUE\"", Boolean.class));
        assertThrows(MismatchedInputException.class, () -> m.readValue("\"FALSE\"", Boolean.class));
    }

    @Test
    public void testShortStrictDeserializer() throws JsonMappingException, JsonProcessingException {

        // demonstrate original non-strict behaviour
        {
            ObjectMapper mOriginal = new ObjectMapper();
            assertEquals(0, mOriginal.readValue("0", Short.class).intValue());
            assertEquals(123, mOriginal.readValue("123", Short.class).intValue());
            assertEquals(123, mOriginal.readValue("\"123\"", Short.class).intValue());
            assertEquals(123, mOriginal.readValue("123.4", Short.class).intValue());
        }

        // demonstrate new strict behaviour
        ObjectMapper m = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(Short.class, new StrictShortDeserializer());
        m.registerModule(module);

        assertEquals(0, m.readValue("0", Integer.class).intValue());
        assertEquals(123, m.readValue("123", Short.class).intValue());
        assertThrows(MismatchedInputException.class, () -> m.readValue("123.4", Short.class));
        assertThrows(MismatchedInputException.class, () -> m.readValue("\"123\"", Short.class));
        assertThrows(InputCoercionException.class, () -> m.readValue("1234567890123", Short.class));
    }

    @Test
    public void testIntegerStrictDeserializer() throws JsonMappingException, JsonProcessingException {

        // demonstrate original non-strict behaviour
        {
            ObjectMapper mOriginal = new ObjectMapper();
            assertEquals(0, mOriginal.readValue("0", Integer.class).intValue());
            assertEquals(123, mOriginal.readValue("123", Integer.class).intValue());
            assertEquals(123, mOriginal.readValue("\"123\"", Integer.class).intValue());
            assertEquals(123, mOriginal.readValue("123.4", Integer.class).intValue());
        }

        // demonstrate new strict behaviour
        ObjectMapper m = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(Integer.class, new StrictIntegerDeserializer());
        m.registerModule(module);

        assertEquals(0, m.readValue("0", Integer.class).intValue());
        assertEquals(123, m.readValue("123", Integer.class).intValue());
        assertThrows(MismatchedInputException.class, () -> m.readValue("123.4", Integer.class));
        assertThrows(MismatchedInputException.class, () -> m.readValue("\"123\"", Integer.class));
        assertThrows(InputCoercionException.class, () -> m.readValue("1234567890123", Integer.class));
    }

    @Test
    public void testLongStrictDeserializer() throws JsonMappingException, JsonProcessingException {

        // demonstrate original non-strict behaviour
        {
            ObjectMapper mOriginal = new ObjectMapper();
            assertEquals(0, mOriginal.readValue("0", Long.class).longValue());
            assertEquals(123, mOriginal.readValue("123", Long.class).longValue());
            assertEquals(123, mOriginal.readValue("\"123\"", Long.class).longValue());
            assertEquals(123, mOriginal.readValue("123.4", Long.class).longValue());
        }

        // demonstrate new strict behaviour
        ObjectMapper m = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(Long.class, new StrictLongDeserializer());
        m.registerModule(module);

        assertEquals(0, m.readValue("0", Long.class).longValue());
        assertEquals(123, m.readValue("123", Long.class).longValue());
        assertEquals(1234567890123L, m.readValue("1234567890123", Long.class).longValue());
        assertThrows(MismatchedInputException.class, () -> m.readValue("123.4", Long.class));
        assertThrows(MismatchedInputException.class, () -> m.readValue("\"123\"", Long.class));
        assertThrows(InputCoercionException.class, () -> m.readValue("123456789012345678901234567890", Long.class));
    }

    @Test
    public void testFloatStrictDeserializer() throws JsonMappingException, JsonProcessingException {

        // demonstrate original non-strict behaviour
        {
            ObjectMapper mOriginal = new ObjectMapper();
            assertEquals(0, mOriginal.readValue("0", Float.class).floatValue());
            assertEquals(123, mOriginal.readValue("123", Float.class).floatValue());
            assertEquals(123, mOriginal.readValue("\"123\"", Float.class).floatValue());
            assertEquals(123.4, mOriginal.readValue("123.4", Float.class).floatValue(), 0.00001);
        }

        // demonstrate new strict behaviour
        ObjectMapper m = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(Float.class, new StrictFloatDeserializer());
        m.registerModule(module);

        assertEquals(0, m.readValue("0", Float.class).floatValue());
        assertEquals(123, m.readValue("123", Float.class).floatValue());
        assertEquals(1234567890123L, m.readValue("1234567890123", Float.class).floatValue());
        assertEquals(123.4, m.readValue("123.4", Float.class).floatValue(), 0.00001);
        assertEquals(1.2345679e29f, m.readValue("123456789012345678901234567890", Float.class));
        assertEquals(Float.POSITIVE_INFINITY, m.readValue("1e300", Float.class));
        assertThrows(MismatchedInputException.class, () -> m.readValue("\"123\"", Float.class));
        assertThrows(MismatchedInputException.class, () -> m.readValue("\"123.4\"", Float.class));
    }

    @Test
    public void testDoubleStrictDeserializer() throws JsonMappingException, JsonProcessingException {

        // demonstrate original non-strict behaviour
        {
            ObjectMapper mOriginal = new ObjectMapper();
            assertEquals(0, mOriginal.readValue("0", Double.class).doubleValue());
            assertEquals(123, mOriginal.readValue("123", Double.class).doubleValue());
            assertEquals(123, mOriginal.readValue("\"123\"", Double.class).doubleValue());
            assertEquals(123.4, mOriginal.readValue("123.4", Double.class).doubleValue(), 0.00001);
        }

        // demonstrate new strict behaviour
        ObjectMapper m = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(Double.class, new StrictDoubleDeserializer());
        m.registerModule(module);

        assertEquals(0, m.readValue("0", Double.class).doubleValue());
        assertEquals(123, m.readValue("123", Double.class).doubleValue());
        assertEquals(1234567890123L, m.readValue("1234567890123", Double.class).doubleValue());
        assertEquals(123.4, m.readValue("123.4", Double.class).doubleValue(), 0.00001);
        assertEquals(1.2345678901234568E29, m.readValue("123456789012345678901234567890", Double.class));
        assertEquals(Double.POSITIVE_INFINITY, m.readValue("1e310", Double.class));
        assertThrows(MismatchedInputException.class, () -> m.readValue("\"123\"", Double.class));
        assertThrows(MismatchedInputException.class, () -> m.readValue("\"123.4\"", Double.class));
    }

    @Test
    public void testLocalDateStrictDeserializer() throws JsonMappingException, JsonProcessingException {

        // demonstrate original non-strict behaviour
        {
            ObjectMapper mOriginal = new ObjectMapper();
            mOriginal.registerModule(new JavaTimeModule());
            assertEquals(LocalDate.of(2024, 11, 30), mOriginal.readValue("\"2024-11-30\"", LocalDate.class));
            assertEquals(LocalDate.of(1970, 1, 1), mOriginal.readValue("0", LocalDate.class));
            assertEquals(LocalDate.of(1975, 07, 18), mOriginal.readValue("2024", LocalDate.class));
            assertNull(mOriginal.readValue("\"\"", LocalDate.class));
        }

        // demonstrate new strict behaviour
        ObjectMapper m = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(LocalDate.class, new StrictLocalDateDeserializer());
        m.registerModule(module);

        assertEquals(LocalDate.of(2024, 11, 30), m.readValue("\"2024-11-30\"", LocalDate.class));
        assertThrows(MismatchedInputException.class, () -> m.readValue("0", LocalDate.class));
        assertThrows(MismatchedInputException.class, () -> m.readValue("2024", LocalDate.class));
        assertThrows(InvalidFormatException.class, () -> m.readValue("\"abc\"", LocalDate.class));
        assertThrows(InvalidFormatException.class, () -> m.readValue("\"\"", LocalDate.class));
    }

    @Test
    public void testOffsetDateTimeStrictDeserializer() throws JsonMappingException, JsonProcessingException {

        // demonstrate original non-strict behaviour
        {
            ObjectMapper mOriginal = new ObjectMapper();
            mOriginal.registerModule(new JavaTimeModule());
            assertEquals(OffsetDateTime.of(2024, 11, 30, 22, 54, 37, 0, ZoneOffset.UTC),
                    mOriginal.readValue("\"2024-11-30T22:54:37Z\"", OffsetDateTime.class));
            assertEquals(OffsetDateTime.of(1970, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC),
                    mOriginal.readValue("0", OffsetDateTime.class));
            assertEquals(OffsetDateTime.of(1970, 1, 24, 10, 13, 20, 0, ZoneOffset.UTC),
                    mOriginal.readValue("2024000", OffsetDateTime.class));
            assertNull(mOriginal.readValue("\"\"", OffsetDateTime.class));
        }

        // demonstrate new strict behaviour
        ObjectMapper m = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(OffsetDateTime.class, new StrictOffsetDateTimeDeserializer());
        m.registerModule(module);

        assertEquals(OffsetDateTime.of(2024, 11, 30, 22, 54, 37, 0, ZoneOffset.UTC),
                m.readValue("\"2024-11-30T22:54:37Z\"", OffsetDateTime.class));
        // must have time zone
        assertThrows(InvalidFormatException.class, () -> m.readValue("\"2024-11-30T22:54:37\"", OffsetDateTime.class));
        assertThrows(MismatchedInputException.class, () -> m.readValue("0", OffsetDateTime.class));
        assertThrows(MismatchedInputException.class, () -> m.readValue("2024", OffsetDateTime.class));
        assertThrows(InvalidFormatException.class, () -> m.readValue("\"abc\"", OffsetDateTime.class));
        assertThrows(InvalidFormatException.class, () -> m.readValue("\"\"", OffsetDateTime.class));
    }
    
    @Test
    public void testModuleIsUsed() {
        assertThrows(MismatchedInputException.class, () -> Config.builder().build().mapper().readValue("2", Boolean.class));
    }
    
}
