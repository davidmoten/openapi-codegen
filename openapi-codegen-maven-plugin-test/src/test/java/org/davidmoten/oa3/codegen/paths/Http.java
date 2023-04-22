package org.davidmoten.oa3.codegen.paths;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.HttpURLConnection;
import java.net.URL;

import org.springframework.http.HttpMethod;

import com.fasterxml.jackson.databind.ObjectMapper;

public final class Http {

    private Http() {
        // prevent instantiation
    }

    public static <T> T read(String url, HttpMethod method, Class<T> cls, ObjectMapper mapper) {
        try {
            URL u = new URL(url);
            HttpURLConnection con = (HttpURLConnection) u.openConnection();
            con.setRequestMethod(method.toString());
            con.setDoInput(true);
            try (InputStream in = con.getInputStream()) {
                return mapper.readValue(in, cls);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static <T> T readError(String url, HttpMethod method, Class<T> cls, ObjectMapper mapper) {
        try {
            URL u = new URL(url);
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
    
//    private static byte[] read(InputStream in) throws IOException {
//        byte[] buffer = new byte[8192];
//        int n = 0;
//        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
//        while ((n = in.read(buffer))!= -1) {
//            bytes.write(buffer, 0, n);
//        }
//        return bytes.toByteArray();
//    }

}
