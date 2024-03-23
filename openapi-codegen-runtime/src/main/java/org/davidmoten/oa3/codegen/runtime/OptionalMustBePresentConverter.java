package org.davidmoten.oa3.codegen.runtime;

import java.util.NoSuchElementException;
import java.util.Optional;

import com.fasterxml.jackson.databind.util.StdConverter;

public final class OptionalMustBePresentConverter<T> extends StdConverter<Optional<T>, Optional<T>> {

    @Override
    public Optional<T> convert(Optional<T> value) {
        System.out.println(value);
        if (!value.isPresent()) {
            throw new NoSuchElementException("optional value must be present (likely because a required readonly property)");
        }
        return value;
    }
}
