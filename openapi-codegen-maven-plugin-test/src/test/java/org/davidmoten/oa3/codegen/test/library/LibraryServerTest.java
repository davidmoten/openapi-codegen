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

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;

@SpringBootTest(classes = { LibraryApplication.class }, webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
public class LibraryServerTest {

    @LocalServerPort
    int serverPort;

    @Test
    public void testClientServerAllOf() {
        BearerAuthenticator authenticator = () -> "tokenthingy";
        Client client = Client //
                .basePath("http://localhost:" + serverPort) //
                .interceptor(authenticator) //
                .build();
        UsersPage page = client.getUsers(Optional.empty(), Optional.empty());
        assertEquals(20, page.users().value().size());
        assertEquals("User19", page.users().value().get(18).user().firstName());
    }
    
    public static final class Thing {
        
        @JsonAutoDetect(fieldVisibility = Visibility.ANY, creatorVisibility = Visibility.ANY, setterVisibility = Visibility.ANY)
        public static final class Visibility2 {
        }
    }

}
