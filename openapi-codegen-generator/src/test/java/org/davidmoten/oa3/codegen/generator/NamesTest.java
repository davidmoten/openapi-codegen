package org.davidmoten.oa3.codegen.generator;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

public class NamesTest {

    @Test
    public void testEnumMapResolvesPotentialCollisions() {
        List<String> list = Arrays.asList("a b", "a  b", "a   b");
        Map<String, String> map = Names.getEnumValueToIdentifierMap(list);
        assertEquals("A_B", map.get("a b"));
        assertEquals("A_B_1", map.get("a  b"));
        assertEquals("A_B_2", map.get("a   b"));
    }

    @Test
    public void testEnumMapResolvesRepeated() {
        List<String> list = Arrays.asList("a", "a", "b");
        Map<String, String> map = Names.getEnumValueToIdentifierMap(list);
        assertEquals("A", map.get("a"));
        assertEquals("B", map.get("b"));
        assertEquals(2, map.size());
    }
    
    @Test
    public void testUnderscoresToCamel() {
        assertEquals("helloThere", Names.underscoreToCamel("hello_there"));
        assertEquals("helloThere", Names.underscoreToCamel("hello__there"));
        assertEquals("helloThere", Names.underscoreToCamel("hello_there_"));
        assertEquals("HelloThere", Names.underscoreToCamel("_hello_there"));
    }
    
    @Test
    public void testUpperFirst() {
        assertEquals("Ab", Names.upperFirst("ab"));
        assertEquals("A", Names.upperFirst("a"));
        assertEquals("", Names.upperFirst(""));
    }

}
