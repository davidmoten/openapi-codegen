package org.davidmoten.oa3.codegen.http;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest(classes = { Application.class }, webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
public class HttpServerTest {

    private static final Serializer serializer = new DefaultSerializer(new ObjectMapper());

    @LocalServerPort
    int serverPort;

    @Test
    void testGetThing() {
        HttpResponse r = getResponseGet("a");
        assertEquals(200, r.statusCode());
        assertTrue(r.data().isPresent());
        Thing a = (Thing) r.data().get();
        assertEquals("janice", a.name);
        assertEquals(34, a.age);
    }

    @Test
    void testGetProblem() {
        HttpResponse r = getResponseGet("b");
        assertEquals(HttpStatus.BAD_REQUEST.value(), r.statusCode());
        assertTrue(r.data().isPresent());
        Problem a = (Problem) r.data().get();
        assertEquals(HttpStatus.BAD_REQUEST.value(), a.statusCode);
    }

    @Test
    void testPostWithRequestBody() {
        HttpResponse r = getResponsePostWithRequestBody();
        assertEquals(200, r.statusCode());
        assertTrue(r.data().isPresent());
        Thing a = (Thing) r.data().get();
        assertEquals("dave", a.name);
    }
    
    @Test
    void testPostWithMultipart() {
        HttpResponse r = getResponsePostWithMultipart();
        assertEquals(200, r.statusCode());
        assertTrue(r.data().isPresent());
        Thing a = (Thing) r.data().get();
        assertEquals("sum", a.name);
        assertEquals(80, a.age);
    }

    private HttpResponse getResponseGet(String id) {
        return Http //
                .method(HttpMethod.GET) //
                .basePath("http://localhost:" + serverPort) //
                .path("/thing") //
                .serializer(serializer) //
                .acceptApplicationJson() //
                .queryParam("id", id) //
                .responseAs(Problem.class) //
                .whenStatusCodeDefault() //
                .whenContentTypeMatches("application/json") //
                .responseAs(Thing.class) //
                .whenStatusCodeMatches("2XX") //
                .whenContentTypeMatches("application/json") //
                .call();
    }

    private HttpResponse getResponsePostWithRequestBody() {
        return Http //
                .method(HttpMethod.POST) //
                .basePath("http://localhost:" + serverPort) //
                .path("/thing") //
                .serializer(serializer) //
                .acceptApplicationJson() //
                .body(new Thing("dave", 20)) //
                .contentTypeApplicationJson()//
                .responseAs(Thing.class) //
                .whenStatusCodeMatches("2XX") //
                .whenContentTypeMatches("application/json") //
                .responseAs(Problem.class) //
                .whenStatusCodeDefault() //
                .whenContentTypeMatches("application/json") //
                .call();
    }
    
    private HttpResponse getResponsePostWithMultipart() {
        return Http //
                .method(HttpMethod.POST) //
                .basePath("http://localhost:" + serverPort) //
                .path("/multipart") //
                .serializer(serializer) //
                .acceptApplicationJson() //
                .multipart("first", new Thing("dave", 20)) //
                .multipart("second", new Thing("fred", 60)) //
                .responseAs(Thing.class) //
                .whenStatusCodeMatches("2XX") //
                .whenContentTypeMatches("application/json") //
                .responseAs(Problem.class) //
                .whenStatusCodeDefault() //
                .whenContentTypeMatches("application/json") //
                .call();
    }

}