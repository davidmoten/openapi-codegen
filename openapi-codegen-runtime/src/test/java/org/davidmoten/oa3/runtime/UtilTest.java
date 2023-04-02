package org.davidmoten.oa3.runtime;

import static org.junit.Assert.assertEquals;

import java.nio.charset.StandardCharsets;

import org.davidmoten.oa3.generator.runtime.internal.Util;
import org.junit.Test;

public class UtilTest {

    @Test
    public void test() {
        String a = "abc";
        String b = Util.encodeOctets(a.getBytes(StandardCharsets.UTF_8));
        assertEquals("616263", b);
        assertEquals("abc", new String(Util.decodeOctets(b), StandardCharsets.UTF_8));
    }
}
