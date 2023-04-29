package org.davidmoten.oa3.codegen.http;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;

import org.junit.jupiter.api.Test;

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
    public void test() throws MalformedURLException {
        try (Server server = Server.start().response() // default is status 200, no body, no headers
                .response().header("Accept", "application/json").body("{}").statusCode(201).response()
                .body("an error occurred").statusCode(500).add()) {
            URL url = new URL(server.baseUrl() + "thing?state=joy");
            // hit the url a couple of times and do your asserts
        }
    }

}
