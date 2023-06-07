package org.davidmoten.oa3.codegen.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Random;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.davidmoten.oa3.codegen.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.github.davidmoten.guavamini.Preconditions;
import com.github.davidmoten.guavamini.annotations.VisibleForTesting;

public final class Http {

    private static Logger log = LoggerFactory.getLogger(Http.class);

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
        private List<Interceptor> interceptors = new ArrayList<>();
        private boolean allowPatch = false;

        Builder(HttpMethod method) {
            this.method = method;
        }

        public BuilderWithBasePath basePath(String basePath) {
            this.basePath = basePath;
            return new BuilderWithBasePath(this);
        }

        public Builder header(String key, Object value) {
            if ("CONTENT-TYPE".equals(key.toUpperCase(Locale.ENGLISH))) {
                throw new IllegalArgumentException("set content type in the builder just after setting the body");
            }
            if (value != null) {
                headers.put(key, value.toString());
            }
            return this;
        }

        public Builder header(String key, Optional<?> value) {
            if (value.isPresent()) {
                return header(key, value.get());
            } else {
                return this;
            }
        }

        public Builder allowPatch() {
            return allowPatch(true);
        }

        private Builder allowPatch(boolean allowPatch) {
            this.allowPatch = allowPatch;
            return this;
        }

        public Builder interceptor(Interceptor interceptor) {
            this.interceptors.add(interceptor);
            return this;
        }

        public Builder interceptors(Iterable<? extends Interceptor> list) {
            interceptors.forEach(x -> interceptor(x));
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

        public Builder urlEncoded(String name, Object value) {
            if (values.stream()
                    .anyMatch(x -> x.type() == ParameterType.FORM_MULTIPART || x.type() == ParameterType.BODY)) {
                throw new IllegalArgumentException(
                        "cannot set url encoded parameter because body already set by multipart or directly");
            }
            values.add(ParameterValue.urlEncoded(name, String.valueOf(value)));
            return this;
        }

        public Builder multipart(byte[] value, Optional<String> contentType) {
            if (values.stream()
                    .anyMatch(x -> x.type() == ParameterType.FORM_URLENCODED || x.type() == ParameterType.BODY)) {
                throw new IllegalArgumentException(
                        "cannot set multipart parameter because body already set by urlEncoded or directly");
            }
            values.add(ParameterValue.multipart(value, contentType));
            return this;
        }

        public Builder multipart(String name, byte[] value, Optional<String> contentType) {
            if (values.stream()
                    .anyMatch(x -> x.type() == ParameterType.FORM_URLENCODED || x.type() == ParameterType.BODY)) {
                throw new IllegalArgumentException(
                        "cannot set multipart parameter because body already set by urlEncoded or directly");
            }
            values.add(ParameterValue.multipart(name, value, contentType));
            return this;
        }

        public Builder multipart(String name, Object o) {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            serializer.serialize(o, "application/json", bytes);
            return multipart(name, bytes.toByteArray(), Optional.of("application/json"));
        }

        public BuilderWithBody body(Object value) {
            if (values.stream().anyMatch(
                    x -> x.type() == ParameterType.FORM_URLENCODED || x.type() == ParameterType.FORM_MULTIPART)) {
                throw new IllegalArgumentException(
                        "cannot set body parameter because body already set by urlEncoded or multipart");
            }
            return new BuilderWithBody(this, value);
        }

        public BuilderWithReponseDescriptor responseAs(Class<?> cls) {
            return new BuilderWithReponseDescriptor(this, cls);
        }

        public HttpResponse call() {
            return Http.call(method, basePath, path, serializer, interceptors, headers, values, responseDescriptors,
                    allowPatch);
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
            List<Interceptor> interceptors, //
            Headers requestHeaders, //
            List<ParameterValue> parameters, //
            // (statusCode, contentType, class)
            List<ResponseDescriptor> descriptors, boolean allowPatch) {
        return call(method, basePath, pathTemplate, serializer, interceptors, requestHeaders, parameters,
                (statusCode, contentType) -> match(descriptors, statusCode, contentType), allowPatch);
    }

    public static String encodeFormEntry(String key, Object value) {
        try {
            return URLEncoder.encode(key, "UTF-8") + "=" + URLEncoder.encode(value.toString(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
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
            List<Interceptor> interceptors, //
            Headers requestHeaders, //
            List<ParameterValue> parameters, //
            // (statusCode x contentType) -> class
            BiFunction<? super Integer, ? super String, Optional<Class<?>>> responseCls, boolean allowPatch) {
        String url = buildUrl(basePath, pathTemplate, parameters);
        Optional<ParameterValue> requestBody = parameters.stream().filter(x -> x.type() == ParameterType.BODY)
                .findFirst();
        Optional<String> urlEncodedBody = urlEncodedBody(parameters);
        Optional<Multipart> multipartBody = multipartBody(parameters);
//      return b.body(body).contentType("application/x-www-form-urlencoded");
        try {
            Headers headers = new Headers(requestHeaders);
            final HttpMethod requestMethod;
            if (!allowPatch && method.equals(HttpMethod.PATCH)) {
                headers.put("X-HTTP-Method-Override", HttpMethod.PATCH.name());
                requestMethod = HttpMethod.POST;
            } else {
                requestMethod = method;
            }
            // modify request metadata (like insert auth related headers)
            RequestBase r = new RequestBase(requestMethod, url, headers);
            for (Interceptor interceptor : interceptors) {
                r = interceptor.intercept(r);
            }
            log.debug("connecting to method=" + r.method() + ", url=" + url + ", headers=" + r.headers());
            return connectAndProcess(serializer, parameters, responseCls, r.url(), requestBody, urlEncodedBody,
                    multipartBody, r.headers(), r.method());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static final class Multipart {
        final Headers headers;
        final byte[] content;

        Multipart(Headers headers, byte[] content) {
            this.headers = headers;
            this.content = content;
        }
    }

    private static Optional<Multipart> multipartBody(List<ParameterValue> parameters) {
        Headers headers = new Headers();

        String boundary = createRandomBoundary();

        Utf8ByteArrayOutputStream b = new Utf8ByteArrayOutputStream();
        parameters.stream() //
                .filter(x -> x.type() == ParameterType.FORM_MULTIPART) //
                .forEach(x -> {
                    try {
                        String contentType = x.contentType().orElse("text/plain");
                        b.write(boundary);
                        b.write("\r\n");
                        b.write("Content-Type: " + contentType + "\r\n");
                        b.write("Content-Disposition: form-data; name=\"" + x.name() + "\"");
                        b.write("\r\n");
                        b.write("\r\n");
                        b.write(x.value().map(y -> (byte[]) y).orElse(new byte[0]));
                        b.write("\r\n");
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                });
        try {
            if (b.size() > 0) {
                b.write(boundary + "--\r\n");
            } else {
                return Optional.empty();
            }
            headers.put("Content-Type", "multipart/form-data; boundary=" + boundary);
            headers.put("Content-Length", Integer.toString(b.size()));

            return Optional.of(new Multipart(headers, b.toByteArray()));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static final Random RANDOM = new Random();

    private static String createRandomBoundary() {
        char[] a = new char[32];
        for (int i = 0; i < a.length; i++) {
            a[i] = (char) (48 + Math.abs(RANDOM.nextInt() % 10));
        }
        return "--------" + new String(a);
    }

    private static Optional<String> urlEncodedBody(List<ParameterValue> parameters) {
        String s = parameters.stream() //
                .filter(a -> a.type() == ParameterType.FORM_URLENCODED) //
                .map(a -> encodeFormEntry(a.name(), a.value().get())) //
                .collect(Collectors.joining("&"));
        if (s.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(s);
        }
    }

    private static HttpResponse connectAndProcess(Serializer serializer, List<ParameterValue> parameters,
            BiFunction<? super Integer, ? super String, Optional<Class<?>>> responseCls, String url,
            Optional<ParameterValue> requestBody, Optional<String> urlEncodedBody, Optional<Multipart> multipartBody,
            Headers headers, final HttpMethod method)
            throws IOException, MalformedURLException, ProtocolException, StreamReadException, DatabindException {
        Preconditions.checkArgument(
                Stream.of(requestBody, urlEncodedBody, multipartBody).filter(Optional::isPresent).count() <= 1,
                "a maximum of one of requestBody, urlEncodedBody, multipartBody can be present");
        log.debug("Http.headers={}", headers);
        HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
        con.setRequestMethod(method.name());
        parameters.stream() //
                .filter(p -> p.type() == ParameterType.HEADER && p.value().isPresent()) //
                .forEach(p -> headers.put(p.name(), String.valueOf(p.value().get())));
        // add request body content type (should just be one)
        parameters.stream().filter(p -> p.contentType().isPresent() && p.type() == ParameterType.BODY)
                .forEach(p -> headers.put("Content-Type", p.contentType().get()));
        writeHeaders(headers, con);
        
        if (requestBody.isPresent()) {
            writeHeaders(headers, con);
            con.setDoOutput(true);
            con.setDoInput(true);
            Optional<?> body = requestBody.get().value();
            if (body.isPresent()) {
                try (OutputStream out = con.getOutputStream()) {
                    serializer.serialize(body.get(), requestBody.get().contentType().get(), out);
                }
            }
        } else if (multipartBody.isPresent()) {
            headers.putAll(multipartBody.get().headers);
            writeHeaders(headers, con);
            con.setDoOutput(true);
            con.setDoInput(true);
            try (OutputStream out = con.getOutputStream()) {
                out.write(multipartBody.get().content);
            }
        } else if (urlEncodedBody.isPresent()) {
            writeHeaders(headers, con);
            con.setDoOutput(true);
            con.setDoInput(true);
            try (OutputStream out = con.getOutputStream()) {
                out.write(urlEncodedBody.get().getBytes(StandardCharsets.UTF_8));
            }
        } else {
            writeHeaders(headers, con);
            con.setDoInput(true);
        }
        int statusCode = con.getResponseCode();
        Headers responseHeaders = Headers.create(con.getHeaderFields());
        String responseContentType = Optional.ofNullable(con.getHeaderField("Content-Type"))
                .orElse("application/octet-stream");
        Object data;
        Optional<Class<?>> responseClass = responseCls.apply(statusCode, responseContentType);
        try (InputStream in = log(con.getInputStream())) {
            data = readResponse(serializer, responseClass, responseContentType, in);
        } catch (IOException e) {
            try (InputStream err = log(con.getErrorStream())) {
                data = readResponse(serializer, responseClass, responseContentType, err);
            }
        }
        return new HttpResponse(statusCode, responseHeaders, Optional.of(data));
    }

    private static void writeHeaders(Headers headers, HttpURLConnection con) {
        headers.forEach((key, list) -> {
            con.setRequestProperty(key, list.stream().collect(Collectors.joining(", ")));
        });
    }

    private static InputStream log(InputStream inputStream) {
        if (!log.isDebugEnabled()) {
            return inputStream;
        }
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        return new InputStream() {

            @Override
            public int read() throws IOException {
                int v = inputStream.read();
                if (v == -1) {
                    log.debug("Http.inputStream=\n{}", new String(bytes.toByteArray(), StandardCharsets.UTF_8));
                }
                bytes.write(v);
                return v;
            }

        };
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
