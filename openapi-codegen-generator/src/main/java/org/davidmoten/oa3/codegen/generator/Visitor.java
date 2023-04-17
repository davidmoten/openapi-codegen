package org.davidmoten.oa3.codegen.generator;

import org.davidmoten.oa3.codegen.generator.internal.ImmutableList;

@FunctionalInterface
interface Visitor {
    void startSchema(SchemaCategory schema, ImmutableList<SchemaWithName> schemaPath);

    default void finishSchema(SchemaCategory schema, ImmutableList<SchemaWithName> schemaPath) {
        // do nothing
    }
}
