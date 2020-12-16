package org.davidmoten.openapi.v3;

import java.io.File;
import java.util.Map;

public final class Definition {
    
    private final String definition;
    private final File generatedSourceDirectory;
    private final Packages packages;
    private final Map<String, Packages> externalPackages;
    
    public Definition(String definition, Packages packages, File generatedSourceDirectory, Map<String, Packages> externalPackages) {
        this.definition = definition;
        this.packages = packages;
        this.generatedSourceDirectory = generatedSourceDirectory;
        this.externalPackages = externalPackages;
    }
    
    public String definition() {
        return definition;
    }

}
