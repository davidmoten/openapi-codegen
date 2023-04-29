package org.davidmoten.oa3.codegen.http;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Problem {

    @JsonProperty("message")
    public String message;

    @JsonProperty("statusCode")
    public int statusCode;

    @JsonCreator
    public Problem(@JsonProperty("message") String message, @JsonProperty("statusCode") int statusCode) {
        this.message = message;
        this.statusCode = statusCode;
    }
}
