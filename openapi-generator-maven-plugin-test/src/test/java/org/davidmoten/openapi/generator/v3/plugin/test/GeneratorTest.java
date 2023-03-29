package org.davidmoten.openapi.generator.v3.plugin.test;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import generated.model.SimpleLong;

public class GeneratorTest {
    
    private static final ObjectMapper m = new ObjectMapper();
    
    @Test
    public void testSimpleLong() throws JsonMappingException, JsonProcessingException {
        String json = "123";
        SimpleLong a = m.readValue(json, SimpleLong.class);
        assertEquals(123L, a.value());
        assertEquals(json, m.writeValueAsString(a));
    }

}
