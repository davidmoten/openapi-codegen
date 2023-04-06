package pet.store;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;


//runtime library code
@JsonAutoDetect(fieldVisibility = Visibility.ANY, creatorVisibility = Visibility.ANY)
@JsonInclude(value = Include.NON_NULL)
public class DefaultError {

    @JsonProperty("statusCode")
    private int statusCode;
    @JsonProperty("message")
    private final String message;

    @JsonCreator
    private DefaultError(@JsonProperty("statusCode") int statusCode, @JsonProperty("message") String message) {
        this.statusCode = statusCode;
        this.message = message;
    }

    public DefaultError(int statusCode, Throwable e) {
        this(statusCode, e.getMessage());
    }
}
