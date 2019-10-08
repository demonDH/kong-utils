package com.kong.config;

import lombok.Getter;
import lombok.Setter;

/**
 * @author kong
 */
@Getter
@Setter
public class BaseConfig {
    private boolean influxLog;
    private boolean accessLog;
    private boolean ding;
}
