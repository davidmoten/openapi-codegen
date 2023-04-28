package org.davidmoten.oa3.codegen.generator;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpMethod;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.davidmoten.guavamini.Preconditions;

public class Http {

    public static <T> HttpResponse<T> call(//
            ObjectMapper mapper, //
            HttpMethod method, //
            String basePath, //
            String pathTemplate, //
            Headers headers, //
            ParameterValue... parameters) {
        Preconditions.checkArgument(pathTemplate.startsWith("/"));
        // substitute path parameters
        String path = stripFinalSlash(basePath) + insertParameters(pathTemplate, Arrays.asList(parameters), mapper);
        // build query string
        String queryString = Arrays.stream(parameters).filter(p -> p.type() == ParamType.QUERY) //
                .map(p -> urlEncode(p.name() + "=" + p.value().map(x -> valueToString(x)).orElse(""))) //
                .collect(Collectors.joining("&"));
        // make call
        // return response

        return null;
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
