package org.rcsb.common.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.*;

/**
 * A map of properties, presumably read from a config file.
 * Contains {@code getXxx} utility methods that convert a value to some type.
 * The most general form is {@link #get(String, Function, Object)}.
 * These methods throw a {@link ConfigValueConversionException} if the value could not be converted,
 * and a {@link ConfigKeyMissingException} if the key was not found.
 * Implements most of the {@link java.util.Map} methods, like {@link #size()} and {@link #entrySet()}.
 * Call {@link #rawMap()} to get a true map; this returns an unmodifiable view of the underlying map.
 *
 * Example:
 *
 * {@code
 * var config = ConfigMap(myMap);
 * Animal animal = config.get("myapp.animals", str -> new Animal(str, ""));
 * List<Double> values = config.getList("myapp.somepath.values", Double::parseDouble);
 * }
 *
 * @author Douglas Myers-Turnbull
 * @since 2.0.0
 * @see ConfigConverters for utilities to convert property values
 */
public class ConfigMap {

    private static final Pattern csvPattern = Pattern.compile(",\\s*");
    private static final Logger logger = LoggerFactory.getLogger(ConfigMap.class);
    private final Map<String, String> props;

    public ConfigMap(Map<?, ?> props) {
        this.props = props.entrySet()
            .stream()
            .collect(Collectors.toMap(e -> e.getKey().toString(), e -> e.getValue().toString()));
    }

    /**
     * Returns a new ConfigMap containing only keys that begin with {@code prefix}.
     */
    public ConfigMap subsetByKeyPrefix(String prefix) {
        return subset(e -> e.getKey().startsWith(prefix));
    }

    /**
     * Returns a new ConfigMap containing only keys that match the regex pattern {@code pattern}.
     */
    public ConfigMap subsetByRegex(String pattern) {
        return subset(e -> Pattern.compile(pattern).matcher(e.getKey().toString()).matches());
    }

    /**
     * Returns a new ConfigMap containing only keys that match the regex pattern {@code pattern}.
     */
    public ConfigMap subsetByRegex(Pattern pattern) {
        return subset(e -> pattern.matcher(e.getKey().toString()).matches());
    }

    /**
     * Returns a new ConfigMap containing only properties where {@code predicate(key, value)} is {@code true}.
     */
    public ConfigMap subset(Predicate<? super Map.Entry<String, String>> predicate) {
        return new ConfigMap(
            props.entrySet()
                .stream()
                .filter(predicate)
                .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue))
        );
    }

    /**
     * @see ConfigConverters#convertPath(String)
     */
    public Path getPath(String field) {
        return get(field, ConfigConverters::convertPath);
    }

    /**
     * @see ConfigConverters#convertExtantFile(String)
     */
    public Path getExtantFile(String field) {
        return get(field, ConfigConverters::convertExtantFile);
    }

    /**
     * @see ConfigConverters#convertNonextantPath(String)
     */
    public Path getNonextantPath(String field) {
        return get(field, ConfigConverters::convertNonextantPath);
    }

    /**
     * @see ConfigConverters#convertUri(String)
     */
    public URI getUri(String field) {
        return get(field, ConfigConverters::convertUri);
    }

    /**
     * @see ConfigConverters#convertUrl(String)
     */
    public URL getUrl(String field) {
        return get(field, ConfigConverters::convertUrl);
    }

    public boolean getBool(String field) {
        return get(field, Boolean::parseBoolean);
    }

    public boolean getBool(String field, boolean defaultValue) {
        return get(field, Boolean::parseBoolean, defaultValue);
    }

    public double getDouble(String field) {
        return get(field, Double::parseDouble);
    }

    public double getDouble(String field, double defaultValue) {
        return get(field, Double::parseDouble, defaultValue);
    }

    public int getInt(String field) {
        return get(field, Integer::parseInt);
    }

    public int getInt(String field, int defaultValue) {
        return get(field, Integer::parseInt, defaultValue);
    }

    public long getLong(String field) {
        return get(field, Long::parseLong);
    }

    public long getLong(String field, long defaultValue) {
        return get(field, Long::parseLong, defaultValue);
    }

    public String getStr(String field) {
        return get(field, String::valueOf);
    }

    public String getStr(String field, String defaultValue) {
        return get(field, String::valueOf, defaultValue);
    }

    /**
     * Loads a value, throwing a {@link ConfigKeyMissingException} if it is not provided.
     * @see #get(String, Function, T).
     */
    public <T> T get(String field, Function<String, ? extends T> convert) {
        return get(field, convert, null);
    }

    /**
     * Example 1: Get the double or 0.0 if not specified
     * {@code
     * double coefficients = load("my.field", Double::parseDouble, 0.0);
     * }
     *
     * Example 2: Get the double, or throw a {@link ConfigKeyMissingException} if it is not specified.
     * {@code
     * double coefficients = loadList("my.field", Double::parseDouble);
     * }
     *
     * @param field the property name
     * @param defaultValue the default value, if null the property is considered non-optional
     * @param convert A function mapping the read string value |--> type {@link T}
     * @return A list of {@link T}
     * @param <T> The type of the returned value
     * @throws ConfigKeyMissingException if {@code defaultValue} is null and the property does not exist
     * @throws ParseException if the property could not be converted to {@link T}
     */
    public <T> T get(String field, Function<String, ? extends T> convert, T defaultValue) {
        String value = loadProp(field, String::toString, null, "string");
        try {
            return convert.apply(value);
        } catch (RuntimeException e) {
            throw new ConfigValueConversionException("Could not parse property " + field + " value '" + value + "'", e);
        }
    }

    // arrays of primitive types are a little tricky, so we provide convenience methods below

    public double[] getDoubleArray(String field) {
        return getDoubleArray(field, null);
    }

    public double[] getDoubleArray(String field, double[] defaultValue) {
        var defaults = defaultValue == null? null : DoubleStream.of(defaultValue).boxed().collect(Collectors.toList());
        // parse values inside loadProp (calling mapToLong after) so that we can throw a ConfigParseException
        return getList(field, Double::parseDouble, defaults).stream().mapToDouble(Double::valueOf).toArray();
    }

    public int[] getIntArray(String field) {
        return getIntArray(field, null);
    }

    public int[] getIntArray(String field, int[] defaultValue) {
        var defaults = defaultValue == null? null : IntStream.of(defaultValue).boxed().collect(Collectors.toList());
        // parse values inside loadProp (calling mapToLong after) so that we can throw a ConfigParseException
        return getList(field, Integer::parseInt, defaults).stream().mapToInt(Integer::valueOf).toArray();
    }

    public long[] getLongArray(String field) {
        return getLongArray(field, null);
    }

    public long[] getLongArray(String field, long[] defaultValue) {
        var defaults = defaultValue == null? null : LongStream.of(defaultValue).boxed().collect(Collectors.toList());
        // parse values inside loadProp (calling mapToLong after) so that we can throw a ConfigParseException
        return getList(field, Long::parseLong, defaults).stream().mapToLong(Long::valueOf).toArray();
    }

    public String[] getStrArray(String field) {
        return getStrList(field).toArray(String[]::new);
    }

    public String[] getStrArray(String field, String[] defaultValue) {
        if (defaultValue == null) {
            return getStrList(field).toArray(String[]::new);
        }
        return getStrList(field, Arrays.asList(defaultValue)).toArray(String[]::new);
    }

    /**
     * Loads a string list, throwing a {@link ConfigKeyMissingException} if it is not provided.
     * @see #getList(String, Function, List).
     */
    public List<String> getStrList(String field) {
        return getList(field, String::valueOf, null);
    }

    /**
     * Loads a string list, falling back to {@code defaultValue}.
     * @see #getList(String, Function, List).
     */
    public List<String> getStrList(String field, List<String> defaultValue) {
        return getList(field, String::valueOf, defaultValue);
    }

    /**
     * Loads a list, throwing a {@link ConfigKeyMissingException} if it is not provided.
     * @see #getList(String, Function, List).
     */
    public <T> List<T> getList(String field, Function<String, ? extends T> convert) {
        return getList(field, convert, null);
    }

    /**
     * Loads a list of values.
     *
     * Example 1: Get the list or an empty list if not specified
     * {@code
     * List<Double> coefficients = loadList("my.field", Double::parseDouble, Collections.emptyList());
     * }
     *
     * Example 2: Get the list, or throw a {@link ConfigKeyMissingException} if it is not specified.
     * {@code
     * List<Double> coefficients = loadList("my.field", Double::parseDouble);
     * }
     *
     * @param field the property name
     * @param defaultValue the default value, if null the property is considered non-optional
     * @param convert A function mapping CSV element |--> type {@link T}
     * @return A list of {@link T}
     * @param <T> The type of elements in the list
     * @throws ConfigKeyMissingException if {@code defaultValue} is null and the property does not exist
     * @throws ParseException if a CSV element could not be converted to {@link T}
     */
    public <T> List<T> getList(String field, Function<String, ? extends T> convert, List<T> defaultValue) {
        Function<String, List<T>> fn = s -> ConfigConverters.splitCsv(s, convert);
        return loadProp(field, fn, defaultValue, "csv list");
    }

    public Map<String, String> rawMap() {
        return Collections.unmodifiableMap(props);
    }

    public boolean containsKey(String key) {
        return props.containsKey(key);
    }

    public Set<Map.Entry<String, String>> entrySet() {
        return props.entrySet();
    }

    public Set<String> keySet() {
        return props.keySet();
    }

    public void forEach(BiConsumer<? super String, ? super String> action) {
        props.forEach(action);
    }

    public int size() {
        return props.size();
    }

    public boolean isEmpty() {
        return props.isEmpty();
    }

    protected <T> T loadProp(String field, Function<String, ? extends T> convert, T defaultValue, String typeName) {
        String value = props.get(field);
        // option 1: provided a value
        if (value != null && !value.isBlank()) {
            T finalValue;
            try {
                finalValue = convert.apply(value);
            } catch (NumberFormatException e) {
                throw new ConfigValueConversionException(
                    "Could not parse " + typeName + " value '" + value + "' from property " + field + ".", e
                );
            }
            logger.info("Setting property {} to '{}'.", field, value);
            return finalValue;
        }
        // option 2: there's a default value
        if (defaultValue != null) {
            logger.warn("Property {} is not in config file. Using default '{}'.", field, defaultValue);
            return defaultValue;
        }
        // option 3: uh-oh, there is no default either
        throw new ConfigKeyMissingException("Missing property " + field);
    }
}
