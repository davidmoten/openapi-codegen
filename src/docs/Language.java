package org.davidmoten.oa3.codegen.test.library.schema;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import jakarta.annotation.Generated;

import java.lang.IllegalArgumentException;
import java.lang.Object;
import java.lang.String;
import java.util.Objects;

import org.davidmoten.oa3.codegen.runtime.Preconditions;
import org.davidmoten.oa3.codegen.test.library.Globals;

@Generated(value = "com.github.davidmoten:openapi-codegen-runtime:0.1.18-SNAPSHOT")
public enum Language {

    CHINESE("Chinese"),
    DANISH("Danish"),
    DUTCH("Dutch"),
    ENGLISH("English"),
    ESPERANTO("Esperanto"),
    FINNISH("Finnish"),
    FRENCH("French"),
    GERMAN("German"),
    GREEK("Greek"),
    HUNGARIAN("Hungarian"),
    ITALIAN("Italian"),
    LATIN("Latin"),
    PORTUGUESE("Portuguese"),
    SPANISH("Spanish"),
    SWEDISH("Swedish"),
    TAGALOG("Tagalog"),
    OTHER("Other");

    @JsonValue
    private final String value;

    Language(
            String value) {
        if (Globals.config().validateInConstructor().test(Language.class)) {
            Preconditions.checkNotNull(value, "value");
        }
        this.value = value;
    }

    public String value() {
        return value;
    }

    @JsonCreator
    public static Language fromValue(Object value) {
        for (Language x: Language.values()) {
            if (Objects.equals(value, x.value)) {
                return x;
            }
        }
        throw new IllegalArgumentException("unexpected enum value: '" + value + "'");
    }
}
