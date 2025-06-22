package org.davidmoten.oa3.codegen.generator.internal;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class IndentTest {

    @Test
    public void testMoreLeftThanRight() {
        assertThrows(RuntimeException.class, () -> new Indent().left());
    }

    @Test
    public void testRightThenLeft() {
        Indent indent = new Indent();
        assertTrue(indent.right().left().toString().isEmpty());
    }

}
