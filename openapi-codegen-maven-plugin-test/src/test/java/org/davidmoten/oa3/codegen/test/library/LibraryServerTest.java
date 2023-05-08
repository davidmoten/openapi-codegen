package org.davidmoten.oa3.codegen.test.library;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;

import org.davidmoten.oa3.codegen.http.BearerAuthenticator;
import org.davidmoten.oa3.codegen.test.library.client.Client;
import org.davidmoten.oa3.codegen.test.library.schema.UsersPage;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;

@SpringBootTest(classes = { LibraryApplication.class }, webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
public class LibraryServerTest {

    @LocalServerPort
    int serverPort;

    @Test
    public void testClientServerAllOf() {
        Client client = Client //
                .basePath("http://localhost:" + serverPort) //
                .interceptor(new Authenticator()) //
                .build();
        UsersPage page = client.userGet(Optional.empty(), Optional.empty());
        assertEquals(20, page.users().value().size());
        assertEquals("User19", page.users().value().get(18).user().firstName());
    }

    public static final class Authenticator implements BearerAuthenticator {

        @Override
        public String token() {
            return "tokenthingy";
        }

    }

}