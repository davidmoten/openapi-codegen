package org.davidmoten.oa3.codegen.library;

import java.util.Optional;

import org.davidmoten.oa3.codegen.http.DefaultSerializer;
import org.davidmoten.oa3.codegen.library.client.Service;
import org.davidmoten.oa3.codegen.library.schema.UsersPage;
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
    public void test() {
        Service client = new Service(new DefaultSerializer(Globals.config().mapper()),
                "http://localhost:" + serverPort);
        UsersPage page = client.userGet(Optional.empty(), Optional.empty());
        System.out.println(page);
        while (page.continuationToken().isPresent()) {
            page = client.userGet(Optional.empty(), Optional.of(page.continuationToken().get().value()));
            System.out.println(page);
        }
    }

}
