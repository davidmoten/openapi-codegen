package org.davidmoten.oa3.codegen.util;


import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;
public class UtilTest {

    @Test
    public void test() {
        String a = "abc";
        String b = Util.encodeOctets(a.getBytes(StandardCharsets.UTF_8));
        assertEquals("616263", b);
        assertEquals("abc", new String(Util.decodeOctets(b), StandardCharsets.UTF_8));
    }

    @Test
    public void testToString() {
        assertEquals("Number[lat=1, lon=2]", Util.toString(Number.class, "lat", 1, "lon", 2));
    }
}