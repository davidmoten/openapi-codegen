package org.davidmoten.oa3.codegen.http;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;

@SpringBootTest(classes = { Application.class }, webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
public class HttpServerTest {

    @LocalServerPort
    int serverPort;

    @Test
    void testGetThing() {
        HttpResponse r = getResponse("a");
        assertEquals(200, r.statusCode());
        assertTrue(r.data().isPresent());
        Thing a = (Thing) r.data().get();
        assertEquals("janice", a.name);
        assertEquals(34, a.age);
    }

    @Test
    void testGetProblem() {
        HttpResponse r = getResponse("b");
        assertEquals(HttpStatus.BAD_REQUEST.value(), r.statusCode());
        assertTrue(r.data().isPresent());
        Problem a = (Problem) r.data().get();
        assertEquals(HttpStatus.BAD_REQUEST.value(), a.statusCode);
    }

    private HttpResponse getResponse(String id) {
        return Http //
                .method("GET") //
                .basePath("http://localhost:" + serverPort) //
                .path("/thing") //
                .header("Accept", "application/json") //
                .queryParam("id", id) //
                .responseAs(Thing.class) //
                .whenStatusCodeMatches("200") //
                .whenContentTypeMatches("application/json") //
                .responseAs(Problem.class) //
                .whenStatusCodeMatches("default") //
                .whenContentTypeMatches("application/json") //
                .call();
    }

}