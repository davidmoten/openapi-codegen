package org.davidmoten.oa3.codegen.client.runtime;

import java.util.function.Function;

import org.davidmoten.oa3.codegen.http.DefaultSerializer;
import org.davidmoten.oa3.codegen.http.Interceptor;
import org.davidmoten.oa3.codegen.http.Serializer;
import org.davidmoten.oa3.codegen.runtime.Config;

public final class ClientBuilder<T> {

    private final Function<ClientBuilder<T>, T> creator;
    private final String basePath;
    private Serializer serializer;
    private Interceptor interceptor;

    public ClientBuilder(Function<ClientBuilder<T>, T> creator, Config config, String basePath) {
        this.creator = creator;
        this.serializer = new DefaultSerializer(config.mapper());
        this.interceptor = x -> x;
        this.basePath = trimAndRemoveFinalSlash(basePath);
    }

    public ClientBuilder<T> serializer(Serializer serializer) {
        this.serializer = serializer;
        return this;
    }

    /**
     * Enables modification of all http requests (url, method, headers, not the
     * body). Particularly useful for defining authentication (which normally
     * involves just the addition of an Authorization header).
     * 
     * @param interceptor modifies http requests
     * @return this
     */
    public ClientBuilder<T> interceptor(Interceptor interceptor) {
        this.interceptor = interceptor;
        return this;
    }

    public Serializer serializer() {
        return serializer;
    }

    public Interceptor interceptor() {
        return interceptor;
    }

    public String basePath() {
        return basePath;
    }

    public T build() {
        return creator.apply(this);
    }

    private static String trimAndRemoveFinalSlash(String s) {
        s = s.trim();
        if (s.endsWith("/")) {
            return s.substring(0, s.length() - 1);
        } else {
            return s;
        }
    }

}