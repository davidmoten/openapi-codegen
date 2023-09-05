package org.davidmoten.oa3.codegen.generator;

public enum ParamStyle {
    FORM("form"), //
    SPACE_DELIMITED("spaceDelimited"), //
    PIPE_DELIMITED("pipeDelimited"), //
    DEEP_OBJECT("deepObject"), //
    LABEL("label"), //
    MATRIX("matrix"), //
    SIMPLE("simple");

    private final String value;

    private ParamStyle(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
