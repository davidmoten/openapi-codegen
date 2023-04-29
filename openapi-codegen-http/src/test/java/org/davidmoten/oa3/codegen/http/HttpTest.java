package org.davidmoten.oa3.codegen.http;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.MalformedURLException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.davidmoten.http.test.server.Server;

public class HttpTest {

    @Test
    public void testBuildUrlWithQueryParameters() {
        String url = Http.buildUrl("/map", "/msi/hello", Arrays.asList(ParameterValue.query("thing", 1),
                ParameterValue.query("stuff", OffsetDateTime.of(2023, 5, 24, 14, 55, 33, 0, ZoneOffset.UTC))));
        assertEquals("/map/msi/hello?thing=1&stuff=2023-05-24T14%3A55%3A33Z", url);
    }

    @Test
    public void testBuildUrlWithPathParameters() {
        String url = Http.buildUrl("/map", "/msi/{id}/hello", Arrays.asList(ParameterValue.path("id", 1)));
        assertEquals("/map/msi/1/hello?", url);
    }

    @Test
    public void testBuildUrlWithPathParametersEncoded() {
        String url = Http.buildUrl("/map", "/msi/{id}/hello", Arrays.asList(ParameterValue.path("id", "abc=")));
        assertEquals("/map/msi/abc%3D/hello?", url);
    }

    @Test
    public void testSerialization() throws JsonProcessingException {
        ObjectMapper m = new ObjectMapper();
        Thing a = new Thing("fred", 23);
        String json = m.writeValueAsString(a);
        assertEquals("{\"name\":\"fred\",\"age\":23}", json);
        Thing b = m.readValue(json, Thing.class);
    }

    @Test
    public void testReadThingFromServer() throws MalformedURLException {
        try (Server server = Server //
                .start() //
                .response().header("Content-Type", "application/json").body("{\"name\":\"fred\",\"age\":23}")
                .statusCode(200).add()) {
            // hit the url a couple of times and do your asserts
            HttpResponse r = Http.call("GET", server.baseUrl() + "map", "/msi", new ObjectMapper(), Headers.create(),
                    Collections.emptyList(), //
                    Arrays.asList(new ResponseDescriptor("2XX", "application/*", Thing.class)));
            assertEquals(200, r.statusCode());
            assertTrue(r.data().isPresent());
            Thing a = (Thing) r.data().get();
            assertEquals("fred", a.name);
            assertEquals(23, a.age);
        }
    }

    public static final class Thing {
        @JsonProperty("name")
        public String name;

        @JsonProperty("age")
        public int age;

        @JsonCreator
        public Thing(@JsonProperty("name") String name, @JsonProperty("age") int age) {
            this.name = name;
            this.age = age;
        }
    }

}
