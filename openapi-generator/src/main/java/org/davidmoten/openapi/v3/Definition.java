package org.davidmoten.openapi.v3;

import java.io.File;
import java.util.function.Function;

public final class Definition {
    
    private final String definition;
    private final File generatedSourceDirectory;
    private final Packages packages;
    private final Function<String, String> externalRefClassNames;
    
    public Definition(String definition, Packages packages, File generatedSourceDirectory, Function<String, String> externalRefClassNames) {
        this.definition = definition;
        this.packages = packages;
        this.generatedSourceDirectory = generatedSourceDirectory;
        this.externalRefClassNames = externalRefClassNames;
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

}
