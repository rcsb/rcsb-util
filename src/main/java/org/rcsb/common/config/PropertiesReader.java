package org.rcsb.common.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * A convenience properties reader providing boiler plate to read properties, set defaults and log the process.
 *
 * @author Jose Duarte
 * @since 1.5.0
 */
public class PropertiesReader {

    private static final Logger logger = LoggerFactory.getLogger(PropertiesReader.class);

    private static final Pattern csvPattern = Pattern.compile(",\\s*");

    private final Properties props;
    private final String fileName;
    private final URL configUrl;

    public PropertiesReader(Properties props, String fileName, URL configUrl) {
        this.props = props;
        this.fileName = fileName;
        this.configUrl = configUrl;
    }

    public Properties getProperties() {
        return props;
    }

    public URL getConfigUrl() {
        return configUrl;
    }

    public String getFileName() {
        return fileName;
    }

    /**
     * Read int from given field or set defaultValue
     *
     * @param field        the property name
     * @param defaultValue the default value, if null the property is considered non-optional
     * @return the parsed int value
     * @throws ConfigException if property is not optional and can't be read
     */
    public int loadIntegerField(String field, Integer defaultValue) {
        return loadProp(field, defaultValue, Integer::parseInt, "integer");
    }

    /**
     * Read double from given field or set defaultValue
     *
     * @param field        the property name
     * @param defaultValue the default value, if null the property is considered non-optional
     * @return the parsed double value
     * @throws ConfigException if property is not optional and can't be read
     */
    public double loadDoubleField(String field, Double defaultValue) {
        return loadProp(field, defaultValue, Double::parseDouble, "double");
    }

    /**
     * Read String from given field or set defaultValue
     *
     * @param field        the property name
     * @param defaultValue the default value, if null the property is considered non-optional
     * @return the parsed string value
     * @throws ConfigException if property is not optional and can't be read
     */
    public String loadStringField(String field, String defaultValue) {
        return loadProp(field, defaultValue, String::toString, "string");
    }

    /**
     * Read a comma separated double array from given field
     *
     * @param field the property name
     * @param defaultValue the default value, if null the property is considered non-optional
     * @return a double array
     * @throws ConfigException if property can't be read
     */
    public double[] loadDoubleArrayField(String field, double[] defaultValue) {
        Function<String, double[]> convert = vs ->
            Arrays.stream(csvPattern.split(vs))
            .mapToDouble(Double::parseDouble)
            .toArray();
        return loadProp(field, defaultValue, convert, "double array");
    }

    /**
     * Read a comma separated int array from given field
     *
     * @param field the property name
     * @param defaultValue the default value, if null the property is considered non-optional
     * @return an int array
     * @throws ConfigException if property can't be read
     */
    public int[] loadIntArrayField(String field, int[] defaultValue) {
        Function<String, int[]> convert = vs ->
            Arrays.stream(csvPattern.split(vs))
                .mapToInt(Integer::parseInt)
                .toArray();
        return loadProp(field, defaultValue, convert, "int array");
    }

    /**
     * Read a comma separated String array from given field. Commas are not an allowed value within the strings.
     *
     * @param field the property name
     * @param defaultValue the default value, if null the property is considered non-optional
     * @return a String array
     * @throws ConfigException if property can't be read
     */
    public String[] loadStringArrayField(String field, String[] defaultValue) {
        Function<String, String[]> convert = vs -> csvPattern.split(vs);
        return loadProp(field, defaultValue, convert, "string array");
    }

    private <T> T loadProp(String field, T defaultValue, Function<String, ? extends T> convert, String typeName) {
        String value = props.getProperty(field);
        // option 1: provided a value
        if (value != null && !value.isBlank()) {
            T finalValue;
            try {
                finalValue = convert.apply(value);
            } catch (NumberFormatException e) {
                if (defaultValue == null) {
                    throw new ConfigException("Could not parse double from '" + field + "' property", e);
                }
                logger.error(
                    "Optional property {} value '{}' is not a {}. Will use the default value '{}'.",
                    field, value, typeName, defaultValue
                );
                return defaultValue;
            }
            logger.info("Using value '{}' for property '{}'", value, field);
            return finalValue;
        }
        // option 2: there's a default value
        if (defaultValue != null) {
            logger.warn(
                "Optional property '{}' is not in config file {} at URL {}."
                    + " Will use the default value '{}'.",
                field, fileName, configUrl, defaultValue
            );
            return defaultValue;
        }
        // option 3: uh-oh, there is no default either
        logger.warn(
            "Property '{}' is not in config file {} at URL {}.",
            field, fileName, configUrl
        );
        throw new ConfigException(
            "Missing config '" + field + "' in '" + fileName + "' at URL" + " " + configUrl
        );
    }

}
