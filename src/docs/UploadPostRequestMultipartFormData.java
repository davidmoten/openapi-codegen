package org.davidmoten.oa3.codegen.test.paths.path;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.github.davidmoten.guavamini.Maps;

import jakarta.annotation.Generated;
import jakarta.annotation.Nonnull;

import java.lang.IllegalArgumentException;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Map;
import java.util.Objects;

import org.davidmoten.oa3.codegen.http.HasEncoding;
import org.davidmoten.oa3.codegen.http.HasStringValue;
import org.davidmoten.oa3.codegen.runtime.OctetsDeserializer;
import org.davidmoten.oa3.codegen.runtime.OctetsSerializer;
import org.davidmoten.oa3.codegen.runtime.Preconditions;
import org.davidmoten.oa3.codegen.test.paths.Globals;
import org.davidmoten.oa3.codegen.test.paths.schema.Point;
import org.davidmoten.oa3.codegen.util.Util;

@JsonInclude(Include.NON_ABSENT)
@JsonAutoDetect(
        fieldVisibility = JsonAutoDetect.Visibility.ANY,
        creatorVisibility = JsonAutoDetect.Visibility.ANY,
        setterVisibility = JsonAutoDetect.Visibility.ANY)
@Generated(value = "com.github.davidmoten:openapi-codegen-runtime:0.2.2-SNAPSHOT")
public final class UploadPostRequestMultipartFormData {

    @JsonProperty("point")
    private final Point point;

    @JsonProperty("description")
    private final String description;

    @JsonProperty("document")
    private final Document document;

    @JsonCreator
    public UploadPostRequestMultipartFormData(
            @Nonnull @JsonProperty("point") Point point,
            @Nonnull @JsonProperty("description") String description,
            @Nonnull @JsonProperty("document") Document document) {
        if (Globals.config().validateInConstructor().test(UploadPostRequestMultipartFormData.class)) {
            Preconditions.checkNotNull(point, "point");
            Preconditions.checkNotNull(description, "description");
            Preconditions.checkNotNull(document, "document");
        }
        this.point = point;
        this.description = description;
        this.document = document;
    }

    public @Nonnull Point point() {
        return point;
    }

    public @Nonnull String description() {
        return description;
    }

    public @Nonnull Document document() {
        return document;
    }

    Map<String, Object> _internal_properties() {
        return Maps
                .put("point", (Object) point)
                .put("description", (Object) description)
                .put("document", (Object) document)
                .build();
    }

    public @Nonnull UploadPostRequestMultipartFormData withPoint(@Nonnull Point point) {
        return new UploadPostRequestMultipartFormData(point, description, document);
    }

    public @Nonnull UploadPostRequestMultipartFormData withDescription(@Nonnull String description) {
        return new UploadPostRequestMultipartFormData(point, description, document);
    }

    public @Nonnull UploadPostRequestMultipartFormData withDocument(@Nonnull Document document) {
        return new UploadPostRequestMultipartFormData(point, description, document);
    }

    public static @Nonnull Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private Point point;
        private String description;
        private Document document;

        Builder() {
        }

        public @Nonnull BuilderWithPoint point(@Nonnull Point point) {
            this.point = point;
            return new BuilderWithPoint(this);
        }
    }

    public static final class BuilderWithPoint {

        private final Builder b;

        BuilderWithPoint(Builder b) {
            this.b = b;
        }

        public @Nonnull BuilderWithDescription description(@Nonnull String description) {
            this.b.description = description;
            return new BuilderWithDescription(this.b);
        }
    }

    public static final class BuilderWithDescription {

        private final Builder b;

        BuilderWithDescription(Builder b) {
            this.b = b;
        }

        public @Nonnull BuilderWithDocument document(@Nonnull Document document) {
            this.b.document = document;
            return new BuilderWithDocument(this.b);
        }
    }

    public static final class BuilderWithDocument {

        private final Builder b;

        BuilderWithDocument(Builder b) {
            this.b = b;
        }

        public @Nonnull UploadPostRequestMultipartFormData build() {
            return new UploadPostRequestMultipartFormData(this.b.point, this.b.description, this.b.document);
        }
    }

    public static @Nonnull BuilderWithPoint point(@Nonnull Point point) {
        return builder().point(point);
    }

    @JsonInclude(Include.NON_ABSENT)
    @JsonAutoDetect(
            fieldVisibility = JsonAutoDetect.Visibility.ANY,
            creatorVisibility = JsonAutoDetect.Visibility.ANY,
            setterVisibility = JsonAutoDetect.Visibility.ANY)
    public static final class Document implements HasEncoding {

        @JsonProperty("contentType")
        private final Document.ContentType contentType;

        @JsonProperty("value")
        @JsonSerialize(using = OctetsSerializer.class)
        private final byte[] value;

        @JsonCreator
        public Document(
                @Nonnull @JsonProperty("contentType") Document.ContentType contentType,
                @Nonnull @JsonProperty("value") @JsonDeserialize(using = OctetsDeserializer.class) byte[] value) {
            if (Globals.config().validateInConstructor().test(Document.class)) {
                Preconditions.checkNotNull(contentType, "contentType");
                Preconditions.checkNotNull(value, "value");
            }
            this.contentType = contentType;
            this.value = value;
        }

        public @Nonnull Document.ContentType contentType() {
            return contentType;
        }

        public @Nonnull byte[] value() {
            return value;
        }

        Map<String, Object> _internal_properties() {
            return Maps
                    .put("contentType", (Object) contentType)
                    .put("value", (Object) value)
                    .build();
        }

        public @Nonnull Document withContentType(@Nonnull Document.ContentType contentType) {
            return new Document(contentType, value);
        }

        public @Nonnull Document withValue(@Nonnull byte[] value) {
            return new Document(contentType, value);
        }

        public static @Nonnull Builder builder() {
            return new Builder();
        }

        public static final class Builder {

            private Document.ContentType contentType;
            private byte[] value;

            Builder() {
            }

            public @Nonnull BuilderWithContentType contentType(@Nonnull Document.ContentType contentType) {
                this.contentType = contentType;
                return new BuilderWithContentType(this);
            }
        }

        public static final class BuilderWithContentType {

            private final Builder b;

            BuilderWithContentType(Builder b) {
                this.b = b;
            }

            public @Nonnull BuilderWithValue value(@Nonnull byte[] value) {
                this.b.value = value;
                return new BuilderWithValue(this.b);
            }
        }

        public static final class BuilderWithValue {

            private final Builder b;

            BuilderWithValue(Builder b) {
                this.b = b;
            }

            public @Nonnull Document build() {
                return new Document(this.b.contentType, this.b.value);
            }
        }

        public static @Nonnull BuilderWithContentType contentType(@Nonnull Document.ContentType contentType) {
            return builder().contentType(contentType);
        }

        public enum ContentType implements HasStringValue {

            APPLICATION_PDF("application/pdf");

            @JsonValue
            private final String value;

            ContentType(
                    @Nonnull String value) {
                if (Globals.config().validateInConstructor().test(ContentType.class)) {
                    Preconditions.checkNotNull(value, "value");
                }
                this.value = value;
            }

            public @Nonnull String value() {
                return value;
            }

            @JsonCreator
            public static ContentType fromValue(Object value) {
                for (ContentType x: ContentType.values()) {
                    if (Objects.equals(value, x.value)) {
                        return x;
                    }
                }
                throw new IllegalArgumentException("unexpected enum value: '" + value + "'");
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
            Document other = (Document) o;
            return 
                Objects.deepEquals(this.contentType, other.contentType) && 
                Objects.deepEquals(this.value, other.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(contentType, value);
        }

        @Override
        public @Nonnull String toString() {
            return Util.toString(Document.class, "contentType", contentType, "value", value);
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
        UploadPostRequestMultipartFormData other = (UploadPostRequestMultipartFormData) o;
        return 
            Objects.deepEquals(this.point, other.point) && 
            Objects.deepEquals(this.description, other.description) && 
            Objects.deepEquals(this.document, other.document);
    }

    @Override
    public int hashCode() {
        return Objects.hash(point, description, document);
    }

    @Override
    public @Nonnull String toString() {
        return Util.toString(UploadPostRequestMultipartFormData.class, "point", point, "description", description, "document", document);
    }
}
