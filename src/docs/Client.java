package org.davidmoten.oa3.codegen.test.library.client;

import jakarta.annotation.Generated;

import java.lang.String;
import java.util.List;
import java.util.Optional;

import org.davidmoten.oa3.codegen.client.runtime.ClientBuilder;
import org.davidmoten.oa3.codegen.http.Http;
import org.davidmoten.oa3.codegen.http.HttpMethod;
import org.davidmoten.oa3.codegen.http.HttpResponse;
import org.davidmoten.oa3.codegen.http.Interceptor;
import org.davidmoten.oa3.codegen.http.Serializer;
import org.davidmoten.oa3.codegen.http.service.HttpService;
import org.davidmoten.oa3.codegen.test.library.Globals;
import org.davidmoten.oa3.codegen.test.library.schema.Item;
import org.davidmoten.oa3.codegen.test.library.schema.User;
import org.davidmoten.oa3.codegen.test.library.schema.UsersPage;

/**
 * <p>Library Demo
 * <p>Library demonstration of some features of OpenAPI 3 and <em>openapi-codegen</em>
 */
@Generated(value = "com.github.davidmoten:openapi-codegen-runtime:0.1.4")
public class Client {

    private final Serializer serializer;
    private final List<Interceptor> interceptors;
    private final String basePath;
    private final HttpService httpService;

    private Client(Serializer serializer, List<Interceptor> interceptors, String basePath, HttpService httpService) {
        this.serializer = serializer;
        this.interceptors = interceptors;
        this.basePath = basePath;
        this.httpService = httpService;
    }

    public static ClientBuilder<Client> basePath(String basePath) {
        return new ClientBuilder<>(b -> new Client(b.serializer(), b.interceptors(), b.basePath(), b.httpService()), Globals.config(), basePath);
    }

    /**
     * <p>List users page by page, filtered by search if present
     * 
     * <p>[status=200, application/json] --&gt; {@link UsersPage}
     * 
     * @param search
     *            search
     * @param continuationToken
     *            continuationToken
     * @return primary response with status code 200
     */
    public UsersPage getUsers(
            Optional<String> search, 
            Optional<String> continuationToken) {
        return getUsersFullResponse(search, continuationToken)
                .assertStatusCodeMatches("200")
                .assertContentTypeMatches("application/json")
                .dataUnwrapped();
    }

    /**
     * <p>List users page by page, filtered by search if present
     * 
     * <p>[status=200, application/json] --&gt; {@link UsersPage}
     * 
     * @param search
     *            search
     * @param continuationToken
     *            continuationToken
     * @return full response with status code, body and headers
     */
    public HttpResponse getUsersFullResponse(
            Optional<String> search, 
            Optional<String> continuationToken) {
        return Http
                .method(HttpMethod.GET)
                .basePath(this.basePath)
                .path("/user")
                .serializer(this.serializer)
                .interceptors(this.interceptors)
                .httpService(this.httpService)
                .acceptApplicationJson()
                .queryParam("search", search)
                .queryParam("continuationToken", continuationToken)
                .responseAs(UsersPage.class)
                .whenStatusCodeMatches("200")
                .whenContentTypeMatches("application/json")
                .call();
    }

    /**
     * <p>Creates a new user
     * 
     * @param requestBody
     *            requestBody
     * @return full response with status code, body and headers
     */
    public HttpResponse createUserFullResponse(
            User requestBody) {
        return Http
                .method(HttpMethod.POST)
                .basePath(this.basePath)
                .path("/user")
                .serializer(this.serializer)
                .interceptors(this.interceptors)
                .httpService(this.httpService)
                .acceptApplicationJson()
                .body(requestBody)
                .contentTypeApplicationJson()
                .call();
    }

    /**
     * <p>Gets user details
     * 
     * <p>[status=200, application/json] --&gt; {@link User}
     * 
     * @param id
     *            id
     * @return primary response with status code 200
     */
    public User getUser(
            String id) {
        return getUserFullResponse(id)
                .assertStatusCodeMatches("200")
                .assertContentTypeMatches("application/json")
                .dataUnwrapped();
    }

    /**
     * <p>Gets user details
     * 
     * <p>[status=200, application/json] --&gt; {@link User}
     * 
     * @param id
     *            id
     * @return full response with status code, body and headers
     */
    public HttpResponse getUserFullResponse(
            String id) {
        return Http
                .method(HttpMethod.GET)
                .basePath(this.basePath)
                .path("/user/{id}")
                .serializer(this.serializer)
                .interceptors(this.interceptors)
                .httpService(this.httpService)
                .acceptApplicationJson()
                .pathParam("id", id)
                .responseAs(User.class)
                .whenStatusCodeMatches("200")
                .whenContentTypeMatches("application/json")
                .call();
    }

    /**
     * <p>Updates a user
     * 
     * @param requestBody
     *            requestBody
     * @param id
     *            id
     * @return full response with status code, body and headers
     */
    public HttpResponse updateUserFullResponse(
            String id, 
            User requestBody) {
        return Http
                .method(HttpMethod.PUT)
                .basePath(this.basePath)
                .path("/user/{id}")
                .serializer(this.serializer)
                .interceptors(this.interceptors)
                .httpService(this.httpService)
                .acceptApplicationJson()
                .pathParam("id", id)
                .body(requestBody)
                .contentTypeApplicationJson()
                .call();
    }

    /**
     * <p>Deletes a user (logically)
     * 
     * @param id
     *            id
     * @return full response with status code, body and headers
     */
    public HttpResponse deleteUserFullResponse(
            String id) {
        return Http
                .method(HttpMethod.DELETE)
                .basePath(this.basePath)
                .path("/user/{id}")
                .serializer(this.serializer)
                .interceptors(this.interceptors)
                .httpService(this.httpService)
                .acceptApplicationJson()
                .pathParam("id", id)
                .call();
    }

    /**
     * <p>Gets item details
     * 
     * <p>[status=200, application/json] --&gt; {@link Item}
     * 
     * @param itemId
     *            itemId
     * @return primary response with status code 200
     */
    public Item getItem(
            String itemId) {
        return getItemFullResponse(itemId)
                .assertStatusCodeMatches("200")
                .assertContentTypeMatches("application/json")
                .dataUnwrapped();
    }

    /**
     * <p>Gets item details
     * 
     * <p>[status=200, application/json] --&gt; {@link Item}
     * 
     * @param itemId
     *            itemId
     * @return full response with status code, body and headers
     */
    public HttpResponse getItemFullResponse(
            String itemId) {
        return Http
                .method(HttpMethod.GET)
                .basePath(this.basePath)
                .path("/item/{itemId}")
                .serializer(this.serializer)
                .interceptors(this.interceptors)
                .httpService(this.httpService)
                .acceptApplicationJson()
                .pathParam("itemId", itemId)
                .responseAs(Item.class)
                .whenStatusCodeMatches("200")
                .whenContentTypeMatches("application/json")
                .call();
    }
}
