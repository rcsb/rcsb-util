package org.rcsb.common.config;

/**
 * An exception thrown when no valid config profile was found.
 *
 * @author Douglas Myers-Turnbull
 * @since 2.0.0
 */
public class ConfigProfileException extends RuntimeException implements ConfigException {

    public ConfigProfileException(String message) {
        super(message);
    }

    public ConfigProfileException(String message, Throwable cause) {
        super(message, cause);
    }
}
