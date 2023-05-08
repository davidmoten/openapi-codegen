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
    private final Imports imports;

    public CodePrintWriter(OutputStream out, String fullClassName) {
        this(out, new Imports(fullClassName), new Indent());
    }
    
    public CodePrintWriter(OutputStream out, Imports imports, Indent indent) {
        super(new OutputStreamWriter(out, StandardCharsets.UTF_8));
        this.indent = indent;
        this.imports = imports;
    }
    
    public static CodePrintWriter create(String fullClassName) {
        return create(new Imports(fullClassName), new Indent());
    }
    
    public static CodePrintWriter create(CodePrintWriter w) {
        return create(w.imports(), w.indent());
    }
    
    private static CodePrintWriter create(Imports imp, Indent indent) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        CodePrintWriter p = new CodePrintWriter(bytes, imp, indent);
        p.setBytes(bytes);
        return p;
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

    public void println() {
        newLine();
    }

    public void line(String format, Object... args) {
        Object[] args2 = new Object[args.length + 2];
        args2[0] = indent;
        for (int i = 0; i < args.length; i++) {
            Object v = args[i];
            if (v instanceof Class) {
                v = imports.add((Class<?>) v);
            }
            args2[i + 1] = v;
        }
        format("%s" + format + "\n", args2);
        if (format.endsWith("{")) {
            right();
        }
    }

    public CodePrintWriter left() {
        indent.left();
        return this;
    }

    public CodePrintWriter right() {
        indent.right();
        return this;
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

    public Imports imports() {
        return imports;
    }
    
    public String add(String fullClassName) {
        return imports.add(fullClassName);
    }
    
    public String add(Class<?> cls) {
        return imports.add(cls);
    }

    public String fullClassName() {
        return imports.fullClassName();
    }

}
