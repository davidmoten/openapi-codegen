package org.davidmoten.openapi.v3;

import java.io.File;

public final class Definition {
    
    private final String definition;
    private final String modelPackage;
    private final String clientPackage;
    private final File generatedSourceDirectory;
    
    public Definition(String definition, String modelPackage, String clientPackage, File generatedSourceDirectory) {
        this.definition = definition;
        this.modelPackage = modelPackage;
        this.clientPackage = clientPackage;
        this.generatedSourceDirectory = generatedSourceDirectory;
    }
    
    public String definition() {
        return definition;
    }

}
