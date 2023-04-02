package org.davidmoten.oa3.codegen.generator;

public final class Packages {

    private final String basePackage;

    public Packages(String basePackage) {
        this.basePackage = basePackage;
    }
    
    public String basePackage() {
        return basePackage;
    }
    
}
