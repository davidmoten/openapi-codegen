package org.davidmoten.oa3.codegen.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import org.davidmoten.oa3.codegen.util.Util;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.github.davidmoten.guavamini.Preconditions;
import com.github.davidmoten.guavamini.annotations.VisibleForTesting;

public final class Http {

    public static Builder method(HttpMethod method) {
        return new Builder(method);
    }

    public static final class Builder {

        private final HttpMethod method;
        private String basePath;
        private String path;
        private final Headers headers = Headers.create();
        private final List<ParameterValue> values = new ArrayList<>();
        private final List<ResponseDescriptor> responseDescriptors = new ArrayList<>();
        private Serializer serializer;

        Builder(HttpMethod method) {
            this.method = method;
        }

        public BuilderWithBasePath basePath(String basePath) {
            this.basePath = basePath;
            return new BuilderWithBasePath(this);
        }

        public Builder header(String key, String value) {
            if ("CONTENT-TYPE".equals(key.toUpperCase(Locale.ENGLISH))) {
                throw new IllegalArgumentException("set content type in the builder just after setting the body");
            }
            headers.put(key, value);
            return this;
        }

        public Builder acceptApplicationJson() {
            return header("Accept", "application/json");
        }

        public Builder acceptAny() {
            return header("Accept", "*/*");
        }

        public Builder param(String name, Optional<Object> value, ParameterType type, Optional<String> contentType) {
            values.add(new ParameterValue(name, value, type, contentType));
            return this;
        }

        public Builder queryParam(String name, Optional<?> value) {
            values.add(ParameterValue.query(name, value));
            return this;
        }

        public Builder queryParam(String name, Object value) {
            values.add(ParameterValue.query(name, value));
            return this;
        }

        public Builder pathParam(String name, Optional<?> value) {
            values.add(ParameterValue.path(name, value));
            return this;
        }

        public Builder pathParam(String name, Object value) {
            values.add(ParameterValue.path(name, value));
            return this;
        }

        public Builder cookie(String name, Object value) {
            values.add(ParameterValue.cookie(name, value));
            return this;
        }

        public BuilderWithBody body(Object value) {
            return new BuilderWithBody(this, value);
        }

        public BuilderWithReponseDescriptor responseAs(Class<?> cls) {
            return new BuilderWithReponseDescriptor(this, cls);
        }

        public HttpResponse call() {
            return Http.call(method, basePath, path, serializer, headers, values, responseDescriptors);
        }

    }

    public static final class BuilderWithBasePath {

        private final Builder b;

        BuilderWithBasePath(Builder b) {
            this.b = b;
        }

        public BuilderWithPath path(String path) {
            b.path = path;
            return new BuilderWithPath(b);
        }
    }

    public static final class BuilderWithPath {

        private final Builder b;

        BuilderWithPath(Builder b) {
            this.b = b;
        }

        public Builder serializer(Serializer serializer) {
            b.serializer = serializer;
            return b;
        }
    }

    public static final class BuilderWithBody {

        private final Builder b;
        private final Object body;

        BuilderWithBody(Builder b, Object body) {
            this.b = b;
            this.body = body;
        }

        public Builder contentType(String value) {
            b.values.add(ParameterValue.body(body, value));
            return b;
        }

        public Builder contentTypeApplicationJson() {
            return contentType("application/json");
        }

    }

    public static final class BuilderWithReponseDescriptor {

        private final Builder b;
        private String statusCode;
        private Class<?> cls;

        BuilderWithReponseDescriptor(Builder b, Class<?> cls) {
            this.b = b;
            this.cls = cls;
        }

        public BuilderWithStatusCodeMatch whenStatusCodeMatches(String statusCode) {
            this.statusCode = statusCode;
            return new BuilderWithStatusCodeMatch(this);
        }

        public BuilderWithStatusCodeMatch whenStatusCodeDefault() {
            this.statusCode = "default";
            return new BuilderWithStatusCodeMatch(this);
        }

    }

    public static final class BuilderWithStatusCodeMatch {

        private final BuilderWithReponseDescriptor brd;

        public BuilderWithStatusCodeMatch(BuilderWithReponseDescriptor brd) {
            this.brd = brd;
        }

        public Builder whenContentTypeMatches(String contentType) {
            brd.b.responseDescriptors.add(new ResponseDescriptor(brd.statusCode, contentType, brd.cls));
            return brd.b;
        }

    }

    public static HttpResponse call(//
            HttpMethod method, //
            String basePath, //
            String pathTemplate, //
            Serializer serializer, //
            Headers requestHeaders, //
            List<ParameterValue> parameters, //
            // (statusCode, contentType, class)
            List<ResponseDescriptor> descriptors) {
        return call(method, basePath, pathTemplate, serializer, requestHeaders, parameters,
                (statusCode, contentType) -> match(descriptors, statusCode, contentType));
    }

    private static Optional<Class<?>> match(List<ResponseDescriptor> descriptors, Integer statusCode,
            String contentType) {
        List<ResponseDescriptor> matches = new ArrayList<>();
        for (ResponseDescriptor d : descriptors) {
            if (d.matches(statusCode, contentType)) {
                matches.add(d);
            }
        }
        Collections.sort(matches, ResponseDescriptor.specificity());
        return matches.stream().findFirst().map(d -> d.cls());
    }

    private static HttpResponse call(//
            HttpMethod method, //
            String basePath, //
            String pathTemplate, //
            Serializer serializer, //
            Headers requestHeaders, //
            List<ParameterValue> parameters, //
            // (statusCode x contentType) -> class
            BiFunction<? super Integer, ? super String, Optional<Class<?>>> responseCls) {
        String url = buildUrl(basePath, pathTemplate, parameters);
        System.out.println("Http.url=" + url);
        Optional<ParameterValue> requestBody = parameters.stream().filter(x -> x.type() == ParameterType.BODY)
                .findFirst();
        try {
            HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
            Headers headers = new Headers(requestHeaders);
            System.out.println("Http.headers=" + headers);
            if (method.equals(HttpMethod.PATCH)) {
                // PATCH not supported by HttpURLConnection so use a workaround
                headers.put("X-HTTP-Method-Override", HttpMethod.PATCH.name());
                con.setRequestMethod(HttpMethod.POST.name());
            } else {
                con.setRequestMethod(method.name());
            }
            // add request body content type (should just be one)
            parameters.stream().filter(p -> p.contentType().isPresent())
                    .forEach(p -> headers.put("Content-Type", p.contentType().get()));
            headers.forEach((key, list) -> {
                con.setRequestProperty(key, list.stream().collect(Collectors.joining(",")));
            });
            con.setDoInput(true);
            if (requestBody.isPresent()) {
                con.setDoOutput(true);
                Optional<?> body = requestBody.get().value();
                if (body.isPresent()) {
                    try (OutputStream out = con.getOutputStream()) {
                        serializer.serialize(body.get(), requestBody.get().contentType().get(), out);
                    }
                }
            }
            int statusCode = con.getResponseCode();
            Map<String, List<String>> responseHeaders = con.getHeaderFields();
            String responseContentType = Optional.ofNullable(con.getHeaderField("Content-Type"))
                    .orElse("application/octet-stream");
            Object data;
            Optional<Class<?>> responseClass = responseCls.apply(statusCode, responseContentType);
            try (InputStream in = con.getInputStream()) {
                data = readResponse(serializer, responseClass, responseContentType, in);
            } catch (IOException e) {
                try (InputStream err = con.getErrorStream()) {
                    data = readResponse(serializer, responseClass, responseContentType, err);
                }
            }
            return new HttpResponse(statusCode, responseHeaders, Optional.of(data));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @VisibleForTesting
    static String buildUrl(String basePath, String pathTemplate, List<ParameterValue> parameters) {
        Preconditions.checkArgument(pathTemplate.startsWith("/"));
        // substitute path parameters
        String path = stripFinalSlash(basePath) + insertParametersIntoPath(pathTemplate, parameters);
        // build query string
        String queryString = parameters //
                .stream() //
                .filter(p -> p.type() == ParameterType.QUERY) //
                .filter(p -> p.value().isPresent()) //
                .map(p -> urlEncode(p.name()) + "=" + p.value().map(x -> valueToString(x)).orElse("")) //
                .collect(Collectors.joining("&"));
        return path + "?" + queryString;
    }

    private static Object readResponse(Serializer serializer, Optional<Class<?>> responseType,
            String responseContentType, InputStream in) throws IOException, StreamReadException, DatabindException {
        if (responseType.isPresent()) {
            return serializer.deserialize(responseType.get(), responseContentType, in);
        } else {
            return new String(Util.read(in), StandardCharsets.UTF_8);
        }
    }

    private static String valueToString(Object value) {
        if (value == null) {
            return "";
        } else if (value instanceof Collection) {
            Collection<?> c = (Collection<?>) value;
            return c.stream().map(x -> valueToString(x)).collect(Collectors.joining(","));
        } else {
            return urlEncode(value.toString());
        }
    }

    private static String stripFinalSlash(String s) {
        if (s.endsWith("/")) {
            return s.substring(0, s.length() - 2);
        } else {
            return s;
        }
    }

    private static String urlEncode(String s) {
        try {
            return URLEncoder.encode(s, "UTF-8").replaceAll("\\+", "%20");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    private static String insertParametersIntoPath(String pathTemplate, List<ParameterValue> parameters) {
        String s = pathTemplate;
        for (ParameterValue p : parameters) {
            if (p.type() == ParameterType.PATH) {
                s = insertParameter(s, p.name(), p.value().get());
            }
        }
        return s;
    }

    private static String insertParameter(String s, String name, Object object) {
        return s.replace("{" + name + "}", urlEncode(object.toString()));
    }

}
