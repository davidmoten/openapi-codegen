package pet.store.client;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.davidmoten.oa3.codegen.spring.runtime.ServiceException;

import pet.store.path.PetsGet200Response;
import pet.store.schema.NewPet;
import pet.store.schema.Pet;

// Generated
public class Api {

    private final String basePath;

    public Api(String basePath) {
        this.basePath = basePath;
    }

    public PetsGet200Response petsGet(Optional<List<String>> tags, int limit) throws ServiceException {
        String uri = basePath + "/pets" + "?" + param("tags", tags) + "&" + param("limit", limit);
        return null;
    }

    private String param(String key, List<?> values) {
        return urlEncode(key) + "="
                + values.stream().map(x -> urlEncode(toString(x))).collect(Collectors.joining(","));
    }

    private String param(String key, Object value) {
        return urlEncode(key) + "=" + toString(value);
    }

    private static String urlEncode(String s) {
        try {
            return URLEncoder.encode(s, "UTF_8").replaceAll("\\+", "%20");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    private static String toString(Object value) {
        return value == null ? "" : value.toString();
    }

    public Pet petsPost(NewPet requestBody) throws ServiceException {
        return null;
    }

    public Pet petsIdGet(long id) throws ServiceException {
        return null;
    }

    public void petsIdDelete(long id) throws ServiceException {
    }

}
