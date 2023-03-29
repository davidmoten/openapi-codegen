package org.davidmoten.openapi.generator.v3.plugin.test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.Base64;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import generated.model.SimpleBoolean;
import generated.model.SimpleByteArray;
import generated.model.SimpleDateTime;
import generated.model.SimpleDouble;
import generated.model.SimpleFloat;
import generated.model.SimpleInt;
import generated.model.SimpleInteger;
import generated.model.SimpleLong;
import generated.model.SimpleString;

public class PluginGeneratorTest {

    private static final ObjectMapper m = new ObjectMapper().registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @Test
    public void testSimpleLong() throws JsonMappingException, JsonProcessingException {
        String json = Long.MAX_VALUE + "";
        SimpleLong a = m.readValue(json, SimpleLong.class);
        assertEquals(Long.TYPE, typeof(a.value()));
        assertEquals(json, m.writeValueAsString(a));
    }

    @Test
    public void testSimpleInt() throws JsonMappingException, JsonProcessingException {
        String json = "123";
        SimpleInt a = m.readValue(json, SimpleInt.class);
        assertEquals(Integer.TYPE, typeof(a.value()));
        assertEquals(123, a.value());
        assertEquals(json, m.writeValueAsString(a));
    }

    @Test
    public void testSimpleIntegerDefaultsToLong() throws JsonMappingException, JsonProcessingException {
        String json = "123";
        SimpleInteger a = m.readValue(json, SimpleInteger.class);
        assertEquals(Long.TYPE, typeof(a.value()));
        assertEquals(123, a.value());
        assertEquals(json, m.writeValueAsString(a));
    }

    @Test
    public void testSimpleFloat() throws JsonMappingException, JsonProcessingException {
        String json = "123.4";
        SimpleFloat a = m.readValue(json, SimpleFloat.class);
        assertEquals(Float.TYPE, typeof(a.value()));
        assertEquals(123.4, a.value(), 0.00001);
        assertEquals(json, m.writeValueAsString(a));
    }

    @Test
    public void testSimpleDouble() throws JsonMappingException, JsonProcessingException {
        String json = "123.4";
        SimpleDouble a = m.readValue(json, SimpleDouble.class);
        assertEquals(Double.TYPE, typeof(a.value()));
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
    }

    @Test
    public void testSimpleByteArrayUsingBase64Encoding() throws JsonMappingException, JsonProcessingException {
        byte[] bytes = "abc".getBytes(StandardCharsets.UTF_8);
        String json = "\"" + Base64.getEncoder().encodeToString(bytes) + "\"";
        SimpleByteArray a = m.readValue(json, SimpleByteArray.class);
        assertTrue(a.value() instanceof byte[]);
        assertArrayEquals(bytes, a.value());
        assertEquals(json, m.writeValueAsString(a));
    }

    @Test
    public void testSimpleDateTime() throws JsonMappingException, JsonProcessingException {
        String s = "2018-03-20T09:12:28Z";
        String json = "\"" + s + "\"";
        SimpleDateTime a = m.readValue(json, SimpleDateTime.class);
        assertTrue(a.value() instanceof OffsetDateTime);
        assertEquals(OffsetDateTime.parse(s), a.value());
        assertEquals(json, m.writeValueAsString(a));
    }

    @Test
    public void testSimpleBoolean() throws JsonMappingException, JsonProcessingException {
        String json = "true";
        SimpleBoolean a = m.readValue(json, SimpleBoolean.class);
        assertEquals(Boolean.TYPE, typeof(a.value()));
        assertTrue(a.value());
        assertEquals(json, m.writeValueAsString(a));
    }

    private static Class<Integer> typeof(int x) {
        return Integer.TYPE;
    }

    private static Class<Long> typeof(long x) {
        return Long.TYPE;
    }

    private static Class<Float> typeof(float x) {
        return Float.TYPE;
    }

    private static Class<Double> typeof(double x) {
        return Double.TYPE;
    }

    private static Class<Byte> typeof(byte x) {
        return Byte.TYPE;
    }

    private static Class<Boolean> typeof(boolean x) {
        return Boolean.TYPE;
    }

}
