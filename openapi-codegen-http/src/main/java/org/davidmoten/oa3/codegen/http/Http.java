package org.davidmoten.oa3.codegen.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import org.davidmoten.oa3.codegen.http.service.DefaultHttpService;
import org.davidmoten.oa3.codegen.http.service.HttpConnection;
import org.davidmoten.oa3.codegen.http.service.HttpService;
import org.davidmoten.oa3.codegen.http.service.Option;
import org.davidmoten.oa3.codegen.http.service.Response;
import org.davidmoten.oa3.codegen.http.service.StandardOption;
import org.davidmoten.oa3.codegen.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.davidmoten.guavamini.Maps;
import com.github.davidmoten.guavamini.Preconditions;
import com.github.davidmoten.guavamini.annotations.VisibleForTesting;

public final class Http {

	private static Logger log = LoggerFactory.getLogger(Http.class);

	private static final long DEFAULT_CONNECT_TIMEOUT_MS = 30000L;
    private static final long DEFAULT_READ_TIMEOUT_MS = 180000L;

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
        private HttpService httpService = DefaultHttpService.INSTANCE;
		private Optional<String> assertStatusCodeMatches = Optional.empty();
		private Optional<String> assertContentTypeMatches = Optional.empty();
		public Optional<Long> connectTimeoutMs = Optional.empty();
		public Optional<Long> readTimeoutMs = Optional.empty();

        Builder(HttpMethod method) {
            this.method = method;
        }

        public BuilderWithBasePath basePath(String basePath) {
            this.basePath = basePath;
            return new BuilderWithBasePath(this);
        }
        
        public Builder httpService(HttpService httpService) {
            this.httpService = httpService;
            return this;
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
        
        public Builder connectTimeout(long duration, TimeUnit unit) {
        	Preconditions.checkArgumentNotNull(duration, "duration");
        	Preconditions.checkArgumentNotNull(unit, "unit");
        	this.connectTimeoutMs = Optional.of(unit.toMillis(duration));
        	return this;
        }
        
        public Builder readTimeout(long duration, TimeUnit unit) {
        	Preconditions.checkArgumentNotNull(duration, "duration");
        	Preconditions.checkArgumentNotNull(unit, "unit");
        	this.readTimeoutMs = Optional.of(unit.toMillis(duration));
        	return this;
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
            return accept("application/json");
        }

        public Builder acceptAny() {
            return accept("*/*");
        }
        
        public Builder accept(String mediaType) {
            return header("Accept", mediaType);
        }

        public Builder param(String name, Optional<?> value, ParameterType type, Optional<String> contentType) {
            return param(name, value, type, contentType, Optional.empty());
        }

        public Builder param(String name, Optional<?> value, ParameterType type, Optional<String> contentType,
                Optional<String> filename) {
            values.add(new ParameterValue(name, value, type, contentType, filename));
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

        public Builder multipartFormData(Object formData) {
            return new BuilderWithBody(this, formData).contentTypeMultipartFormData();
        }

        public Builder formUrlEncoded(Object formData) {
            return new BuilderWithBody(this, formData).contentTypeFormUrlEncoded();
        }

        public BuilderWithReponseDescriptor responseAs(Class<?> cls) {
            return new BuilderWithReponseDescriptor(this, cls);
        }
        
        public <T> RequestBuilder<T> requestBuilder() {
        	return new RequestBuilder<>(this);
        }
        
        public <T> RequestBuilder<T> requestBuilder(String primaryStatusCode, String primaryMediaType) {
        	Preconditions.checkArgumentNotNull(primaryStatusCode);
        	Preconditions.checkArgumentNotNull(primaryMediaType);
        	this.assertStatusCodeMatches = Optional.of(primaryStatusCode);
            this.assertContentTypeMatches = Optional.of(primaryMediaType);
        	return new RequestBuilder<T>(this);
        }

        public HttpResponse call() {
        	return Http.call(httpService, method, basePath, path, serializer, interceptors, headers, values, responseDescriptors,
                    allowPatch, connectTimeoutMs, readTimeoutMs);
        }
        
        public HttpResponse callAssertIsPrimaryResponse() {
            HttpResponse r = call();
            if (assertStatusCodeMatches.isPresent()) {
            	r.assertStatusCodeMatches(assertStatusCodeMatches.get());
            }
            if (assertContentTypeMatches.isPresent()) {
            	r.assertContentTypeMatches(assertContentTypeMatches.get());
            }
            return r;
        }

    }
    
    public static final class RequestBuilder<T> {

		private Builder builder;

		public RequestBuilder(Builder builder) {
			this.builder = builder;
		}
    	
		public RequestBuilder<T> acceptApplicationJson() {
            builder.accept("application/json");
            return this;
        }

        public RequestBuilder<T> acceptAny() {
            builder.accept("*/*");
            return this;
        }
        
        public RequestBuilder<T> accept(String mediaType) {
            builder.header("Accept", mediaType);
            return this;
        }
        
        public RequestBuilder<T> header(String name, String value) {
            builder.header(name, value);
            return this;
        }
        
        public RequestBuilder<T> interceptor(Interceptor interceptor) {
        	builder.interceptor(interceptor);
        	return this;
        }
        
        public RequestBuilder<T> connectTimeout(long duration, TimeUnit unit) {
        	builder.connectTimeout(duration, unit);
        	return this;
        }
        
        public RequestBuilder<T> readTimeout(long duration, TimeUnit unit) {
        	builder.readTimeout(duration, unit);
        	return this;
        }
        
        public HttpResponse fullResponse() {
        	return builder.call();
        }
        
        public T get() {
        	return builder.callAssertIsPrimaryResponse().dataUnwrapped();
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

        public Builder contentTypeFormUrlEncoded() {
            return contentType("application/x-www-form-urlencoded");
        }

        public Builder contentType(String value) {
            b.values.add(ParameterValue.body(body, value));
            return b;
        }

        public Builder contentTypeApplicationJson() {
            return contentType("application/json");
        }

        public Builder contentTypeMultipartFormData() {
            return contentType("multipart/form-data");
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
            HttpService httpService, //
            HttpMethod method, //
            String basePath, //
            String pathTemplate, //
            Serializer serializer, //
            List<Interceptor> interceptors, //
            Headers requestHeaders, //
            List<ParameterValue> parameters, //
            // (statusCode, contentType, class)
            List<ResponseDescriptor> descriptors, boolean allowPatch, Optional<Long> connectTimeoutMs, Optional<Long> readTimeoutMs) {
        return call(httpService, method, basePath, pathTemplate, serializer, interceptors, requestHeaders, parameters,
                (statusCode, contentType) -> match(descriptors, statusCode, contentType), allowPatch, connectTimeoutMs, readTimeoutMs);
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
            HttpService httpService, //
            HttpMethod method, //
            String basePath, //
            String pathTemplate, //
            Serializer serializer, //
            List<Interceptor> interceptors, //
            Headers requestHeaders, //
            List<ParameterValue> parameters, //
            // (statusCode x contentType) -> class
            BiFunction<? super Integer, ? super String, Optional<Class<?>>> responseCls, boolean allowPatch,
            		Optional<Long> connectTimeoutMs, Optional<Long> readTimeoutMs) {
        String url = buildUrl(basePath, pathTemplate, parameters);
        Optional<ParameterValue> requestBody = parameters.stream().filter(x -> x.type() == ParameterType.BODY)
                .findFirst();
        try {
            Headers headers = new Headers(requestHeaders);

            // modify request metadata (like insert auth related headers)
            RequestBase r = new RequestBase(method, url, headers);
            for (Interceptor interceptor : interceptors) {
                r = interceptor.intercept(r);
            }
            final Option[] options;
            if (allowPatch) {
                options = new Option[] {};
            } else {
                options = new Option[] { StandardOption.PATCH_USING_HEADER };
            }
            log.debug("connecting to method=" + r.method() + ", url=" + url + ", headers=" + r.headers());
            return connectAndProcess(serializer, parameters, responseCls, r.url(), requestBody, r.headers(), r.method(),
                    httpService, connectTimeoutMs, readTimeoutMs, options);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static HttpResponse connectAndProcess(Serializer serializer, List<ParameterValue> parameters,
            BiFunction<? super Integer, ? super String, Optional<Class<?>>> responseCls, String url,
            Optional<ParameterValue> requestBody, Headers headers, final HttpMethod method, HttpService httpService,
            Optional<Long> connectTimeoutMs, Optional<Long> readTimeoutMs,
            Option... options)
            throws IOException, MalformedURLException, ProtocolException, StreamReadException, DatabindException {
        log.debug("Http.headers={}", headers);
        HttpConnection con = httpService.connection(url, method, options);
        con.setConnectTimeoutMs(connectTimeoutMs.orElse(DEFAULT_CONNECT_TIMEOUT_MS));
        con.setReadTimeoutMs(readTimeoutMs.orElse(DEFAULT_READ_TIMEOUT_MS));
        parameters.stream() //
                .filter(p -> p.type() == ParameterType.HEADER && p.value().isPresent()) //
                .forEach(p -> headers.put(p.name(), String.valueOf(p.value().get())));
        headers.forEach((key, list) -> {
            con.header(key, list.stream().collect(Collectors.joining(", ")));
        });
        if (requestBody.isPresent()) {
            Optional<?> body = requestBody.get().value();
            if (body.isPresent()) {
                String contentType = requestBody.get().contentType().orElse("");
                if (MediaType.isMultipartFormData(contentType)) {
                    String boundary = Multipart.randomBoundary();
                    String ct = "multipart/form-data; boundary="+ boundary;
                    // TODO stream content rather than build in memory?
                    // TODO support parts without names?
                    byte[] multipartContent = multipartContent(serializer, body, boundary);
                    con.output(out -> write(out, multipartContent), ct, Optional.empty(), false);
                } else if (MediaType.isWwwFormUrlEncoded(contentType)) {
                    byte[] encoded = wwwFormUrlEncodedContent(serializer, body).getBytes(StandardCharsets.UTF_8);
                    con.output(out -> write(out, encoded), contentType, Optional.empty(), false);
                } else {
                    con.output(out -> serializer.serialize(body.get(), contentType, out),
                            requestBody.get().contentType().get(), Optional.empty(), false);
                }
            }
        }
        Response response = con.response();
        int statusCode = response.statusCode();
        Headers responseHeaders = Headers.create(response.headers());
        String responseContentType = last(response.headers().get("Content-Type")).orElse("application/octet-stream");
        Object data;
        Optional<Class<?>> responseClass = responseCls.apply(statusCode, responseContentType);
        try (InputStream in = log(response.inputStream())) {
            data = readResponse(serializer, responseClass, responseContentType, in);
        }
        return new HttpResponse(statusCode, responseHeaders, Optional.of(data));
    }

    private static void write(OutputStream out, byte[] bytes) {
        try {
            out.write(bytes);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static <T> Optional<T> last(List<T> list) {
        if (list.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(list.get(list.size() - 1));
        }
    }

    private static String wwwFormUrlEncodedContent(Serializer serializer, Optional<?> body) {
        Map<String, Object> map = properties(body.get());
        String encoded = map.entrySet().stream().map(entry -> {
            String json = new String(serializer.serialize(entry.getValue(), "application/json"),
                    StandardCharsets.UTF_8);
            ObjectMapper m = new ObjectMapper();
            try {
                JsonNode tree = m.readTree(json);
                String v = tree.asText();
                return URLEncoder.encode(entry.getKey(), "UTF-8") + "=" + URLEncoder.encode(v, "UTF-8");
            } catch (JsonProcessingException | UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        }).collect(Collectors.joining("&"));
        return encoded;
    }

    private static byte[] multipartContent(Serializer serializer, Optional<?> body, String boundary) {
        Map<String, Object> map = properties(body.get());
        Multipart.Builder b = Multipart.builder();
        map.forEach((name, value) -> {
            if (value != null) {
                final String contentType;
                final Object v;
                if (value instanceof HasEncoding) {
                    contentType = ((HasStringValue) ((HasEncoding) value).contentType()).value();
                    v = ((HasEncoding) value).value();
                } else {
                    contentType = "application/json";
                    v = value;
                }
                b.addFormEntry(name, serializer.serialize(v, contentType), Optional.empty(), Optional.of(contentType));
            }
        });
        return b.multipartContent(boundary);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> properties(Object o) {
        try {
            Method method = o.getClass().getDeclaredMethod("_internal_properties");
            method.setAccessible(true);
            return (Map<String, Object>) method.invoke(o);
        } catch (IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
            return Maps.empty();
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
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
