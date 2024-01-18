package org.rcsb.common.config;

/**
 * An exception related to config.
 *
 * @author Douglas Myers-Turnbull
 * @since 1.9.0
 */
public abstract class ConfigException extends RuntimeException {

    protected ConfigException(String message) {
        super(message);
    }

    protected ConfigException(String message, Throwable cause) {
        super(message, cause);
    }

    protected ConfigException(Throwable cause) {
        super(cause);
    }
}
