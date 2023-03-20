package org.davidmoten.openapi.v3;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

import com.google.common.io.Files;

@FunctionalInterface
public interface JavaClassWriter {

    void write(Indent indent, Imports imports, PrintWriter p);

    public static void write(File file, String className, ClassType classType, JavaClassWriter writer) {
        String importsToken = "<<IMPORTS>>";
        file.getParentFile().mkdirs();
        Imports imports = new Imports(className);
        StringWriter w = new StringWriter();
        String simpleClassName = Names.simpleClassName(className);
        try (PrintWriter p = new PrintWriter(w)) {
            Indent indent = new Indent();
            p.format("%spackage %s;\n", indent, Names.pkg(className));
            p.format("\n" + importsToken);
            final String declaration;
            if (classType == ClassType.INTERFACE) {
                declaration = "public interface";
            } else if (classType == ClassType.ENUM) {
                declaration = "public enum";
            } else {
                declaration = "public final class";
            }
            p.format("%s %s {\n\n", declaration, simpleClassName);
            writer.write(indent.right(), imports, p);
            p.format("}");
        }
        try {
            String content = w.toString().replace(importsToken, imports.toString());
            System.out.println(content);
            System.out.println("//////////////////////////////////////////");
            Files.write(content.getBytes(StandardCharsets.UTF_8), file);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

}
