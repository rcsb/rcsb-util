package org.rcsb.common.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.text.ParseException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.function.Function;
import java.util.function.Predicate;
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

    private final Map<String, String> props;
    private final String fileName;
    private final URL configUrl;

    public PropertiesReader(Map<?, ?> props, String fileName, URL configUrl) {
        this.props = props.entrySet()
            .stream()
            .collect(Collectors.toMap(e -> e.getKey().toString(), e -> e.getValue().toString()));
        this.fileName = fileName;
        this.configUrl = configUrl;
    }

    public Map<String, String> getProperties() {
        return props.entrySet()
            .stream()
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public Map<String, String> getProperties(Predicate<? super Map.Entry<String, String>> predicate) {
        return props.entrySet()
            .stream()
            .filter(predicate)
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public Map<String, Object> getProperties(Pattern pattern) {
        return props.entrySet()
            .stream()
            .filter(e -> pattern.matcher(e.getKey().toString()).matches())
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public URL getConfigUrl() {
        return configUrl;
    }

    public String getFileName() {
        return fileName;
    }

    public boolean hasProperty(String field) {
        return props.containsKey(field);
    }

    /**
     * Read double from given field or set defaultValue
     *
     * @param field the property name
     * @param defaultValue the default value, if null the property is considered non-optional
     * @return the parsed boolean value
     * @throws ConfigPropertyMissingException if {@code defaultValue} is null and the property does not exist
     * @throws ParseException if the property could not be converted to a boolean
     */
    public boolean loadBooleanField(String field, Boolean defaultValue) {
        return loadProp(field, defaultValue, Boolean::parseBoolean, "boolean");
    }

    /**
     * Read int from given field or set defaultValue
     *
     * @param field        the property name
     * @param defaultValue the default value, if null the property is considered non-optional
     * @return the parsed int value
     * @throws ConfigPropertyMissingException if {@code defaultValue} is null and the property does not exist
     * @throws ParseException if the property could not be converted to an int
     */
    public int loadIntegerField(String field, Integer defaultValue) {
        return loadProp(field, defaultValue, Integer::parseInt, "integer");
    }

    /**
     * Read double from given field or set defaultValue
     *
     * @param field the property name
     * @param defaultValue the default value, if null the property is considered non-optional
     * @return the parsed double value
     * @throws ConfigPropertyMissingException if {@code defaultValue} is null and the property does not exist
     * @throws ParseException if the property could not be converted to a double
     */
    public double loadDoubleField(String field, Double defaultValue) {
        return loadProp(field, defaultValue, Double::parseDouble, "double");
    }

    /**
     * Read String from given field or set defaultValue
     *
     * @param field the property name
     * @param defaultValue the default value, if null the property is considered non-optional
     * @return the parsed string value
     * @throws ConfigPropertyMissingException if {@code defaultValue} is null and the property does not exist
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
     * @throws ConfigPropertyMissingException if {@code defaultValue} is null and the property does not exist
     * @throws ParseException if the property could not be converted to a double array
     */
    public double[] loadDoubleArrayField(String field, double[] defaultValue) {
        return loadProp(field, defaultValue, this::convertDoubleArray, "double array");
    }

    /**
     * Read a comma separated int array from given field
     *
     * @param field the property name
     * @param defaultValue the default value, if null the property is considered non-optional
     * @return an int array
     * @throws ConfigPropertyMissingException if {@code defaultValue} is null and the property does not exist
     * @throws ParseException if the property could not be converted to an int array
     */
    public int[] loadIntArrayField(String field, int[] defaultValue) {
        return loadProp(field, defaultValue, this::convertIntArray, "int array");
    }

    /**
     * Read a comma separated String array from given field. Commas are not an allowed value within the strings.
     *
     * @param field the property name
     * @param defaultValue the default value, if null the property is considered non-optional
     * @return a String array
     * @throws ConfigPropertyMissingException if {@code defaultValue} is null and the property does not exist
     * @throws ParseException if the property could not be converted to a string array
     */
    public String[] loadStringArrayField(String field, String[] defaultValue) {
        return loadProp(field, defaultValue, this::convertStringArray, "string array");
    }

    public double[] convertDoubleArray(String value) {
        return Arrays.stream(csvPattern.split(value))
            .mapToDouble(Double::parseDouble)
            .toArray();
    }

    public int[] convertIntArray(String value) {
        return Arrays.stream(csvPattern.split(value))
            .mapToInt(Integer::parseInt)
            .toArray();
    }

    public String[] convertStringArray(String value) {
        return csvPattern.split(value);
    }

    private <T> T loadProp(String field, T defaultValue, Function<String, ? extends T> convert, String typeName) {
        String value = props.get(field);
        // option 1: provided a value
        if (value != null && !value.isBlank()) {
            T finalValue;
            try {
                finalValue = convert.apply(value);
            } catch (NumberFormatException e) {
                if (defaultValue == null) {
                    throw new ConfigParseException("Could not parse " + typeName + " from '" + field + "' property", e);
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
        throw new ConfigPropertyMissingException(
            "Missing config '" + field + "' in '" + fileName + "' at URL" + " " + configUrl
        );
    }

}
