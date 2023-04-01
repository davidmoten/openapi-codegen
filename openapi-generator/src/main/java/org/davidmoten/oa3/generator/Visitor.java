package org.davidmoten.oa3.generator;

@FunctionalInterface
public interface Visitor {
    void startSchema(ImmutableList<SchemaWithName> schemaPath);

    default void finishSchema(ImmutableList<SchemaWithName> schemaPath) {
        // do nothing
    }
}