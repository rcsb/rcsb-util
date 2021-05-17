package org.rcsb.common.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.Properties;

/**
 * A convenience properties reader providing boiler plate to read properties, set defaults and log the process.
 *
 * @author Jose Duarte
 * @since 1.5.0
 */
public class PropertiesReader {

    private static final Logger logger = LoggerFactory.getLogger(PropertiesReader.class);

    private final Properties props;
    private final String fileName;
    private final URL configUrl;

    public PropertiesReader(Properties props, URL configUrl) {
        this.props = props;
        this.fileName = getFileNameFromUrl(configUrl);
        this.configUrl = configUrl;
    }

    private static String getFileNameFromUrl(URL url) {
        String file = url.getFile();
        if (file.contains("/")) {
            return file.substring(file.lastIndexOf('/'));
        }
        return file;
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
     * @throws IllegalArgumentException if property is not optional and can't be read
     */
    public int loadIntegerField(String field, Integer defaultValue) {
        String value = props.getProperty(field);
        int finalValue;
        if (value == null || value.trim().equals("")) {
            if (defaultValue != null) {
                logger.warn("Optional property '{}' is not specified correctly in config file {} found in URL {}.  Will use default value '{}' instead.", field, fileName, configUrl, defaultValue);
                finalValue = defaultValue;
            } else {
                logger.error("Property '{}' is not specified correctly in config file {} found in URL {}", field, fileName, configUrl);
                throw new IllegalArgumentException("Missing configuration '" + field + "' in '" + fileName + "' found in URL " + configUrl);
            }
        } else {
            try {
                finalValue = Integer.parseInt(value);
                logger.info("Using value '{}' for configuration field '{}'", value, field);
            } catch (NumberFormatException e) {
                if (defaultValue != null) {
                    logger.warn("Could not parse integer from specified value '{}' for optional property '{}'", value, field);
                    finalValue = defaultValue;
                } else {
                    logger.error("Could not parse integer from specified value '{}' for property '{}'", value, field);
                    throw new IllegalArgumentException("Could not parse double from specified '" + field + "' property");
                }
            }

        }
        return finalValue;
    }

    /**
     * Read double from given field or set defaultValue
     *
     * @param field        the property name
     * @param defaultValue the default value, if null the property is considered non-optional
     * @return the parsed double value
     * @throws IllegalArgumentException if property is not optional and can't be read
     */
    public double loadDoubleField(String field, Double defaultValue) {
        String value = props.getProperty(field);
        double finalValue;
        if (value == null || value.trim().equals("")) {
            if (defaultValue != null) {
                logger.warn("Optional property '{}' is not specified correctly in config file {} found in URL {}.  Will use default value '{}' instead.", field, fileName, configUrl, defaultValue);
                finalValue = defaultValue;
            } else {
                logger.error("Property '{}' is not specified correctly in config file {} found in URL {}", field, fileName, configUrl);
                throw new IllegalArgumentException("Missing configuration '" + field + "' in '" + fileName + "' found in URL " + configUrl);
            }
        } else {
            try {
                finalValue = Double.parseDouble(value);
                logger.info("Using value '{}' for configuration field '{}'", value, field);
            } catch (NumberFormatException e) {
                if (defaultValue != null) {
                    logger.warn("Could not parse double from specified value '{}' for optional property '{}'", value, field);
                    finalValue = defaultValue;
                } else {
                    logger.error("Could not parse double from specified value '{}' for property '{}'", value, field);
                    throw new IllegalArgumentException("Could not parse double from specified '" + field + "' property");
                }
            }
        }
        return finalValue;
    }

    /**
     * Read String from given field or set defaultValue
     *
     * @param field        the property name
     * @param defaultValue the default value, if null the property is considered non-optional
     * @return the parsed string value
     * @throws IllegalArgumentException if property is not optional and can't be read
     */
    public String loadStringField(String field, String defaultValue) {
        String value = props.getProperty(field);
        String finalValue;
        if (value == null || value.trim().equals("")) {
            if (defaultValue != null) {
                logger.warn("Optional property '{}' is not specified correctly in config file {} found in URL {}. Will use default value '{}' instead.", field, fileName, configUrl, defaultValue);
                finalValue = defaultValue;
            } else {
                logger.error("Property '{}' is not specified correctly in config file {} found in URL {}", field, fileName, configUrl);
                throw new IllegalArgumentException("Missing configuration '" + field + "' in '" + fileName + "' found in URL " + configUrl);
            }
        } else {
            logger.info("Using value '{}' for configuration field '{}'", value, field);
            finalValue = value;
        }

        return finalValue;
    }

    /**
     * Read a comma separated double array from given field
     *
     * @param field the property name
     * @param defaultValue the default value, if null the property is considered non-optional
     * @return a double array
     * @throws IllegalArgumentException if property can't be read
     */
    public double[] loadDoubleArrayField(String field, double[] defaultValue) {
        String value = props.getProperty(field);
        double[] doubleArrValue;
        if (value == null || value.trim().equals("")) {
            if (defaultValue != null) {
                logger.warn("Optional property '{}' is not specified correctly in config file {} found in URL {}. Will use default value '{}' instead.", field, fileName, configUrl, defaultValue);
                return defaultValue;
            } else {
                logger.error("Field '{}' is not specified correctly in config file {} found in URL {}", field, fileName, configUrl);
                throw new IllegalArgumentException("Missing configuration '" + field + "' in '" + fileName + "' found in URL " + configUrl);
            }
        } else {
            logger.info("Using value '{}' for configuration field '{}'", value, field);
        }
        String[] tokens = value.split(",\\s*");
        doubleArrValue = new double[tokens.length];
        for (int i = 0; i < tokens.length; i++) {
            try {
                doubleArrValue[i] = Double.parseDouble(tokens[i]);
            } catch (NumberFormatException e) {
                logger.error("Could not parse double from specified value '{}' at index {} for property '{}'", tokens[i], i, field);
                throw new IllegalArgumentException("Could not parse double from specified '" + field + "' property");
            }

        }
        return doubleArrValue;
    }

    /**
     * Read a comma separated int array from given field
     *
     * @param field the property name
     * @param defaultValue the default value, if null the property is considered non-optional
     * @return an int array
     * @throws IllegalArgumentException if property can't be read
     */
    public int[] loadIntArrayField(String field, int[] defaultValue) {
        String value = props.getProperty(field);
        int[] intArrValue;
        if (value == null || value.trim().equals("")) {
            if (defaultValue != null) {
                logger.warn("Optional property '{}' is not specified correctly in config file {} found in URL {}. Will use default value '{}' instead.", field, fileName, configUrl, defaultValue);
                return defaultValue;
            } else {
                logger.error("Field '{}' is not specified correctly in config file {} found in URL {}", field, fileName, configUrl);
                throw new IllegalArgumentException("Missing configuration '" + field + "' in '" + fileName + "' found in URL " + configUrl);
            }
        } else {
            logger.info("Using value '{}' for configuration field '{}'", value, field);
        }
        String[] tokens = value.split(",\\s*");
        intArrValue = new int[tokens.length];
        for (int i = 0; i < tokens.length; i++) {
            try {
                intArrValue[i] = Integer.parseInt(tokens[i]);
            } catch (NumberFormatException e) {
                logger.error("Could not parse int from specified value '{}' at index {} for property '{}'", tokens[i], i, field);
                throw new IllegalArgumentException("Could not parse int from specified '" + field + "' property");
            }

        }
        return intArrValue;
    }

    /**
     * Read a comma separated String array from given field. Commas are not an allowed value within the strings.
     *
     * @param field the property name
     * @param defaultValue the default value, if null the property is considered non-optional
     * @return a String array
     * @throws IllegalArgumentException if property can't be read
     */
    public String[] loadStringArrayField(String field, String[] defaultValue) {
        String value = props.getProperty(field);
        if (value == null || value.trim().equals("")) {
            if (defaultValue != null) {
                logger.warn("Optional property '{}' is not specified correctly in config file {} found in URL {}. Will use default value '{}' instead.", field, fileName, configUrl, defaultValue);
                return defaultValue;
            } else {
                logger.error("Property '{}' is not specified correctly in config file {} found in URL {}", field, fileName, configUrl);
                throw new IllegalArgumentException("Missing configuration '" + field + "' in '" + fileName + "' found in URL " + configUrl);
            }
        } else {
            String[] tokens = value.split(",\\s*");
            String[] stringArrValue = new String[tokens.length];
            System.arraycopy(tokens, 0, stringArrValue, 0, tokens.length);
            return stringArrValue;
        }
    }
}
