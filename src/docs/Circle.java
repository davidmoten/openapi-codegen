package org.davidmoten.oa3.codegen.test.main.schema;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.davidmoten.guavamini.Maps;

import jakarta.annotation.Generated;

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
@Generated(value = "com.github.davidmoten:openapi-codegen-runtime:0.1.18-SNAPSHOT")
public final class Circle {

    @JsonProperty("lat")
    private final Latitude lat;

    @JsonProperty("lon")
    private final Longitude lon;

    @JsonProperty("radiusNm")
    private final float radiusNm;

    @JsonCreator
    public Circle(
            @JsonProperty("lat") Latitude lat,
            @JsonProperty("lon") Longitude lon,
            @JsonProperty("radiusNm") float radiusNm) {
        if (Globals.config().validateInConstructor().test(Circle.class)) {
            Preconditions.checkNotNull(lat, "lat");
            Preconditions.checkNotNull(lon, "lon");
            Preconditions.checkMinimum(radiusNm, "0", "radiusNm", false);
        }
        this.lat = lat;
        this.lon = lon;
        this.radiusNm = radiusNm;
    }

    public Latitude lat() {
        return lat;
    }

    public Longitude lon() {
        return lon;
    }

    public float radiusNm() {
        return radiusNm;
    }

    Map<String, Object> _internal_properties() {
        return Maps
                .put("lat", (Object) lat)
                .put("lon", (Object) lon)
                .put("radiusNm", (Object) radiusNm)
                .build();
    }

    public Circle withLat(Latitude lat) {
        return new Circle(lat, lon, radiusNm);
    }

    public Circle withLon(Longitude lon) {
        return new Circle(lat, lon, radiusNm);
    }

    public Circle withRadiusNm(float radiusNm) {
        return new Circle(lat, lon, radiusNm);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private Latitude lat;
        private Longitude lon;
        private float radiusNm;

        Builder() {
        }

        public BuilderWithLat lat(Latitude lat) {
            this.lat = lat;
            return new BuilderWithLat(this);
        }
    }

    public static final class BuilderWithLat {

        private final Builder b;

        BuilderWithLat(Builder b) {
            this.b = b;
        }

        public BuilderWithLon lon(Longitude lon) {
            this.b.lon = lon;
            return new BuilderWithLon(this.b);
        }
    }

    public static final class BuilderWithLon {

        private final Builder b;

        BuilderWithLon(Builder b) {
            this.b = b;
        }

        public BuilderWithRadiusNm radiusNm(float radiusNm) {
            this.b.radiusNm = radiusNm;
            return new BuilderWithRadiusNm(this.b);
        }
    }

    public static final class BuilderWithRadiusNm {

        private final Builder b;

        BuilderWithRadiusNm(Builder b) {
            this.b = b;
        }

        public Circle build() {
            return new Circle(this.b.lat, this.b.lon, this.b.radiusNm);
        }
    }

    public static BuilderWithLat lat(Latitude lat) {
        return builder().lat(lat);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Circle other = (Circle) o;
        return 
            Objects.deepEquals(this.lat, other.lat) && 
            Objects.deepEquals(this.lon, other.lon) && 
            Objects.deepEquals(this.radiusNm, other.radiusNm);
    }

    @Override
    public int hashCode() {
        return Objects.hash(lat, lon, radiusNm);
    }

    @Override
    public String toString() {
        return Util.toString(Circle.class, "lat", lat, "lon", lon, "radiusNm", radiusNm);
    }
}
