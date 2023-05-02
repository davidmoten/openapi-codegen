package org.davidmoten.oa3.codegen.generator;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JacksonBugWorkaroundTest {

    @Test
    public void testIntToDoubleUglyWorkaround() throws JsonMappingException, JsonProcessingException {
        ObjectMapper m = new ObjectMapper();
        String json = "{\"a\":1.1,\"b\":2}";
        Thing x = m.readValue(json, Thing.class);
        assertEquals(1.1, x.a(), 0.0001);
        assertEquals(2, x.b(), 0.0001);
        assertEquals("{\"a\":1.1,\"b\":2.0}", m.writeValueAsString(x));
    }

    @JsonAutoDetect(fieldVisibility = Visibility.ANY)
    public static final class Thing {

        @JsonProperty("a")
        private final double ax;

        @JsonProperty("b")
        private final double bx;

        @JsonCreator
        private Thing(@JsonProperty("a") Number a, @JsonProperty("b") Number b) {
            if (a == null) {
                throw new IllegalArgumentException("a cannot be null");
            }
            if (b == null) {
                throw new IllegalArgumentException("b cannot be null");
            }
            this.ax = a.doubleValue();
            this.bx = b.doubleValue();
        }

        public Thing(double a, double b) {
            this((Number) a, (Number) b);
        }

        public double a() {
            return ax;
        }

        public double b() {
            return bx;
        }

    }

}
