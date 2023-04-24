package org.davidmoten.oa3.codegen.paths;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.davidmoten.oa3.codegen.paths.schema.Response1;
import org.davidmoten.oa3.codegen.paths.schema.Response2;
import org.davidmoten.oa3.codegen.spring.runtime.DefaultError;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpMethod;
import org.springframework.web.servlet.DispatcherServlet;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * See {@link PathsService} class for implementations being called by these
 * tests.
 *
 */
public class PathsServerTest {

    private static ConfigurableApplicationContext context;
    private static ObjectMapper m;

    @BeforeClass
    public static void start() {
        context = SpringApplication.run(Application.class, new String[] {});
        m = context.getBean(ObjectMapper.class);
    }

    @AfterClass
    public static void stop() {
        context.stop();
    }

    @Test
    public void testObjectMapperIsCorrect() {
        assertTrue(m == Globals.config().mapper());
    }

    @Test
    public void testGet() {
        Response2 r = Http.read("http://localhost:8080/item", HttpMethod.GET, Response2.class, m);
        assertEquals("abcToken", r.token());
    }

    @Test
    public void testDefaultError() {
        DefaultError error = Http.readError("http://localhost:8080/item201", HttpMethod.GET, DefaultError.class, m);
        assertEquals("todo sale mal", error.message());
        assertEquals(500, error.statusCode());
    }

    @Test
    public void testCustomResponse() {
        // primary return is Response3 but we will get to return Response1 with a 500
        // status code
        Response1 r = Http.readError("http://localhost:8080/responseRef", HttpMethod.GET, Response1.class, m);
        assertEquals("beehive", r.thing());
    }

    @Test
    public void testQueryObjectModelAttribute() {
        assertEquals(200,
                Http.readStatusCodeOnly("http://localhost:8080/query-object?first=abc&second=12", HttpMethod.GET));
    }

    @Test
    public void testQueryObjectBadParamType() {
        assertEquals(400,
                Http.readStatusCodeOnly("http://localhost:8080/query-object?first=abc&second=bad", HttpMethod.GET));
    }

    @Test
    public void testQueryObjectParamMissing() {
        // we can't tell with object parameters if the cause was bad request or bad
        // server-side logic so we defensively return status code 500
        assertEquals(500, Http.readStatusCodeOnly("http://localhost:8080/query-object?second=12", HttpMethod.GET));
    }
    
    @Ignore("deepObject style not supported by spring-boot (a[lat]=1&a[lon]=2)")
    @Test
    public void testPoints() {
        // we can't tell with object parameters if the cause was bad request or bad
        // server-side logic so we defensively return status code 500
        assertEquals(200, Http.readStatusCodeOnly("http://localhost:8080/points?a%5Blat%5D=1&a%5Blon%5D=2&b%5Blat%5D=3&b%5Blon%5D=4", HttpMethod.GET));
    }
    
    public static void main(String[] args) {
        start();
        DispatcherServlet s;
    }
}
