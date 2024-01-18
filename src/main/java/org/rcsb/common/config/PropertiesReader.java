package org.rcsb.common.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.text.ParseException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * A convenience properties reader providing boiler plate to read properties, set defaults and log the process.
 *
 * @author Jose Duarte
 * @since 1.5.0

 * @deprecated Use {@link ConfigMap} instead
 */
@Deprecated(since="1.9.0", forRemoval = true)
public class PropertiesReader extends ConfigMap {

    private static final Logger logger = LoggerFactory.getLogger(PropertiesReader.class);

    private final String fileName;
    private final URL configUrl;

    public PropertiesReader(Map<?, ?> props, String fileName, URL configUrl) {
        super(props);
        logger.info("Reading config file {} from URL {}", fileName, configUrl);
        this.fileName = fileName;
        this.configUrl = configUrl;
    }

    public URL getConfigUrl() {
        return configUrl;
    }

    public String getFileName() {
        return fileName;
    }

    /**
     * Read String from given field or set defaultValue
     *
     * @param field the property name
     * @param defaultValue the default value, if null the property is considered non-optional
     * @return the parsed string value
     * @throws ConfigKeyMissingException if {@code defaultValue} is null and the property does not exist
     */
    public String loadStringField(String field, String defaultValue) {
        return loadProp(field, String::toString, defaultValue, "string");
    }

    /**
     * Read double from given field or set defaultValue
     *
     * @param field the property name
     * @param defaultValue the default value, if null the property is considered non-optional
     * @return the parsed boolean value
     * @throws ConfigKeyMissingException if {@code defaultValue} is null and the property does not exist
     * @throws ParseException if the property could not be converted to a boolean
     */
    public boolean loadBooleanField(String field, Boolean defaultValue) {
        return loadProp(field, Boolean::parseBoolean, defaultValue, "boolean");
    }

    /**
     * Read int from given field or set defaultValue
     *
     * @param field        the property name
     * @param defaultValue the default value, if null the property is considered non-optional
     * @return the parsed int value
     * @throws ConfigKeyMissingException if {@code defaultValue} is null and the property does not exist
     * @throws ParseException if the property could not be converted to an int
     */
    public int loadIntegerField(String field, Integer defaultValue) {
        return loadProp(field, Integer::parseInt, defaultValue, "integer");
    }

    /**
     * Read double from given field or set defaultValue
     *
     * @param field the property name
     * @param defaultValue the default value, if null the property is considered non-optional
     * @return the parsed double value
     * @throws ConfigKeyMissingException if {@code defaultValue} is null and the property does not exist
     * @throws ParseException if the property could not be converted to a double
     */
    public double loadDoubleField(String field, Double defaultValue) {
        return loadProp(field, Double::parseDouble, defaultValue, "double");
    }

    /**
     * Read a comma separated String array from given field. Commas are not an allowed value within the strings.
     *
     * @param field the property name
     * @param defaultValue the default value, if null the property is considered non-optional
     * @return a String array
     * @throws ConfigKeyMissingException if {@code defaultValue} is null and the property does not exist
     * @throws ParseException if the property could not be converted to a string array
     */
    public String[] loadStringArrayField(String field, String[] defaultValue) {
        return loadProp(field, this::convertStringArray, defaultValue, "string array");
    }

    /**
     * Read a comma separated double array from given field.
     *
     * @param field the property name
     * @param defaultValue the default value, if null the property is considered non-optional
     * @return a double array
     * @throws ConfigKeyMissingException if {@code defaultValue} is null and the property does not exist
     * @throws ParseException if the property could not be converted to a double array
     */
    public double[] loadDoubleArrayField(String field, double[] defaultValue) {
        return loadProp(field, this::convertDoubleArray, defaultValue, "double array");
    }

    /**
     * Read a comma separated int array from given field.
     *
     * @param field the property name
     * @param defaultValue the default value, if null the property is considered non-optional
     * @return an int array
     * @throws ConfigKeyMissingException if {@code defaultValue} is null and the property does not exist
     * @throws ParseException if the property could not be converted to an int array
     */
    public int[] loadIntArrayField(String field, int[] defaultValue) {
        return loadProp(field, this::convertIntArray, defaultValue, "int array");
    }

    public String[] convertStringArray(String value) {
        return ConfigConverters.splitCsv(value).toArray(String[]::new);
    }

    public double[] convertDoubleArray(String value) {
        var x = ConfigConverters.splitCsv(value).stream().map(Double::parseDouble).mapToDouble(Double::valueOf);
        return convertPrimitiveArray(value, v -> v.mapToDouble(Double::parseDouble).toArray(), "double");
    }

    public int[] convertIntArray(String value) {
        return convertPrimitiveArray(value, v -> v.mapToInt(Integer::parseInt).toArray(), "int");
    }

    protected <T, A> A convertPrimitiveArray(String value, Function<? super Stream<String>, A> convert, String type) {
        try {
            return convert.apply(ConfigConverters.splitCsv(value).stream());
        } catch (NumberFormatException e) {
            throw new ConfigValueConversionException("Could not parse '" + value + "' to array of '" + type + "'", e);
        }
    }

}
