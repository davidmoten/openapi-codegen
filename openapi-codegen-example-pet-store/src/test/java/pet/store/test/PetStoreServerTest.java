package pet.store.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;

import pet.store.client.Client;
import pet.store.path.PetsGet200Response;

@SpringBootTest(classes = { PetStoreApplication.class }, webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
public class PetStoreServerTest {

    @LocalServerPort
    int serverPort;

    private String basePath() {
        return "http://localhost:" + serverPort;
    }

    private Client client() {
        return Client.basePath(basePath()).build();
    }

    @Test
    public void testGet() {
        PetsGet200Response r = client().petsGet(Optional.empty(), 10);
        assertEquals(1, r.value().size());
        assertEquals("fido", r.value().get(0).newPet().name());
    }
}