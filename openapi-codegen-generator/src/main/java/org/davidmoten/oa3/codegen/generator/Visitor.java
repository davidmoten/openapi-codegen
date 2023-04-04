package org.davidmoten.oa3.codegen.generator;

import org.davidmoten.oa3.codegen.generator.internal.ImmutableList;

@FunctionalInterface
interface Visitor {
    void startSchema(ImmutableList<SchemaWithName> schemaPath);

    default void finishSchema(ImmutableList<SchemaWithName> schemaPath) {
        // do nothing
    }
}
