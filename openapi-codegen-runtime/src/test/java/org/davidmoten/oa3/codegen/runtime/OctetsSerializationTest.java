package org.davidmoten.oa3.codegen.runtime;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

public class OctetsSerializationTest {
    
    private static final byte[] HI_THERE = "hi there".getBytes(StandardCharsets.UTF_8);

    public static final class Thing {

        @JsonProperty("bytes")
        @JsonSerialize(using = OctetsSerializer.class)
        final byte[] bytes;

        @JsonCreator
        public Thing(
                @JsonDeserialize(using = OctetsDeserializer.class) @JsonProperty("bytes") byte[] bytes) {
            this.bytes = bytes;
        }
    }

    @Test
    public void testRoundTrip() throws JsonProcessingException {
        ObjectMapper m = Config.builder().build().mapper();
        Thing a = new Thing(HI_THERE);
        String json = m.writeValueAsString(a);
        assertEquals(json, "{\"bytes\":\"6869207468657265\"}");
        assertArrayEquals(HI_THERE, m.readValue(json, Thing.class).bytes);
    }

}
