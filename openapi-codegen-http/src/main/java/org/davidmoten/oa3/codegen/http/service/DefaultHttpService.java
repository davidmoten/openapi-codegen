package org.davidmoten.oa3.codegen.http.service;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import org.davidmoten.oa3.codegen.http.HttpMethod;
import org.davidmoten.oa3.codegen.http.service.internal.DefaultHttpConnection;

public final class DefaultHttpService implements HttpService {
    
    public static final DefaultHttpService INSTANCE = new DefaultHttpService();

    @Override
    public HttpConnection connection(String url, HttpMethod method, Option... options) throws IOException {
        HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
        HttpMethod requestMethod;
        if (contains(options, StandardOption.PATCH_USING_HEADER) && method.equals(HttpMethod.PATCH)) {
            con.setRequestProperty("X-HTTP-Method-Override", HttpMethod.PATCH.name());
            requestMethod = HttpMethod.POST;
        } else {
            requestMethod = method;
        }
        con.setRequestMethod(requestMethod.name());
        return new DefaultHttpConnection(con);
    }
    
    private static boolean contains(Option[] options, Option o) {
        for (Option option: options) {
            if (option.equals(o)) {
                return true;
            }
        }
        return false;
    }

}
