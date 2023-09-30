package org.davidmoten.oa3.codegen.generator.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.net.HttpURLConnection;

import org.junit.jupiter.api.Test;

public class ImportsTest {

    @Test
    public void testNoClassWithParentClass() {
        Imports imports = new Imports("Something", x-> false);
        assertEquals("fred.Something", imports.add("fred.Something"));
    }

    @Test
    public void test() {
        Imports imports = new Imports("Something", x -> false);
        assertEquals("Boo", imports.add("fred.Boo"));
        assertEquals("jill.Boo", imports.add("jill.Boo"));
        assertEquals("Boo", imports.add("fred.Boo"));
    }

    @Test
    public void testNameClash() {
        Imports imports = new Imports("fred.Something", x -> false);
        assertEquals("anne.Something", imports.add("anne.Something"));
        assertEquals("Something", imports.add("fred.Something"));
    }

    @Test
    public void testSortedAndGroupedWithNewLineSeparatorBetweenFirstSegmentChanges() {
        Imports imports = new Imports("Something", x -> false);
        imports.add("com.fred.MyClass");
        imports.add("com.andrew.AnotherClass");
        imports.add(Integer.class);
        imports.add(HttpURLConnection.class);
        imports.add(IOException.class);
        assertEquals("import com.andrew.AnotherClass;\n" //
                + "import com.fred.MyClass;\n" //
                + "\n" //
                + "import java.io.IOException;\n" //
                + "import java.lang.Integer;\n" //
                + "import java.net.HttpURLConnection;\n", //
                imports.toString());
    }

}
