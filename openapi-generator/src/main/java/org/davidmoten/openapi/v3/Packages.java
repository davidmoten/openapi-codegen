package org.davidmoten.openapi.v3;

public final class Packages {

    private final String basePackage;

    public Packages(String basePackage) {
        this.basePackage = basePackage;
    }
    
    public String basePackage() {
        return basePackage;
    }
    
}
