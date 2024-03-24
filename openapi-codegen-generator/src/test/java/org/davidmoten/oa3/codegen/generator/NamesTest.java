package org.davidmoten.oa3.codegen.generator;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.ParseOptions;

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
        // must preserve leading underscore so an identifier like _2 does not get
        // trimmed to 2
        assertEquals("_helloThere", Names.underscoreToCamel("_hello_there"));
    }

    @Test
    public void testUpperFirst() {
        assertEquals("Ab", Names.upperFirst("ab"));
        assertEquals("A", Names.upperFirst("a"));
        assertEquals("", Names.upperFirst(""));
    }

    @Test
    public void testToIdentifier() {
        assertEquals("b", Names.toIdentifier("b"));
    }

    @Test
    public void testEnumClassName() {
        assertEquals("B", Names.simpleClassNameFromSimpleName("b"));
    }

    @Test
    public void testUnderscoreToCamel() {
        assertEquals("b", Names.underscoreToCamel("b"));
    }

    @Test
    public void testUnderscoreToCamel2() {
        assertEquals("b", Names.underscoreToCamel("b_"));
    }
    
    @Test
    public void testUnderscoreToCamel3() {
        assertEquals("b", Names.underscoreToCamel("b__"));
    }

//    @Test
    public void testMaxCodePointsOpenApi31() {
        System.setProperty("maxYamlCodePoints", "999999999");
        ParseOptions options = new ParseOptions();
        options.setResolve(true);
        OpenAPIV3Parser parser = new OpenAPIV3Parser();
        parser.readLocation("https://raw.githubusercontent.com/codatio/oas/main/yaml/Codat-Lending.yaml", null, options);
    }
    
}
