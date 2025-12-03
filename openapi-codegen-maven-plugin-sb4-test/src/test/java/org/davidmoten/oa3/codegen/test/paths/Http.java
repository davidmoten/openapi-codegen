package org.davidmoten.oa3.codegen.test.paths;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

import com.fasterxml.jackson.databind.ObjectMapper;

public final class Http {

    private Http() {
        // prevent instantiation
    }

    public static <T> T read(String url, HttpMethod method, Class<T> cls, HttpHeaders headers, ObjectMapper mapper) {
        try {
            URL u = toUrl(url);
            HttpURLConnection con = (HttpURLConnection) u.openConnection();
            con.setRequestMethod(method.toString());
            con.setDoInput(true);
            headers.forEach((k, v) -> con.setRequestProperty(k, v.stream().collect(Collectors.joining(","))));
            try (InputStream in = con.getInputStream()) {
                return mapper.readValue(in, cls);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static String readString(String url, HttpMethod method, String acceptHeader) {
        try {
            URL u = toUrl(url);
            HttpURLConnection con = (HttpURLConnection) u.openConnection();
            con.setRequestMethod(method.toString());
            con.setDoInput(true);
            con.setRequestProperty("Accept", acceptHeader);
            try (InputStream in = con.getInputStream()) {
                return new String(read(in), StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static String readStringFromOctetStream(String url, HttpMethod method) {
        return readString(url, method, "application/octet-stream");
    }

    public static <T> T read(String url, HttpMethod method, Class<T> cls, ObjectMapper mapper) {
        return read(url, method, cls, HttpHeaders.EMPTY, mapper);
    }

    public static int readStatusCodeOnly(String url, HttpMethod method) {
        try {
            URL u = toUrl(url);
            HttpURLConnection con = (HttpURLConnection) u.openConnection();
            con.setRequestMethod(method.toString());
            con.setDoInput(true);
            return con.getResponseCode();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static <T> T readError(String url, HttpMethod method, Class<T> cls, ObjectMapper mapper) {
        try {
            URL u = toUrl(url);
            HttpURLConnection con = (HttpURLConnection) u.openConnection();
            con.setRequestMethod(method.toString());
            con.setDoInput(true);
            try (InputStream in = con.getInputStream()) {
            } catch (IOException e) {
                // do nothing
            }
            try (InputStream err = con.getErrorStream()) {
                return mapper.readValue(err, cls);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
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

    private static URL toUrl(String url) {
        try {
            return new URI(url).toURL();
        } catch (MalformedURLException e) {
            throw new UncheckedIOException(e);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

}
