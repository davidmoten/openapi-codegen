package org.davidmoten.oa3.generator;

import io.swagger.v3.oas.models.media.Schema;

final class SchemaWithName {
        final String name;
        final Schema<?> schema;

        SchemaWithName(String name, Schema<?> schema) {
            this.name = name;
            this.schema = schema;
        }

        @Override
        public String toString() {
            final String s;
            if (schema.get$ref() != null) {
                s = schema.get$ref();
            } else {
                s = schema.getClass().getSimpleName();
            }
            return "(" + name + ": " + s + ")";
        }

    }