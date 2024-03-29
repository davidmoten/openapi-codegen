package org.davidmoten.oa3.codegen.generator.internal;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

class Annotation {

    Optional<String> getString() {
        return Optional.empty();
    }

    public Optional<Boolean> getBool() {
        return Optional.empty();
    }

    public List<String> getRecords() {
        return Collections.emptyList();
    }

    public String getTerm() {
        return "";
    }

}
