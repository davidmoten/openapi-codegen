package org.davidmoten.oa3.codegen.http;

import java.util.Optional;

public final class ParameterValue {

    private final String name;
    private final Optional<?> value;
    private final ParameterType type;
    private final Optional<String> contentType;

    public ParameterValue(String name, Optional<?> value, ParameterType type, Optional<String> contentType) {
        this.name = name;
        this.value = value;
        this.type = type;
        this.contentType = contentType;
    }

    public static ParameterValue query(String name, Optional<?> value) {
        return new ParameterValue(name, value, ParameterType.QUERY, Optional.empty());
    }

    public static ParameterValue query(String name, Object value) {
        return query(name, Optional.ofNullable(value));
    }

    public static ParameterValue path(String name, Optional<?> value) {
        return new ParameterValue(name, value, ParameterType.PATH, Optional.empty());
    }

    public static ParameterValue path(String name, Object value) {
        return path(name, Optional.ofNullable(value));
    }

    public static ParameterValue cookie(String name, Optional<?> value) {
        return new ParameterValue(name, value, ParameterType.COOKIE, Optional.empty());
    }

    public static ParameterValue cookie(String name, Object value) {
        return cookie(name, Optional.ofNullable(value));
    }

    public static ParameterValue header(String name, Optional<?> value) {
        return new ParameterValue(name, value, ParameterType.HEADER, Optional.empty());
    }

    public static ParameterValue header(String name, Object value) {
        return header(name, Optional.ofNullable(value));
    }

    public static ParameterValue body(Object value, String contentType) {
        return body(Optional.ofNullable(value), contentType);
    }

    public static ParameterValue body(Optional<?> value, String contentType) {
        return new ParameterValue("requestBody", value, ParameterType.BODY, Optional.of(contentType));
    }

    public String name() {
        return name;
    }

    public Optional<?> value() {
        return value;
    }

    public ParameterType type() {
        return type;
    }

    public Optional<String> contentType() {
        return contentType;
    }

    public static ParameterValue urlEncoded(String name, String value) {
        return new ParameterValue(name, Optional.ofNullable(value), ParameterType.FORM_URLENCODED, Optional.empty());
    }
    
    public static ParameterValue multipart(byte[] value, Optional<String> contentType) {
        return new ParameterValue("", Optional.ofNullable(value), ParameterType.FORM_MULTIPART, contentType);
    }

    public static ParameterValue multipart(String name, byte[] value, Optional<String> contentType) {
        return new ParameterValue(name, Optional.ofNullable(value), ParameterType.FORM_MULTIPART, contentType);
    }
}
