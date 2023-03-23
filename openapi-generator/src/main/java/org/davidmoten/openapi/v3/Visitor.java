package org.davidmoten.openapi.v3;

@FunctionalInterface
public interface Visitor {
    void startSchema(ImmutableList<SchemaWithName> schemaPath);

    default void finishSchema(ImmutableList<SchemaWithName> schemaPath) {
        // do nothing
    }
}
