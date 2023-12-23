package org.rcsb.common.config;

public class ConfigPropertyMissingException extends IllegalStateException {

    public ConfigPropertyMissingException(String message) {
        super(message);
    }

    public ConfigPropertyMissingException(String message, Throwable cause) {
        super(message, cause);
    }

}
