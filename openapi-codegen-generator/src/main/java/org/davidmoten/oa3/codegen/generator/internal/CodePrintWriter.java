package org.davidmoten.oa3.codegen.generator.internal;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

public final class CodePrintWriter extends PrintWriter {

    private ByteArrayOutputStream bytes;
    private final Indent indent;

    public CodePrintWriter(OutputStream out) {
        super(new OutputStreamWriter(out, StandardCharsets.UTF_8));
        this.indent = new Indent();
    }

    private void setBytes(ByteArrayOutputStream bytes) {
        this.bytes = bytes;
    }

    public Indent indent() {
        return indent;
    }

    public CodePrintWriter newLine() {
        format("\n");
        return this;
    }

    public void line(String format, Object... args) {
        Object[] args2 = new Object[args.length + 2];
        args2[0] = indent;
        for (int i = 0; i < args.length; i++) {
            args2[i + 1] = args[i];
        }
        format("%s" + format + "\n", args2);
    }

    public CodePrintWriter left() {
        indent.left();
        return this;
    }

    public CodePrintWriter right() {
        indent.right();
        return this;
    }

    public static CodePrintWriter create() {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        CodePrintWriter p = new CodePrintWriter(bytes);
        p.setBytes(bytes);
        return p;
    }

    public String text() {
        this.flush();
        try {
            return bytes.toString("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public void closeParen() {
        indent.left();
        format("%s}\n", indent);
    }

}
