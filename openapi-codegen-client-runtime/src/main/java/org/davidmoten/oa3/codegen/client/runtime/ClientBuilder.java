package org.davidmoten.oa3.codegen.client.runtime;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.davidmoten.oa3.codegen.http.DefaultSerializer;
import org.davidmoten.oa3.codegen.http.Interceptor;
import org.davidmoten.oa3.codegen.http.Serializer;
import org.davidmoten.oa3.codegen.runtime.Config;

public final class ClientBuilder<T> {

    private final Function<ClientBuilder<T>, T> creator;
    private final String basePath;
    private final List<Interceptor> interceptors;
    private Serializer serializer;

    public ClientBuilder(Function<ClientBuilder<T>, T> creator, Config config, String basePath) {
        this.creator = creator;
        this.serializer = new DefaultSerializer(config.mapper());
        this.interceptors = new ArrayList<>();
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
        this.interceptors.add(interceptor);
        return this;
    }
    
    public ClientBuilder<T> interceptors(Iterable<? extends Interceptor> list) {
        list.forEach(x -> interceptor(x));
        return this;
    }

    public Serializer serializer() {
        return serializer;
    }

    public List<Interceptor> interceptors() {
        return interceptors;
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
