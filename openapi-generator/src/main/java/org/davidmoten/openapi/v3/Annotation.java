package org.davidmoten.openapi.v3;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class Annotation {

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
