package org.davidmoten.oa3.codegen.test.main.schema;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.davidmoten.guavamini.Maps;

import jakarta.annotation.Generated;
import jakarta.annotation.Nonnull;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Map;
import java.util.Objects;

import org.davidmoten.oa3.codegen.runtime.Preconditions;
import org.davidmoten.oa3.codegen.test.main.Globals;
import org.davidmoten.oa3.codegen.util.Util;

@JsonInclude(Include.NON_ABSENT)
@JsonAutoDetect(
        fieldVisibility = JsonAutoDetect.Visibility.ANY,
        creatorVisibility = JsonAutoDetect.Visibility.ANY,
        setterVisibility = JsonAutoDetect.Visibility.ANY)
@Generated(value = "com.github.davidmoten:openapi-codegen-runtime:0.2.2-SNAPSHOT")
public final class Rectangle {

    @JsonProperty("minLat")
    private final Latitude minLat;

    @JsonProperty("leftLon")
    private final Longitude leftLon;

    @JsonProperty("heightDegrees")
    private final float heightDegrees;

    @JsonProperty("widthDegrees")
    private final float widthDegrees;

    @JsonCreator
    public Rectangle(
            @Nonnull @JsonProperty("minLat") Latitude minLat,
            @Nonnull @JsonProperty("leftLon") Longitude leftLon,
            @Nonnull @JsonProperty("heightDegrees") float heightDegrees,
            @Nonnull @JsonProperty("widthDegrees") float widthDegrees) {
        if (Globals.config().validateInConstructor().test(Rectangle.class)) {
            Preconditions.checkNotNull(minLat, "minLat");
            Preconditions.checkNotNull(leftLon, "leftLon");
            Preconditions.checkMinimum(heightDegrees, "0", "heightDegrees", true);
            Preconditions.checkMaximum(heightDegrees, "180", "heightDegrees", false);
            Preconditions.checkMinimum(widthDegrees, "0", "widthDegrees", true);
            Preconditions.checkMaximum(widthDegrees, "360", "widthDegrees", false);
        }
        this.minLat = minLat;
        this.leftLon = leftLon;
        this.heightDegrees = heightDegrees;
        this.widthDegrees = widthDegrees;
    }

    public @Nonnull Latitude minLat() {
        return minLat;
    }

    public @Nonnull Longitude leftLon() {
        return leftLon;
    }

    public @Nonnull float heightDegrees() {
        return heightDegrees;
    }

    public @Nonnull float widthDegrees() {
        return widthDegrees;
    }

    Map<String, Object> _internal_properties() {
        return Maps
                .put("minLat", (Object) minLat)
                .put("leftLon", (Object) leftLon)
                .put("heightDegrees", (Object) heightDegrees)
                .put("widthDegrees", (Object) widthDegrees)
                .build();
    }

    public @Nonnull Rectangle withMinLat(@Nonnull Latitude minLat) {
        return new Rectangle(minLat, leftLon, heightDegrees, widthDegrees);
    }

    public @Nonnull Rectangle withLeftLon(@Nonnull Longitude leftLon) {
        return new Rectangle(minLat, leftLon, heightDegrees, widthDegrees);
    }

    public @Nonnull Rectangle withHeightDegrees(@Nonnull float heightDegrees) {
        return new Rectangle(minLat, leftLon, heightDegrees, widthDegrees);
    }

    public @Nonnull Rectangle withWidthDegrees(@Nonnull float widthDegrees) {
        return new Rectangle(minLat, leftLon, heightDegrees, widthDegrees);
    }

    public static @Nonnull Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private Latitude minLat;
        private Longitude leftLon;
        private float heightDegrees;
        private float widthDegrees;

        Builder() {
        }

        public @Nonnull BuilderWithMinLat minLat(@Nonnull Latitude minLat) {
            this.minLat = minLat;
            return new BuilderWithMinLat(this);
        }
    }

    public static final class BuilderWithMinLat {

        private final Builder b;

        BuilderWithMinLat(Builder b) {
            this.b = b;
        }

        public @Nonnull BuilderWithLeftLon leftLon(@Nonnull Longitude leftLon) {
            this.b.leftLon = leftLon;
            return new BuilderWithLeftLon(this.b);
        }
    }

    public static final class BuilderWithLeftLon {

        private final Builder b;

        BuilderWithLeftLon(Builder b) {
            this.b = b;
        }

        public @Nonnull BuilderWithHeightDegrees heightDegrees(@Nonnull float heightDegrees) {
            this.b.heightDegrees = heightDegrees;
            return new BuilderWithHeightDegrees(this.b);
        }
    }

    public static final class BuilderWithHeightDegrees {

        private final Builder b;

        BuilderWithHeightDegrees(Builder b) {
            this.b = b;
        }

        public @Nonnull BuilderWithWidthDegrees widthDegrees(@Nonnull float widthDegrees) {
            this.b.widthDegrees = widthDegrees;
            return new BuilderWithWidthDegrees(this.b);
        }
    }

    public static final class BuilderWithWidthDegrees {

        private final Builder b;

        BuilderWithWidthDegrees(Builder b) {
            this.b = b;
        }

        public @Nonnull Rectangle build() {
            return new Rectangle(this.b.minLat, this.b.leftLon, this.b.heightDegrees, this.b.widthDegrees);
        }
    }

    public static @Nonnull BuilderWithMinLat minLat(@Nonnull Latitude minLat) {
        return builder().minLat(minLat);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Rectangle other = (Rectangle) o;
        return 
            Objects.deepEquals(this.minLat, other.minLat) && 
            Objects.deepEquals(this.leftLon, other.leftLon) && 
            Objects.deepEquals(this.heightDegrees, other.heightDegrees) && 
            Objects.deepEquals(this.widthDegrees, other.widthDegrees);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                minLat, 
                leftLon, 
                heightDegrees, 
                widthDegrees);
    }

    @Override
    public @Nonnull String toString() {
        return Util.toString(Rectangle.class,
                "minLat", minLat,
                "leftLon", leftLon,
                "heightDegrees", heightDegrees,
                "widthDegrees", widthDegrees);
    }
}
