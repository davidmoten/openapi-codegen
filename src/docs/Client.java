package org.davidmoten.oa3.codegen.test.library.client;

import jakarta.annotation.Generated;

import java.lang.String;
import java.lang.Void;
import java.util.List;
import java.util.Optional;

import org.davidmoten.oa3.codegen.client.runtime.ClientBuilder;
import org.davidmoten.oa3.codegen.http.Http;
import org.davidmoten.oa3.codegen.http.Http.Builder;
import org.davidmoten.oa3.codegen.http.Http.RequestBuilder;
import org.davidmoten.oa3.codegen.http.HttpMethod;
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
@Generated(value = "com.github.davidmoten:openapi-codegen-runtime:0.1.18-SNAPSHOT")
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

    public static ClientBuilder<Client> basePath(Server server) {
        return new ClientBuilder<>(b -> new Client(b.serializer(), b.interceptors(), b.basePath(), b.httpService()), Globals.config(), server.url());
    }

    public enum Server {

        SERVER1("/");

        private String url;

        Server(String url) {
            this.url = url;
        }

        public String url() {
            return url;
        }
    }

    private Builder http(HttpMethod method, String path) {
        return Http
                .method(method)
                .basePath(this.basePath)
                .path(path)
                .serializer(this.serializer)
                .interceptors(this.interceptors)
                .httpService(this.httpService);
    }

    /**
     * <p>List users page by page, filtered by search if present
     * 
     * <p>[status=200, application/json] --&gt; {@code UsersPage}
     * 
     * @param search
     *            <p>search
     * @param continuationToken
     *            <p>continuationToken
     * @return call builder
     */
    public RequestBuilder<UsersPage> getUsers(
            Optional<String> search, 
            Optional<String> continuationToken) {
        return http(HttpMethod.GET, "/user")
                .acceptApplicationJson()
                .queryParam("search", search)
                .queryParam("continuationToken", continuationToken)
                .responseAs(UsersPage.class)
                .whenStatusCodeMatches("200")
                .whenContentTypeMatches("application/json")
                .<UsersPage>requestBuilder("200", "application/json");
    }

    /**
     * <p>Creates a new user
     * 
     * @param requestBody
     *            <p>requestBody
     * @return call builder
     */
    public RequestBuilder<Void> createUser(
            User requestBody) {
        return http(HttpMethod.POST, "/user")
                .body(requestBody)
                .contentTypeApplicationJson()
                .<Void>requestBuilder();
    }

    /**
     * <p>Gets user details
     * 
     * <p>[status=200, application/json] --&gt; {@code User}
     * 
     * @param id
     *            <p>id
     * @return call builder
     */
    public RequestBuilder<User> getUser(
            String id) {
        return http(HttpMethod.GET, "/user/{id}")
                .acceptApplicationJson()
                .pathParam("id", id)
                .responseAs(User.class)
                .whenStatusCodeMatches("200")
                .whenContentTypeMatches("application/json")
                .<User>requestBuilder("200", "application/json");
    }

    /**
     * <p>Updates a user
     * 
     * @param requestBody
     *            <p>requestBody
     * @param id
     *            <p>id
     * @return call builder
     */
    public RequestBuilder<Void> updateUser(
            String id, 
            User requestBody) {
        return http(HttpMethod.PUT, "/user/{id}")
                .pathParam("id", id)
                .body(requestBody)
                .contentTypeApplicationJson()
                .<Void>requestBuilder();
    }

    /**
     * <p>Deletes a user (logically)
     * 
     * @param id
     *            <p>id
     * @return call builder
     */
    public RequestBuilder<Void> deleteUser(
            String id) {
        return http(HttpMethod.DELETE, "/user/{id}")
                .pathParam("id", id)
                .<Void>requestBuilder();
    }

    /**
     * <p>Gets item details
     * 
     * <p>[status=200, application/json] --&gt; {@code Item}
     * 
     * @param itemId
     *            <p>itemId
     * @return call builder
     */
    public RequestBuilder<Item> getItem(
            String itemId) {
        return http(HttpMethod.GET, "/item/{itemId}")
                .acceptApplicationJson()
                .pathParam("itemId", itemId)
                .responseAs(Item.class)
                .whenStatusCodeMatches("200")
                .whenContentTypeMatches("application/json")
                .<Item>requestBuilder("200", "application/json");
    }

    public Builder _custom(HttpMethod method, String path) {
        return Http
                .method(method)
                .basePath(this.basePath)
                .path(path)
                .serializer(this.serializer)
                .httpService(this.httpService);
    }
}
