package org.rcsb.common.config;

public class ConfigException extends IllegalStateException {

    public ConfigException(String message) {
        super(message);
    }

    public ConfigException(String message, Throwable cause) {
        super(message, cause);
    }

}
