package org.davidmoten.oa3.codegen.test.main.schema;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import jakarta.annotation.Generated;
import jakarta.annotation.Nonnull;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.Objects;

import org.davidmoten.oa3.codegen.runtime.PolymorphicDeserializer;
import org.davidmoten.oa3.codegen.runtime.PolymorphicType;
import org.davidmoten.oa3.codegen.runtime.Preconditions;
import org.davidmoten.oa3.codegen.test.main.Globals;
import org.davidmoten.oa3.codegen.util.Util;

@JsonDeserialize(using = Geometry._Deserializer.class)
@JsonInclude(Include.NON_ABSENT)
@JsonAutoDetect(
        fieldVisibility = JsonAutoDetect.Visibility.ANY,
        creatorVisibility = JsonAutoDetect.Visibility.ANY,
        setterVisibility = JsonAutoDetect.Visibility.ANY)
@Generated(value = "com.github.davidmoten:openapi-codegen-runtime:0.2.2-SNAPSHOT")
public final class Geometry {

    @JsonValue
    private final Object value;

    private Geometry(Object value) {
        this.value = Preconditions.checkNotNull(value, "value");
    }

    private Geometry(Rectangle value) {
        this.value = Preconditions.checkNotNull(value, "value");
    }

    private Geometry(Circle value) {
        this.value = Preconditions.checkNotNull(value, "value");
    }

    public @Nonnull Object value() {
        return value;
    }

    public static @Nonnull Geometry of(@Nonnull Rectangle value) {
        if (Globals.config().validateInConstructor().test(Geometry.class)) {
            Preconditions.checkNotNull(value, "value");
        }
        return new Geometry(value);
    }

    public static @Nonnull Geometry of(@Nonnull Circle value) {
        if (Globals.config().validateInConstructor().test(Geometry.class)) {
            Preconditions.checkNotNull(value, "value");
        }
        return new Geometry(value);
    }

    @SuppressWarnings("serial")
    public static final class _Deserializer extends PolymorphicDeserializer<Geometry> {

        public _Deserializer() {
            super(Globals.config(), PolymorphicType.ONE_OF, Geometry.class, Rectangle.class, Circle.class);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Geometry other = (Geometry) o;
        return 
            Objects.deepEquals(this.value, other.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, value);
    }

    @Override
    public @Nonnull String toString() {
        return Util.toString(Geometry.class, "value", value, "value", value);
    }
}
