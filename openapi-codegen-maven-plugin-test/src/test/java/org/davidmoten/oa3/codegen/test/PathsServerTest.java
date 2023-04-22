package org.davidmoten.oa3.codegen.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.davidmoten.oa3.codegen.paths.Application;
import org.davidmoten.oa3.codegen.paths.Globals;
import org.davidmoten.oa3.codegen.paths.schema.Response2;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpMethod;

import com.fasterxml.jackson.databind.ObjectMapper;

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
    public void testGet() {
        Response2 r = Http.read("http://localhost:8080/item", HttpMethod.GET, Response2.class, m);
        assertEquals("abcToken", r.token());
        assertTrue(m == Globals.config().mapper());
    }

}
