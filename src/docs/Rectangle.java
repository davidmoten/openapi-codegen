package org.davidmoten.oa3.codegen.test.main.schema;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.annotation.Generated;

import java.lang.Float;
import java.lang.Override;
import java.util.Objects;

import org.davidmoten.oa3.codegen.runtime.Preconditions;
import org.davidmoten.oa3.codegen.test.main.Globals;
import org.davidmoten.oa3.codegen.util.Util;
import org.springframework.boot.context.properties.ConstructorBinding;

@JsonInclude(Include.NON_NULL)
@JsonAutoDetect(fieldVisibility = Visibility.ANY, creatorVisibility = Visibility.ANY)
@Generated(value = "com.github.davidmoten:openapi-codegen-runtime0.1-alpha-7-SNAPSHOT")
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
    @ConstructorBinding
    public Rectangle(
            @JsonProperty("minLat") Latitude minLat,
            @JsonProperty("leftLon") Longitude leftLon,
            @JsonProperty("heightDegrees") float heightDegrees,
            @JsonProperty("widthDegrees") float widthDegrees) {
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

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private Latitude minLat;
        private Longitude leftLon;
        private Float heightDegrees;
        private Float widthDegrees;

        Builder() {
        }

        public BuilderWithMinLat minLat(Latitude minLat) {
            this.minLat = minLat;
            return new BuilderWithMinLat(this);
        }
    }

    public static final class BuilderWithMinLat {

        private final Builder b;

        BuilderWithMinLat(Builder b) {
            this.b = b;
        }

        public BuilderWithLeftLon leftLon(Longitude leftLon) {
            this.b.leftLon = leftLon;
            return new BuilderWithLeftLon(this.b);
        }
    }

    public static final class BuilderWithLeftLon {

        private final Builder b;

        BuilderWithLeftLon(Builder b) {
            this.b = b;
        }

        public BuilderWithHeightDegrees heightDegrees(float heightDegrees) {
            this.b.heightDegrees = heightDegrees;
            return new BuilderWithHeightDegrees(this.b);
        }
    }

    public static final class BuilderWithHeightDegrees {

        private final Builder b;

        BuilderWithHeightDegrees(Builder b) {
            this.b = b;
        }

        public BuilderWithWidthDegrees widthDegrees(float widthDegrees) {
            this.b.widthDegrees = widthDegrees;
            return new BuilderWithWidthDegrees(this.b);
        }
    }

    public static final class BuilderWithWidthDegrees {

        private final Builder b;

        BuilderWithWidthDegrees(Builder b) {
            this.b = b;
        }

        public Rectangle build() {
            return new Rectangle(this.b.minLat, this.b.leftLon, this.b.heightDegrees, this.b.widthDegrees);
        }
    }

    public Latitude minLat() {
        return minLat;
    }

    public Longitude leftLon() {
        return leftLon;
    }

    public float heightDegrees() {
        return heightDegrees;
    }

    public float widthDegrees() {
        return widthDegrees;
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
            Objects.equals(this.minLat, other.minLat) && 
            Objects.equals(this.leftLon, other.leftLon) && 
            Objects.equals(this.heightDegrees, other.heightDegrees) && 
            Objects.equals(this.widthDegrees, other.widthDegrees);
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
    public String toString() {
        return Util.toString(Rectangle.class,
                "minLat", minLat,
                "leftLon", leftLon,
                "heightDegrees", heightDegrees,
                "widthDegrees", widthDegrees);
    }
}
