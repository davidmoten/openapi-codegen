package org.davidmoten.openapi.v3;

public final class Packages {

    private final String modelPackage;
    private final String clientPackage;

    public Packages(String modelPackage, String clientPackage) {
        this.modelPackage = modelPackage;
        this.clientPackage = clientPackage;
    }
    
    public String modelPackage() {
        return modelPackage;
    }
    
    public String clientPackage() {
        return clientPackage;
    }

}
