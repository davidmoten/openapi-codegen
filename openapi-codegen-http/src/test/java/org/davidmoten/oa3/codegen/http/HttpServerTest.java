package org.davidmoten.oa3.codegen.http;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;

@SpringBootTest(classes = { Application.class }, webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
public class HttpServerTest {

    @LocalServerPort
    int serverPort;

    @Test
    void testGet() {
        HttpResponse r = Http //
                .method("GET") //
                .basePath("http://localhost:" + serverPort) //
                .pathTemplate("/thing") //
                .header("Accept", "application/json") //
                .queryParam("id", "abc1") //
                .responseAs(Thing.class) //
                .whenStatusCodeMatches("200") //
                .whenContentTypeMatches("application/json") //
                .call();
        assertEquals(200, r.statusCode());
        assertTrue(r.data().isPresent());
        Thing a = (Thing) r.data().get();
        assertEquals("janice", a.name);
        assertEquals(34, a.age);
    }

}