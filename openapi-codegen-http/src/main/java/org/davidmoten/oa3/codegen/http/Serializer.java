package org.davidmoten.oa3.codegen.http;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;

public interface Serializer {

    void serialize(Object o, String contentType, OutputStream out);

    <T> T deserialize(Class<T> cls, String contentType, InputStream in);
    
    /**
     * Returns the properties of object. If has no properties or is not an object
     * type that supports properties then returns an empty map. In the case of JSON
     * the map value is of an imprecise type but will serialize to JSON in an 
     * expected deterministic way using Jackson serialization (i.e the object could be 
     * an annotated Jackson object or an instance of {@link JsonNode}).
     * 
     * @param o object to get properties from, if null then returns an empty map
     * @param contentType content type of the object, cannot be null
     */
    Map<String, Object> properties(Object o, String contentType);
    
    default byte[] serialize(Object o, String contentType) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        serialize(o, contentType, bytes);
        return bytes.toByteArray();
    }
    
}
