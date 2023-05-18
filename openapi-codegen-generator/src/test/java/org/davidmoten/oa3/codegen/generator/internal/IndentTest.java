package org.davidmoten.oa3.codegen.generator.internal;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class IndentTest {

    @Test
    public void testMoreLeftThanRight() {
        assertThrows(RuntimeException.class, () -> {
            Indent indent = new Indent();
            indent.left();
        });
    }

    @Test
    public void testRightThenLeft() {
        Indent indent = new Indent();
        assertTrue(indent.right().left().toString().isEmpty());
    }

}
