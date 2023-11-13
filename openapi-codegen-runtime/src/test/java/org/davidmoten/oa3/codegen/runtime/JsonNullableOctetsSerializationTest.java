package org.davidmoten.oa3.codegen.runtime;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;
import org.openapitools.jackson.nullable.JsonNullable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

public class JsonNullableOctetsSerializationTest {

    private static final byte[] HI_THERE = "hi there".getBytes(StandardCharsets.UTF_8);

    public static final class Thing {

        @JsonProperty("bytes")
        @JsonSerialize(using = JsonNullableOctetsSerializer.class)
        final JsonNullable<byte[]> bytes;

        @JsonCreator
        public Thing(
                @JsonDeserialize(using = JsonNullableOctetsDeserializer.class) //
                @JsonProperty("bytes") // 
                JsonNullable<byte[]> bytes) {
            this.bytes = bytes;
        }
    }

    @Test
    public void testJsonNullablePresent() throws JsonProcessingException {
        ObjectMapper m = Config.builder().build().mapper();
        Thing a = new Thing(JsonNullable.of(HI_THERE));
        String json = m.writeValueAsString(a);
        assertEquals(json, "{\"bytes\":\"6869207468657265\"}");
        assertArrayEquals(HI_THERE, m.readValue(json, Thing.class).bytes.get());
    }
    
    @Test
    public void testJsonNullableNull() throws JsonProcessingException {
        ObjectMapper m = Config.builder().build().mapper();
        Thing a = new Thing(JsonNullable.of(null));
        String json = m.writeValueAsString(a);
        assertTrue(m.readValue(json, Thing.class).bytes.isPresent());
    }

    @Test
    public void testJsonNullableAbsent() throws JsonProcessingException {
        ObjectMapper m = Config.builder().build().mapper();
        Thing a = new Thing(JsonNullable.undefined());
        String json = m.writeValueAsString(a);
        assertFalse(m.readValue(json, Thing.class).bytes.isPresent());
    }

}
