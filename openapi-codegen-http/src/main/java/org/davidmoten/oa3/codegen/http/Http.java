package org.davidmoten.oa3.codegen.http;

import java.io.ByteArrayOutputStream;
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
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.davidmoten.guavamini.Preconditions;
import com.github.davidmoten.guavamini.annotations.VisibleForTesting;

public final class Http {

    public static Builder method(String method) {
        return new Builder(method);
    }

    public static final class Builder {

        private final String method;
        private String basePath;
        private String pathTemplate;
        private ObjectMapper objectMapper = new ObjectMapper();
        private final Headers headers = Headers.create();
        private final List<ParameterValue> values = new ArrayList<>();
        private final List<ResponseDescriptor> responseDescriptors = new ArrayList<>();

        Builder(String method) {
            this.method = method;
        }

        public Builder basePath(String basePath) {
            this.basePath = basePath;
            return this;
        }

        public Builder pathTemplate(String pathTemplate) {
            this.pathTemplate = pathTemplate;
            return this;
        }

        public Builder objectMapper(ObjectMapper objectMapper) {
            this.objectMapper = objectMapper;
            return this;
        }

        public Builder header(String key, String value) {
            headers.put(key, value);
            return this;
        }

        public Builder param(String name, Optional<Object> value, ParameterType type, Optional<String> contentType) {
            values.add(new ParameterValue(name, value, type, contentType));
            return this;
        }

        public Builder queryParam(String name, Object value) {
            values.add(ParameterValue.query(name, value));
            return this;
        }

        public Builder pathParam(String name, Object value) {
            values.add(ParameterValue.path(name, value));
            return this;
        }

        public Builder headerParam(String name, Object value) {
            values.add(ParameterValue.header(name, value));
            return this;
        }

        public Builder cookieParam(String name, Object value) {
            values.add(ParameterValue.cookie(name, value));
            return this;
        }

        public Builder bodyParam(Object value) {
            values.add(ParameterValue.body(value));
            return this;
        }

        public ResponseDescriptorBuilder responseAs(Class<?> cls) {
            return new ResponseDescriptorBuilder(this, cls);
        }

        public HttpResponse call() {
            return Http.call(method, basePath, pathTemplate, objectMapper, headers, values, responseDescriptors);
        }

    }

    public static final class ResponseDescriptorBuilder {

        private final Builder b;
        private String statusCode;
        private Class<?> cls;

        ResponseDescriptorBuilder(Builder b, Class<?> cls) {
            this.b = b;
            this.cls = cls;
        }

        public ResponseDescriptorBuilder whenStatusCodeMatches(String statusCode) {
            this.statusCode = statusCode;
            return this;
        }

        public Builder whenContentTypeMatches(String contentType) {
            b.responseDescriptors.add(new ResponseDescriptor(statusCode, contentType, cls));
            return b;
        }

    }

    public static HttpResponse call(//
            String method, //
            String basePath, //
            String pathTemplate, //
            ObjectMapper mapper, //
            Headers requestHeaders, //
            List<ParameterValue> parameters, //
            // (statusCode, contentType, class)
            List<ResponseDescriptor> descriptors) {
        return call(method, basePath, pathTemplate, mapper, requestHeaders, parameters, (statusCode, contentType) -> {
            List<ResponseDescriptor> matches = new ArrayList<>();
            for (ResponseDescriptor d : descriptors) {
                if (d.matches(statusCode, contentType)) {
                    matches.add(d);
                }
            }
            Collections.sort(matches, (a, b) -> Integer.compare(a.specificity(), b.specificity()));
            return matches.stream().findFirst().map(d -> d.cls());
        });
    }

    private static HttpResponse call(//
            String method, //
            String basePath, //
            String pathTemplate, //
            ObjectMapper mapper, //
            Headers requestHeaders, //
            List<ParameterValue> parameters, //
            // (statusCode x contentType) -> class
            BiFunction<? super Integer, ? super String, Optional<Class<?>>> responseCls) {
        String url = buildUrl(basePath, pathTemplate, parameters);
        Optional<ParameterValue> requestBody = parameters.stream().filter(x -> x.type() == ParameterType.BODY)
                .findFirst();
        try {
            HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
            Headers headers = new Headers(requestHeaders);
            if (method.equals("PATCH")) {
                // PATCH not supported by HttpURLConnection so use a workaround
                headers.put("X-HTTP-Method-Override", "PATCH");
                con.setRequestMethod("POST");
            } else {
                con.setRequestMethod(method);
            }

            headers.forEach((key, list) -> {
                con.setRequestProperty(key, list.stream().collect(Collectors.joining(",")));
            });

            if (requestBody.isPresent()) {
                con.setDoOutput(true);
                Optional<Object> body = requestBody.get().value();
                if (body.isPresent()) {
                    try (OutputStream out = con.getOutputStream()) {
                        out.write(mapper.writeValueAsBytes(body.get()));
                    }
                }
            }
            con.setDoInput(true);
            int statusCode = con.getResponseCode();
            Map<String, List<String>> responseHeaders = con.getHeaderFields();
            String responseContentType = Optional.ofNullable(con.getHeaderField("Content-Type"))
                    .orElse("application/octet-stream");
            Object data;
            Optional<Class<?>> responseType = responseCls.apply(statusCode, responseContentType);
            try (InputStream in = con.getInputStream()) {
                data = readResponse(mapper, responseType, in);
            } catch (IOException e) {
                try (InputStream err = con.getErrorStream()) {
                    data = readResponse(mapper, responseType, err);
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
                .map(p -> urlEncode(p.name()) + "=" + p.value().map(x -> valueToString(x)).orElse("")) //
                .collect(Collectors.joining("&"));
        return path + "?" + queryString;
    }

    private static Object readResponse(ObjectMapper mapper, Optional<Class<?>> responseType, InputStream in)
            throws IOException, StreamReadException, DatabindException {
        if (responseType.isPresent()) {
            return mapper.readValue(in, responseType.get());
        } else {
            return new String(read(in), StandardCharsets.UTF_8);
        }
    }

    private static byte[] read(InputStream in) throws IOException {
        byte[] buffer = new byte[8192];
        int n = 0;
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        while ((n = in.read(buffer)) != -1) {
            bytes.write(buffer, 0, n);
        }
        return bytes.toByteArray();
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
