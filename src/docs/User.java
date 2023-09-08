package org.davidmoten.oa3.codegen.test.library.schema;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
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
import java.util.Optional;

import org.davidmoten.oa3.codegen.runtime.Preconditions;
import org.davidmoten.oa3.codegen.test.library.Globals;
import org.davidmoten.oa3.codegen.util.Util;
import org.springframework.boot.context.properties.ConstructorBinding;

@JsonInclude(Include.NON_NULL)
@JsonAutoDetect(fieldVisibility = Visibility.ANY, creatorVisibility = Visibility.ANY, setterVisibility = Visibility.ANY)
@Generated(value = "com.github.davidmoten:openapi-codegen-runtime:0.1.6")
public final class User {

    @JsonProperty("firstName")
    private final String firstName;

    @JsonProperty("lastName")
    private final String lastName;

    @JsonProperty("email")
    private final String email;

    @JsonProperty("mobile")
    private final String mobile;

    @JsonCreator
    private User(
            @JsonProperty("firstName") String firstName,
            @JsonProperty("lastName") String lastName,
            @JsonProperty("email") String email,
            @JsonProperty("mobile") String mobile) {
        if (Globals.config().validateInConstructor().test(User.class)) {
            Preconditions.checkMinLength(firstName, 1, "firstName");
            Preconditions.checkMinLength(lastName, 1, "lastName");
            Preconditions.checkMinLength(email, 3, "email");
            Preconditions.checkMatchesPattern(mobile, "[\\+][0-9]+", "mobile");
        }
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.mobile = mobile;
    }

    @ConstructorBinding
    public User(
            String firstName,
            String lastName,
            String email,
            Optional<String> mobile) {
        if (Globals.config().validateInConstructor().test(User.class)) {
            Preconditions.checkNotNull(firstName, "firstName");
            Preconditions.checkMinLength(firstName, 1, "firstName");
            Preconditions.checkNotNull(lastName, "lastName");
            Preconditions.checkMinLength(lastName, 1, "lastName");
            Preconditions.checkNotNull(email, "email");
            Preconditions.checkMinLength(email, 3, "email");
            Preconditions.checkNotNull(mobile, "mobile");
            Preconditions.checkMatchesPattern(mobile, "[\\+][0-9]+", "mobile");
        }
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.mobile = mobile.orElse(null);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private String firstName;
        private String lastName;
        private String email;
        private Optional<String> mobile = Optional.empty();

        Builder() {
        }

        public BuilderWithFirstName firstName(String firstName) {
            this.firstName = firstName;
            return new BuilderWithFirstName(this);
        }
    }

    public static final class BuilderWithFirstName {

        private final Builder b;

        BuilderWithFirstName(Builder b) {
            this.b = b;
        }

        public BuilderWithLastName lastName(String lastName) {
            this.b.lastName = lastName;
            return new BuilderWithLastName(this.b);
        }
    }

    public static final class BuilderWithLastName {

        private final Builder b;

        BuilderWithLastName(Builder b) {
            this.b = b;
        }

        public BuilderWithEmail email(String email) {
            this.b.email = email;
            return new BuilderWithEmail(this.b);
        }
    }

    public static final class BuilderWithEmail {

        private final Builder b;

        BuilderWithEmail(Builder b) {
            this.b = b;
        }

        public BuilderWithEmail mobile(String mobile) {
            this.b.mobile = Optional.of(mobile);
            return this;
        }

        public BuilderWithEmail mobile(Optional<String> mobile) {
            this.b.mobile = mobile;
            return this;
        }

        public User build() {
            return new User(this.b.firstName, this.b.lastName, this.b.email, this.b.mobile);
        }
    }

    public static BuilderWithFirstName firstName(String firstName) {
        return builder().firstName(firstName);
    }

    public String firstName() {
        return firstName;
    }

    public String lastName() {
        return lastName;
    }

    public String email() {
        return email;
    }

    public Optional<String> mobile() {
        return Optional.ofNullable(mobile);
    }

    Map<String, Object> _internal_properties() {
        return Maps
                .put("firstName", (Object) firstName)
                .put("lastName", (Object) lastName)
                .put("email", (Object) email)
                .put("mobile", (Object) mobile)
                .build();
    }

    public User withFirstName(String firstName) {
        return new User(firstName, lastName, email, Optional.ofNullable(mobile));
    }

    public User withLastName(String lastName) {
        return new User(firstName, lastName, email, Optional.ofNullable(mobile));
    }

    public User withEmail(String email) {
        return new User(firstName, lastName, email, Optional.ofNullable(mobile));
    }

    public User withMobile(Optional<String> mobile) {
        return new User(firstName, lastName, email, mobile);
    }

    public User withMobile(String mobile) {
        return new User(firstName, lastName, email, Optional.of(mobile));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        User other = (User) o;
        return 
            Objects.equals(this.firstName, other.firstName) && 
            Objects.equals(this.lastName, other.lastName) && 
            Objects.equals(this.email, other.email) && 
            Objects.equals(this.mobile, other.mobile);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                firstName, 
                lastName, 
                email, 
                mobile);
    }

    @Override
    public String toString() {
        return Util.toString(User.class,
                "firstName", firstName,
                "lastName", lastName,
                "email", email,
                "mobile", mobile);
    }
}
