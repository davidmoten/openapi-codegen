package org.davidmoten.oa3.codegen.http;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public final class Thing {
    @JsonProperty("name")
    public String name;

    @JsonProperty("age")
    public int age;

    @JsonCreator
    public Thing(@JsonProperty("name") String name, @JsonProperty("age") int age) {
        this.name = name;
        this.age = age;
    }
}