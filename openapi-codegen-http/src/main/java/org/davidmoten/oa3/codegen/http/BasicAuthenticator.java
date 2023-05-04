package org.davidmoten.oa3.codegen.http;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public interface BasicAuthenticator extends Interceptor {

    String username();

    String password();

    default RequestBase intercept(RequestBase r) {
        Headers h = new Headers(r.headers());
        String encoded = Base64.getEncoder()
                .encodeToString((username() + ":" + password()).getBytes(StandardCharsets.UTF_8));
        h.put("Authorization", "Basic " + encoded);
        return new RequestBase(r.method(), r.url(), h);
    }

}
