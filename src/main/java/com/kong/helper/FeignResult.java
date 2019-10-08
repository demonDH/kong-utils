package com.kong.helper;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @author kong
 */
@Data
public class FeignResult<R> {

    @JsonProperty("code")
    private Integer code;

    @JsonProperty
    private String message;

    @JsonProperty
    private R data;
}
