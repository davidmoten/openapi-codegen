package org.davidmoten.oa3.codegen.test.paths.path;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.github.davidmoten.guavamini.Maps;

import jakarta.annotation.Generated;

import java.lang.IllegalArgumentException;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Map;
import java.util.Objects;

import org.davidmoten.oa3.codegen.http.HasEncoding;
import org.davidmoten.oa3.codegen.http.HasStringValue;
import org.davidmoten.oa3.codegen.runtime.Preconditions;
import org.davidmoten.oa3.codegen.test.paths.Globals;
import org.davidmoten.oa3.codegen.test.paths.schema.Point;
import org.davidmoten.oa3.codegen.util.Util;
import org.springframework.boot.context.properties.ConstructorBinding;

@JsonInclude(Include.NON_NULL)
@JsonAutoDetect(fieldVisibility = Visibility.ANY, creatorVisibility = Visibility.ANY, setterVisibility = Visibility.ANY)
@Generated(value = "com.github.davidmoten:openapi-codegen-runtime:0.1.2-SNAPSHOT")
public final class UploadPostRequestMultipartFormData {

    @JsonProperty("point")
    private final Point point;

    @JsonProperty("description")
    private final String description;

    @JsonProperty("document")
    private final Document document;

    @JsonCreator
    @ConstructorBinding
    public UploadPostRequestMultipartFormData(
            @JsonProperty("point") Point point,
            @JsonProperty("description") String description,
            @JsonProperty("document") Document document) {
        if (Globals.config().validateInConstructor().test(UploadPostRequestMultipartFormData.class)) {
            Preconditions.checkNotNull(point, "point");
            Preconditions.checkNotNull(description, "description");
            Preconditions.checkNotNull(document, "document");
        }
        this.point = point;
        this.description = description;
        this.document = document;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private Point point;
        private String description;
        private Document document;

        Builder() {
        }

        public BuilderWithPoint point(Point point) {
            this.point = point;
            return new BuilderWithPoint(this);
        }
    }

    public static final class BuilderWithPoint {

        private final Builder b;

        BuilderWithPoint(Builder b) {
            this.b = b;
        }

        public BuilderWithDescription description(String description) {
            this.b.description = description;
            return new BuilderWithDescription(this.b);
        }
    }

    public static final class BuilderWithDescription {

        private final Builder b;

        BuilderWithDescription(Builder b) {
            this.b = b;
        }

        public BuilderWithDocument document(Document document) {
            this.b.document = document;
            return new BuilderWithDocument(this.b);
        }
    }

    public static final class BuilderWithDocument {

        private final Builder b;

        BuilderWithDocument(Builder b) {
            this.b = b;
        }

        public UploadPostRequestMultipartFormData build() {
            return new UploadPostRequestMultipartFormData(this.b.point, this.b.description, this.b.document);
        }
    }

    public static BuilderWithPoint point(Point point) {
        return builder().point(point);
    }

    public Point point() {
        return point;
    }

    public String description() {
        return description;
    }

    public Document document() {
        return document;
    }

    Map<String, Object> _internal_properties() {
        return Maps
                .put("point", (Object) point)
                .put("description", (Object) description)
                .put("document", (Object) document)
                .build();
    }

    public UploadPostRequestMultipartFormData withPoint(Point point) {
        return new UploadPostRequestMultipartFormData(point, description, document);
    }

    public UploadPostRequestMultipartFormData withDescription(String description) {
        return new UploadPostRequestMultipartFormData(point, description, document);
    }

    public UploadPostRequestMultipartFormData withDocument(Document document) {
        return new UploadPostRequestMultipartFormData(point, description, document);
    }

    @JsonInclude(Include.NON_NULL)
    @JsonAutoDetect(fieldVisibility = Visibility.ANY, creatorVisibility = Visibility.ANY, setterVisibility = Visibility.ANY)
    public static final class Document implements HasEncoding {

        @JsonProperty("contentType")
        private final ContentType contentType;

        @JsonProperty("value")
        private final String value;

        @JsonCreator
        private Document(
                @JsonProperty("contentType") ContentType contentType,
                @JsonProperty("value") String value) {
            this.contentType = contentType;
            this.value = value;
        }

        @ConstructorBinding
        public Document(
                ContentType contentType,
                byte[] value) {
            if (Globals.config().validateInConstructor().test(Document.class)) {
                Preconditions.checkNotNull(contentType, "contentType");
                Preconditions.checkNotNull(value, "value");
            }
            this.contentType = contentType;
            this.value = Util.encodeOctets(value);
        }

        public static Builder builder() {
            return new Builder();
        }

        public static final class Builder {

            private ContentType contentType;
            private byte[] value;

            Builder() {
            }

            public BuilderWithContentType contentType(ContentType contentType) {
                this.contentType = contentType;
                return new BuilderWithContentType(this);
            }
        }

        public static final class BuilderWithContentType {

            private final Builder b;

            BuilderWithContentType(Builder b) {
                this.b = b;
            }

            public BuilderWithValue value(byte[] value) {
                this.b.value = value;
                return new BuilderWithValue(this.b);
            }
        }

        public static final class BuilderWithValue {

            private final Builder b;

            BuilderWithValue(Builder b) {
                this.b = b;
            }

            public Document build() {
                return new Document(this.b.contentType, this.b.value);
            }
        }

        public static BuilderWithContentType contentType(ContentType contentType) {
            return builder().contentType(contentType);
        }

        public ContentType contentType() {
            return contentType;
        }

        public byte[] value() {
            return Util.decodeOctets(value);
        }

        Map<String, Object> _internal_properties() {
            return Maps
                    .put("contentType", (Object) contentType)
                    .put("value", (Object) value)
                    .build();
        }

        public Document withContentType(ContentType contentType) {
            return new Document(contentType, value);
        }

        public Document withValue(byte[] value) {
            return new Document(contentType, value);
        }

        public enum ContentType implements HasStringValue {

            APPLICATION_PDF("application/pdf");

            @JsonValue
            private final String value;

            private ContentType(
                    String value) {
                this.value = value;
            }

            public String value() {
                return value;
            }

            Map<String, Object> _internal_properties() {
                return Maps
                        .put("value", (Object) value)
                        .build();
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
                Objects.equals(this.contentType, other.contentType) && 
                Objects.equals(this.value, other.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(contentType, value);
        }

        @Override
        public String toString() {
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
            Objects.equals(this.point, other.point) && 
            Objects.equals(this.description, other.description) && 
            Objects.equals(this.document, other.document);
    }

    @Override
    public int hashCode() {
        return Objects.hash(point, description, document);
    }

    @Override
    public String toString() {
        return Util.toString(UploadPostRequestMultipartFormData.class, "point", point, "description", description, "document", document);
    }
}
