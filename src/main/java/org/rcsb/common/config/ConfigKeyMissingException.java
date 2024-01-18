package org.rcsb.common.config;

/**
 * An exception thrown when a {@link ConfigMap} does not contain a property with the requested key.
 *
 * @author Douglas Myers-Turnbull
 * @since 1.9.0
 */
public class ConfigKeyMissingException extends ConfigException {

    public ConfigKeyMissingException(String message) {
        super(message);
    }

    public ConfigKeyMissingException(String message, Throwable cause) {
        super(message, cause);
    }
}
