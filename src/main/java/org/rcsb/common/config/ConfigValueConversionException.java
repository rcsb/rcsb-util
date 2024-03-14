package org.rcsb.common.config;

/**
 * An exception thrown when {@link ConfigMapImpl#getLazy} could not convert the property value to the requested type.
 *
 * @author Douglas Myers-Turnbull
 * @since 2.0.0
 */
public class ConfigValueConversionException extends ConfigException {

    public ConfigValueConversionException(String message) {
        super(message);
    }

    public ConfigValueConversionException(String message, Throwable cause) {
        super(message, cause);
    }
}
