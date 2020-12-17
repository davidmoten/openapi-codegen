package org.davidmoten.openapi.v3;

import java.io.PrintWriter;

@FunctionalInterface
public interface JavaClassWriter {

    void write(Indent indent, Imports imports, PrintWriter p);
    
}
