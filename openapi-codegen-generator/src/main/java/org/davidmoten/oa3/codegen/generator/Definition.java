package org.davidmoten.oa3.codegen.generator;

import java.io.File;
import java.util.Optional;
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
    private final boolean mapNumberToBigDecimal;
    private final boolean failOnParseErrors;
    private final Optional<String> generator;
    private final boolean generateService;
    private final boolean applyReadOnly;
    private final boolean applyWriteOnly;
    private final int maxClassNameLength;

    public Definition(String definition, Packages packages, File generatedSourceDirectory,
            Function<String, String> externalRefClassNames, Set<String> includeSchemas, Set<String> excludeSchemas,
            boolean mapIntegerToBigInteger, boolean mapNumberToBigDecimal, boolean failOnParseErrors, 
            Optional<String> generator, boolean generateService, boolean applyReadOnly, boolean applyWriteOnly, int maxClassNameLength) {
        this.definition = definition;
        this.packages = packages;
        this.generatedSourceDirectory = generatedSourceDirectory;
        this.externalRefClassNames = externalRefClassNames;
        this.includeSchemas = includeSchemas;
        this.excludeSchemas = excludeSchemas;
        this.mapIntegerToBigInteger = mapIntegerToBigInteger;
        this.mapNumberToBigDecimal = mapNumberToBigDecimal;
        this.failOnParseErrors = failOnParseErrors;
        this.generator = generator;
        this.generateService = generateService;
        this.applyReadOnly = applyReadOnly;
        this.applyWriteOnly = applyWriteOnly;
        this.maxClassNameLength = maxClassNameLength;
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

    public Optional<String> generator() {
        return generator;
    }

    public boolean mapNumberToBigDecimal() {
        return mapNumberToBigDecimal;
    }
    
    public boolean generateService() {
        return generateService;
    }
    
    public boolean applyReadOnly() {
        return applyReadOnly;
    }

    public boolean applyWriteOnly() {
        return applyWriteOnly;
    }
    
    public int maxClassNameLength() {
        return maxClassNameLength;
    }
}
