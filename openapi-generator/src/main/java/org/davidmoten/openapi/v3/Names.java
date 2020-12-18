package org.davidmoten.openapi.v3;

import java.io.File;
import java.util.Set;

import com.github.davidmoten.guavamini.Sets;

public final class Names {

    private static final Set<String> javaReservedWords = Sets.newHashSet("abstract", "assert", "boolean", "break",
            "byte", "case", "catch", "char", "class", "const", "continue", "default", "do", "double", "else", "extends",
            "false", "final", "finally", "float", "for", "goto", "if", "implements", "import", "instanceof", "int",
            "interface", "long", "native", "new", "null", "package", "private", "protected", "public", "return",
            "short", "static", "strictfp", "super", "switch", "synchronized", "this", "throw", "throws", "transient",
            "true", "try", "void", "volatile", "while", "var");

    private final Definition definition;

    public Names(Definition definition) {
        this.definition = definition;
    }

    public String schemaNameToClassName(String schemaName) {
        return definition.packages().modelPackage() + "." + schemaNameToSimpleClassName(schemaName);
    }

    public String schemaNameToSimpleClassName(String schemaName) {
        return upperFirst(toIdentifier(schemaName));
    }

    public File schemaNameToJavaFile(String schemaName) {
        return new File(definition.generatedSourceDirectory(),
                schemaNameToClassName(schemaName).replace(".", File.separator) + ".java");
    }

    public static String simpleClassName(String className) {
        return getLastItemInDotDelimitedString(className);
    }
    
    public static String pkg(String className) {
        return className.substring(0, className.lastIndexOf("."));
    }

    public static String toIdentifier(String s) {
        if (javaReservedWords.contains(s.toLowerCase())) {
            return s.toLowerCase() + "_";
        } else if (s.toUpperCase().equals(s)) {
            return s;
        } else {
            return lowerFirst(s);
        }
    }
    
    public static String propertyNameToClassSimpleName(String propertyName) {
        return upperFirst(toIdentifier(propertyName));
    }

    public static String upperFirst(String name) {
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }

    private static String lowerFirst(String name) {
        return name.substring(0, 1).toLowerCase() + name.substring(1);
    }

    private static String getLastItemInDotDelimitedString(String name) {
        int i = name.lastIndexOf(".");
        if (i == -1) {
            return name;
        } else {
            return name.substring(i + 1);
        }
    }

    public String clientClassName() {
        return definition.packages().clientPackage() + ".Client";
    }

    public File clientClassJavaFile() {
        return new File(definition.generatedSourceDirectory(),
                clientClassName().replace(".", File.separator) + ".java");
    }

    public static String propertyNameToFieldName(String propertyName) {
        return lowerFirst(toIdentifier(propertyName));
    }

}
