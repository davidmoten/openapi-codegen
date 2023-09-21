package org.davidmoten.oa3.codegen.runtime;

import static org.davidmoten.oa3.codegen.runtime.AnyOfSerializer.merge;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class AnyOfSerializerTest {

    private static final ObjectMapper m = new ObjectMapper();

    @Test
    public void testMergeSameInteger() {
        JsonNode n = node("1");
        assertEquals(n, merge(n, n));
    }

    @Test
    public void testMergeDifferentInteger() {
        JsonNode n = node("1");
        iae(() -> merge(n, node("2")));
    }

    @Test
    public void testMergeIntegerAndStringThrows() {
        JsonNode n = node("1");
        iae(() -> merge(n, node("\"1\"")));
    }

    @Test
    public void testMergeIntegerAndArrayThrows() {
        JsonNode n = node("1");
        iae(() -> merge(n, node("[]")));
    }

    @Test
    public void testMergeArray() {
        JsonNode n = node("[1,2,3]");
        assertEquals(n, merge(n, n));
    }

    @Test
    public void testMergeArrayDifferentLengths() {
        JsonNode n = node("[1,2,3]");
        iae(() -> merge(n, node("[1,2,3,4]")));
    }

    @Test
    public void testMergeArrayDifferentValues() {
        JsonNode n = node("[1,2,3]");
        iae(() -> merge(n, node("[1,2,4]")));
    }

    @Test
    public void testMergeObjects() {
        JsonNode a = node("{\"a\":1, \"b\":2}");
        JsonNode b = node("{\"c\":3, \"d\":4}");
        JsonNode c = node("{\"a\":1, \"b\":2, \"c\":3, \"d\":4}");
        assertEquals(c, merge(a, b));
    }

    @Test
    public void testMergeObjectsCommonField() {
        JsonNode a = node("{\"a\":1, \"b\":2}");
        JsonNode b = node("{\"b\":2, \"c\":3}");
        JsonNode c = node("{\"a\":1, \"b\":2, \"c\":3}");
        assertEquals(c, merge(a, b));
    }

    @Test
    public void testMergeObjectsCommonFieldDiffers() {
        JsonNode a = node("{\"a\":1, \"b\":2}");
        JsonNode b = node("{\"b\":4, \"c\":3}");
        iae(() -> merge(a, b));
    }
    
    @Test
    public void testMergeObjectsDeepCommonFieldDiffers() {
        JsonNode a = node("{\"a\":1, \"b\":{\"d\":5}}");
        JsonNode b = node("{\"b\":{\"d\":6}, \"c\":3}");
        iae(() -> merge(a, b));
    }

    private static void iae(Executable exec) {
        assertThrows(IllegalArgumentException.class, exec);
    }

    private static JsonNode node(String json) {
        try {
            return m.readTree(json);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

}
