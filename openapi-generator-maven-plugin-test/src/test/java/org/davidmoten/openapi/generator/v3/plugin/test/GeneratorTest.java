package org.davidmoten.openapi.generator.v3.plugin.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import generated.model.SimpleInt;
import generated.model.SimpleInteger;
import generated.model.SimpleLong;
import generated.model.SimpleString;

public class GeneratorTest {

    private static final ObjectMapper m = new ObjectMapper();

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
    public void testSimpleString() throws JsonMappingException, JsonProcessingException {
        String json = "\"abc\"";
        SimpleString a = m.readValue(json, SimpleString.class);
        assertTrue(a.value() instanceof String);
        assertEquals("abc", a.value());
        assertEquals(json, m.writeValueAsString(a));
    }

    public static Class<Integer> typeof(final int x) {
        return Integer.TYPE;
    }

    public static Class<Long> typeof(final long x) {
        return Long.TYPE;
    }

}
