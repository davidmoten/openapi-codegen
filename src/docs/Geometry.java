package org.davidmoten.oa3.codegen.test.main.schema;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import jakarta.annotation.Generated;

import java.lang.Object;
import java.lang.Override;
import java.lang.SuppressWarnings;
import java.util.Objects;

import org.davidmoten.oa3.codegen.runtime.PolymorphicDeserializer;
import org.davidmoten.oa3.codegen.runtime.PolymorphicType;
import org.davidmoten.oa3.codegen.runtime.Preconditions;
import org.davidmoten.oa3.codegen.test.main.Globals;
import org.davidmoten.oa3.codegen.util.Util;

@JsonDeserialize(using = Geometry.Deserializer.class)
@JsonInclude(Include.NON_NULL)
@JsonAutoDetect(fieldVisibility = Visibility.ANY, creatorVisibility = Visibility.ANY, setterVisibility = Visibility.ANY)
@Generated(value = "com.github.davidmoten:openapi-codegen-runtime:0.1.6")
public final class Geometry {

    @JsonValue
    private final Object value;

    @JsonCreator
    private Geometry(Object value) {
        this.value = Preconditions.checkNotNull(value, "value");
    }

    public Geometry(Rectangle value) {
        this.value = Preconditions.checkNotNull(value, "value");
    }

    public Geometry(Circle value) {
        this.value = Preconditions.checkNotNull(value, "value");
    }
    public static Geometry of(Rectangle value) {
        return new Geometry(value);
    }
    public static Geometry of(Circle value) {
        return new Geometry(value);
    }

    public Object value() {
        return value;
    }

    @SuppressWarnings("serial")
    public static final class Deserializer extends PolymorphicDeserializer<Geometry> {

        public Deserializer() {
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
            Objects.equals(this.value, other.value) && 
            Objects.equals(this.value, other.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, value);
    }

    @Override
    public String toString() {
        return Util.toString(Geometry.class, "value", value, "value", value);
    }
}
