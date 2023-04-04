package org.davidmoten.oa3.codegen.generator;

import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

public final class Definition {

    public static boolean MAP_INTEGER_TO_BIG_INTEGER = false;

    private final String definition;
    private final File generatedSourceDirectory;
    private final Packages packages;
    private final Function<String, String> externalRefClassNames;
    private final Set<String> includeSchemas;
    private final Set<String> excludeSchemas;

    public Definition(String definition, Packages packages, File generatedSourceDirectory,
            Function<String, String> externalRefClassNames, Set<String> includeSchemas, Set<String> excludeSchemas) {
        this.definition = definition;
        this.packages = packages;
        this.generatedSourceDirectory = generatedSourceDirectory;
        this.externalRefClassNames = externalRefClassNames;
        this.includeSchemas = includeSchemas;
        this.excludeSchemas = excludeSchemas;
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

}
