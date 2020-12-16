package org.davidmoten.openapi.v3;

public final class Definition {
    
    private final String definition;
    private final String modelPackage;
    private final String clientPackage;
    
    public Definition(String definition, String modelPackage, String clientPackage) {
        this.definition = definition;
        this.modelPackage = modelPackage;
        this.clientPackage = clientPackage;
    }
    
    public String definition() {
        return definition;
    }

}
