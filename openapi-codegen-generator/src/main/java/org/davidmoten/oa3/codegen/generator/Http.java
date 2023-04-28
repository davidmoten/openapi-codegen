package org.davidmoten.oa3.codegen.generator;

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
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import org.springframework.http.HttpMethod;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.davidmoten.guavamini.Preconditions;

public final class Http {

    public static HttpResponse call(//
            ObjectMapper mapper, //
            HttpMethod method, //
            String basePath, //
            String pathTemplate, //
            Headers requestHeaders, //
            List<ParameterValue> parameters, //
            // (statusCode x contentType) -> class
            BiFunction<? super Integer, ? super Optional<String>, Optional<Class<?>>> responseCls) {
        Preconditions.checkArgument(pathTemplate.startsWith("/"));
        // substitute path parameters
        String path = stripFinalSlash(basePath) + insertParameters(pathTemplate, parameters, mapper);
        // build query string
        String queryString = parameters.stream().filter(p -> p.type() == ParamType.QUERY) //
                .map(p -> urlEncode(p.name() + "=" + p.value().map(x -> valueToString(x)).orElse(""))) //
                .collect(Collectors.joining("&"));
        String url = path + "?" + queryString;
        Optional<ParameterValue> requestBody = parameters.stream().filter(x -> x.type() == ParamType.BODY).findFirst();
        try {
            HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
            Headers headers = new Headers(requestHeaders);
            if (method == HttpMethod.PATCH) {
                // PATCH not supported by HttpURLConnection so use a workaround
                headers.put("X-HTTP-Method-Override", HttpMethod.PATCH.name());
                con.setRequestMethod(HttpMethod.POST.name());
            } else {
                con.setRequestMethod(method.name());
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
            Optional<String> responseContentType = Optional.ofNullable(con.getHeaderField("Content-Type"));
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

    private static String insertParameters(String pathTemplate, List<ParameterValue> parameters, ObjectMapper m) {
        String s = pathTemplate;
        for (ParameterValue p : parameters) {
            if (p.type() == ParamType.PATH) {
                s = insertParameter(s, p.value().get(), m);
            }
        }
        return s;
    }

    private static String insertParameter(String s, Object object, ObjectMapper m) {
        return s;
    }

}
