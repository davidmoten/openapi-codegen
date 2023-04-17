package org.davidmoten.oa3.codegen.generator;

import java.io.File;
import java.util.Set;
import java.util.function.Function;

public final class Definition {

    private final String definition;
    private final File generatedSourceDirectory;
    private final Packages packages;
    private final Function<String, String> externalRefClassNames;
    private final Set<String> includeSchemas;
    private final Set<String> excludeSchemas;
    private final boolean mapIntegerToBigInteger;
    private final boolean failOnParseErrors;

    public Definition(String definition, Packages packages, File generatedSourceDirectory,
            Function<String, String> externalRefClassNames, Set<String> includeSchemas, Set<String> excludeSchemas,
            boolean mapIntegerToBigInteger, boolean failOnParseErrors) {
        this.definition = definition;
        this.packages = packages;
        this.generatedSourceDirectory = generatedSourceDirectory;
        this.externalRefClassNames = externalRefClassNames;
        this.includeSchemas = includeSchemas;
        this.excludeSchemas = excludeSchemas;
        this.mapIntegerToBigInteger = mapIntegerToBigInteger;
        this.failOnParseErrors = failOnParseErrors;
    }

    public String definition() {
        return definition;
    }

    public Packages packages() {
        return packages;
    }

    public File generatedSourceDirectory() {
        return generatedSourceDirectory;
    }

    public String externalRefClassName(String ref) {
        return externalRefClassNames.apply(ref);
    }
    
    public Set<String> includeSchemas() {
        return includeSchemas;
    }
    
    public Set<String> excludeSchemas() {
        return excludeSchemas;
    }
    
    public boolean mapIntegerToBigInteger() {
        return mapIntegerToBigInteger;
    }

    public boolean failOnParseErrors() {
        return failOnParseErrors;
    }

}
