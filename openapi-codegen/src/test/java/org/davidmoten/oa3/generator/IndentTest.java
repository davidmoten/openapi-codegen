package org.davidmoten.oa3.generator;

import static org.junit.Assert.assertTrue;

import org.davidmoten.oa3.generator.Indent;
import org.junit.Test;

public class IndentTest {

    @Test(expected = RuntimeException.class)
    public void testMoreLeftThanRight() {
        Indent indent = new Indent();
        indent.left();
    }

    @Test
    public void testRightThenLeft() {
        Indent indent = new Indent();
        assertTrue(indent.right().left().toString().isEmpty());
    }

}
