package org.davidmoten.oa3.codegen.test.paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import org.davidmoten.oa3.codegen.http.HttpResponse;
import org.davidmoten.oa3.codegen.http.NotPrimaryResponseException;
import org.davidmoten.oa3.codegen.http.service.HttpService;
import org.davidmoten.oa3.codegen.spring.runtime.DefaultError;
import org.davidmoten.oa3.codegen.spring.runtime.ServiceException;
import org.davidmoten.oa3.codegen.test.Helper;
import org.davidmoten.oa3.codegen.test.paths.client.Client;
import org.davidmoten.oa3.codegen.test.paths.path.SubmitPostRequestApplicationXWwwFormUrlencoded;
import org.davidmoten.oa3.codegen.test.paths.path.UploadPostRequestMultipartFormData;
import org.davidmoten.oa3.codegen.test.paths.path.UploadPostRequestMultipartFormData.Document;
import org.davidmoten.oa3.codegen.test.paths.path.UploadPostRequestMultipartFormData.Document.ContentType;
import org.davidmoten.oa3.codegen.test.paths.response.Response4;
import org.davidmoten.oa3.codegen.test.paths.schema.Point;
import org.davidmoten.oa3.codegen.test.paths.schema.Response1;
import org.davidmoten.oa3.codegen.test.paths.schema.Response2;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@SpringBootTest(classes = { PathsApplication.class }, webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
public class PathsServerTest {

    private static final ObjectMapper m = Globals.config().mapper();

    @LocalServerPort
    int serverPort;

    private String basePath() {
        return "http://localhost:" + serverPort;
    }

    private Client client(HttpService httpService) {
        return Client.basePath(basePath()).httpService(httpService).build();
    }

    @Test
    public void testGet() {
        Response2 r = Http.read(basePath() + "/item", HttpMethod.GET, Response2.class, m);
        assertEquals("abcToken", r.token());
    }

    @Test
    public void testDefaultError() {
        DefaultError error = Http.readError(basePath() + "/item201", HttpMethod.GET, DefaultError.class, m);
        assertEquals("todo sale mal", error.message());
        assertEquals(500, error.statusCode());
    }

    @Test
    public void testResponseMultiTypeWithAcceptHeader() {
        HttpHeaders headers = new HttpHeaders();
        headers.put(HttpHeaders.ACCEPT, Arrays.asList("application/json"));
        Response1 response = Http.read(basePath() + "/responseMultiType?username=fred", HttpMethod.GET, Response1.class,
                headers, m);
        assertEquals("fred", response.thing());
    }

    @Test
    public void testResponseMultiTypeWithAcceptHeaderOctetStream() {
        String response = Http.readStringFromOctetStream(basePath() + "/responseMultiType?username=fred",
                HttpMethod.GET);
        assertEquals("hello there", response);
    }

    @Test
    public void testCustomResponse() {
        // primary return is Response3 but we will get to return Response1 with a 500
        // status code
        Response1 r = Http.readError(basePath() + "/responseRef", HttpMethod.GET, Response1.class, m);
        assertEquals("beehive", r.thing());
    }

    @ParameterizedTest
    @MethodSource("httpServices")
    public void testClientCustomCall(HttpService httpService) throws ServiceException {
        HttpResponse r = client(httpService) //
                ._custom(org.davidmoten.oa3.codegen.http.HttpMethod.GET, "/item") //
                .acceptApplicationJson().responseAs(Response2.class).whenStatusCodeMatches("200")
                .whenContentTypeMatches("application/json").responseAs(Error.class).whenStatusCodeMatches("default")
                .whenContentTypeMatches("application/json").call();
        assertEquals(200, r.statusCode());
        assertTrue(r.data().isPresent());
        Response2 a = (Response2) r.data().get();
        assertEquals("abcToken", a.token());
    }

    @Test
    public void testQueryObjectModelAttribute() {
        assertEquals(200, Http.readStatusCodeOnly(basePath() + "/query-object?first=abc&second=12", HttpMethod.GET));
    }

    @Test
    public void testQueryObjectBadParamType() {
        assertEquals(400, Http.readStatusCodeOnly(basePath() + "/query-object?first=abc&second=bad", HttpMethod.GET));
    }

    @Test
    public void testQueryObjectParamMissing() {
        // we can't tell with object parameters if the cause was bad request or bad
        // server-side logic so we defensively return status code 500
        assertEquals(500, Http.readStatusCodeOnly(basePath() + "/query-object?second=12", HttpMethod.GET));
    }

    // deepObject style not supported by spring-boot (a[lat]=1&a[lon]=2)
    // @Test()
    public void testPoints() {
        // we can't tell with object parameters if the cause was bad request or bad
        // server-side logic so we defensively return status code 500
        assertEquals(200, Http.readStatusCodeOnly(
                basePath() + "/points?a%5Blat%5D=1&a%5Blon%5D=2&b%5Blat%5D=3&b%5Blon%5D=4", HttpMethod.GET));
    }

    @Test
    public void testOctetStream() {
        assertEquals("hello there", Http.readStringFromOctetStream(basePath() + "/bytes", HttpMethod.GET));
    }

    @ParameterizedTest
    @MethodSource("httpServices")
    public void testOctetStreamWithClient(HttpService httpService) {
        assertEquals("hello there", new String(client(httpService).bytesGet(), StandardCharsets.UTF_8));
    }

    @Test
    public void testText() {
        assertEquals("example text", Http.readString(basePath() + "/text", HttpMethod.GET, "text/plain"));
    }

    @ParameterizedTest
    @MethodSource("httpServices")
    public void testClientNoArgs(HttpService httpService) throws ServiceException {
        HttpResponse r = client(httpService).itemGetFullResponse();
        assertEquals(200, r.statusCode());
        assertTrue(r.data().isPresent());
        Response2 a = (Response2) r.data().get();
        assertEquals("abcToken", a.token());
    }

    @ParameterizedTest
    @MethodSource("httpServices")
    public void testClientFullResponseMultiTypeWithAcceptHeaderJson(HttpService httpService) {
        HttpResponse r = client(httpService).responseMultiTypeGetFullResponse("application/json", "jason");
        assertEquals(200, r.statusCode());
    }

    @ParameterizedTest
    @MethodSource("httpServices")
    public void testClientPrimaryResponseMultiTypeWithAcceptHeaderJson(HttpService httpService) {
        try {
            client(httpService).responseMultiTypeGet("application/json", "jason");
        } catch (NotPrimaryResponseException e) {
            assertEquals("hello there", new String((byte[]) e.response().data().get(), StandardCharsets.UTF_8));
        }
    }

    @ParameterizedTest
    @MethodSource("httpServices")
    public void testClientResponseMultiTypeWithAcceptHeaderOctetStream(HttpService httpService) {
        HttpResponse r = client(httpService).responseMultiTypeGetFullResponse("application/octet-stream", "jason");
        assertEquals(200, r.statusCode());
        assertEquals("hello there", new String((byte[]) r.data().get(), StandardCharsets.UTF_8));
        assertTrue(r.headers().contains("content-type", "application/octet-stream"));
    }

    @ParameterizedTest
    @MethodSource("httpServices")
    public void testDefaultErrorReturned(HttpService httpService) {
        HttpResponse r = client(httpService).defaultErrorGetFullResponse();
        org.davidmoten.oa3.codegen.test.paths.schema.Error e = r.dataUnwrapped();
        assertEquals("not found eh", e.errorMessage().orElse(""));
    }

    @ParameterizedTest
    @MethodSource("httpServices")
    public void testSimpleStringJsonResponse(HttpService httpService) {
        assertEquals("hello", client(httpService).jsonStringGet().value());
    }

    @ParameterizedTest
    @MethodSource("httpServices")
    public void testWildcardStatusCode(HttpService httpService) {
        assertEquals("hi there", client(httpService).wildcardStatusCodeGetFullResponse().<Response4>dataUnwrapped().value());
    }    
    
    @ParameterizedTest
    @MethodSource("httpServices")
    public void testMultipartFormData(HttpService httpService) {
        Object o = client(httpService) //
                .uploadPost(UploadPostRequestMultipartFormData //
                        .point(Point.lat(-23).lon(135).build()) //
                        .description("theDescription") //
                        .document(Document //
                                .contentType(ContentType.APPLICATION_PDF) //
                                .value(new byte[] { 1, 2, 3 }) //
                                .build()) //
                        .build());
        assertTrue(o instanceof ObjectNode);
        assertTrue(((ObjectNode) o).isEmpty());
    }

    @ParameterizedTest
    @MethodSource("httpServices")
    public void testUrlEncodedFormData(HttpService httpService) {
        SubmitPostRequestApplicationXWwwFormUrlencoded request = //
                SubmitPostRequestApplicationXWwwFormUrlencoded.name("Fred").count(23).build();
        Object o = client(httpService).submitPost(request);
        assertTrue(o instanceof ObjectNode);
        assertTrue(((ObjectNode) o).isEmpty());
    }

    static List<HttpService> httpServices() {
        return Helper.httpServices();
    }

}
