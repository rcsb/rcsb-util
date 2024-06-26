package org.rcsb.common.config;

/**
 * An exception thrown when a {@link ConfigMapImpl} does not contain a property with the requested key.
 *
 * @author Douglas Myers-Turnbull
 * @since 2.0.0
 */
public class ConfigKeyMissingException extends RuntimeException implements ConfigException {

    public ConfigKeyMissingException(String message) {
        super(message);
    }

    public ConfigKeyMissingException(String message, Throwable cause) {
        super(message, cause);
    }
}
