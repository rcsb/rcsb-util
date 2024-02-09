package org.rcsb.common.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
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
public class ConfigMapImpl implements ConfigMap {

    private static final Pattern csvPattern = Pattern.compile(",\\s*");
    private static final Logger logger = LoggerFactory.getLogger(ConfigMapImpl.class);
    private final Map<String, String> props;

    protected ConfigMapImpl(Map<?, ?> props) {
        this.props = props.entrySet()
            .stream()
            .collect(Collectors.toMap(e -> e.getKey().toString(), e -> e.getValue().toString()));
    }

    @Override
    public ConfigMapImpl subsetByKeyPrefix(String prefix) {
        return subset(e -> e.getKey().startsWith(prefix));
    }

    @Override
    public ConfigMapImpl subsetByRegex(String pattern) {
        return subset(e -> Pattern.compile(pattern).matcher(e.getKey().toString()).matches());
    }

    @Override
    public ConfigMapImpl subsetByRegex(Pattern pattern) {
        return subset(e -> pattern.matcher(e.getKey().toString()).matches());
    }

    @Override
    public ConfigMapImpl subset(Predicate<? super Map.Entry<String, String>> predicate) {
        return new ConfigMapImpl(
            props.entrySet()
                .stream()
                .filter(predicate)
                .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue))
        );
    }

    @Override
    public Path getPath(String field) {
        return get(field, ConfigConverters::convertPath);
    }

    @Override
    public Path getExtantFile(String field) {
        return get(field, ConfigConverters::convertExtantFile);
    }

    @Override
    public Path getNonextantPath(String field) {
        return get(field, ConfigConverters::convertNonextantPath);
    }

    @Override
    public URI getUri(String field) {
        return get(field, ConfigConverters::convertUri);
    }

    @Override
    public URL getUrl(String field) {
        return get(field, ConfigConverters::convertUrl);
    }

    @Override
    public boolean getBool(String field) {
        return get(field, Boolean::parseBoolean);
    }

    @Override
    public boolean getBool(String field, boolean defaultValue) {
        return get(field, Boolean::parseBoolean, defaultValue);
    }

    @Override
    public double getDouble(String field) {
        return get(field, Double::parseDouble);
    }

    @Override
    public double getDouble(String field, double defaultValue) {
        return get(field, Double::parseDouble, defaultValue);
    }

    @Override
    public int getInt(String field) {
        return get(field, Integer::parseInt);
    }

    @Override
    public int getInt(String field, int defaultValue) {
        return get(field, Integer::parseInt, defaultValue);
    }

    @Override
    public long getLong(String field) {
        return get(field, Long::parseLong);
    }

    @Override
    public long getLong(String field, long defaultValue) {
        return get(field, Long::parseLong, defaultValue);
    }

    @Override
    public String getStr(String field) {
        return get(field, String::valueOf);
    }

    @Override
    public String getStr(String field, String defaultValue) {
        return get(field, String::valueOf, defaultValue);
    }

    @Override
    public <T> T get(String field, Function<String, ? extends T> convert) {
        return get(field, convert, null);
    }

    @Override
    public <T> T get(String field, Function<String, ? extends T> convert, T defaultValue) {
        String value = loadProp(field, String::toString, null, "string");
        try {
            return convert.apply(value);
        } catch (RuntimeException e) {
            throw new ConfigValueConversionException("Could not parse property " + field + " value '" + value + "'", e);
        }
    }

    // arrays of primitive types are a little tricky, so we provide convenience methods below

    @Override
    public double[] getDoubleArray(String field) {
        return getDoubleArray(field, null);
    }

    @Override
    public double[] getDoubleArray(String field, double[] defaultValue) {
        var defaults = defaultValue == null ?
            null
            : DoubleStream.of(defaultValue).boxed().collect(Collectors.toUnmodifiableList());
        // parse values inside loadProp (calling mapToLong after) so that we can throw a ConfigParseException
        return getList(field, Double::parseDouble, defaults).stream().mapToDouble(Double::valueOf).toArray();
    }

    @Override
    public int[] getIntArray(String field) {
        return getIntArray(field, null);
    }

    @Override
    public int[] getIntArray(String field, int[] defaultValue) {
        var defaults = defaultValue == null ?
            null :
            IntStream.of(defaultValue).boxed().collect(Collectors.toUnmodifiableList());
        // parse values inside loadProp (calling mapToLong after) so that we can throw a ConfigParseException
        return getList(field, Integer::parseInt, defaults).stream().mapToInt(Integer::valueOf).toArray();
    }

    @Override
    public long[] getLongArray(String field) {
        return getLongArray(field, null);
    }

    @Override
    public long[] getLongArray(String field, long[] defaultValue) {
        var defaults = defaultValue == null ?
            null :
            LongStream.of(defaultValue).boxed().collect(Collectors.toUnmodifiableList());
        // parse values inside loadProp (calling mapToLong after) so that we can throw a ConfigParseException
        return getList(field, Long::parseLong, defaults).stream().mapToLong(Long::valueOf).toArray();
    }

    @Override
    public String[] getStrArray(String field) {
        return getStrList(field).toArray(String[]::new);
    }

    @Override
    public String[] getStrArray(String field, String[] defaultValue) {
        if (defaultValue == null) {
            return getStrList(field).toArray(String[]::new);
        }
        return getStrList(field, Arrays.asList(defaultValue)).toArray(String[]::new);
    }

    @Override
    public List<String> getStrList(String field) {
        return getList(field, String::valueOf, null);
    }

    @Override
    public List<String> getStrList(String field, List<String> defaultValue) {
        return getList(field, String::valueOf, defaultValue);
    }

    @Override
    public <T> Set<T> getSet(String field, Function<String, ? extends T> convert) {
        return getList(field, convert, null).stream().collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public Set<String> getStrSet(String field) {
        return getStrList(field, null).stream().collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public Set<String> getStrSet(String field, Set<String> defaultValue) {
        return getStrList(field, new ArrayList<>(defaultValue)).stream().collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public <T> List<T> getList(String field, Function<String, ? extends T> convert) {
        return getList(field, convert, null);
    }

    @Override
    public <T> Set<T> getSet(String field, Function<String, ? extends T> convert, Set<? extends T> defaultValue) {
        return getList(field, convert, new ArrayList<>(defaultValue)).stream().collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public <T> List<T> getList(String field, Function<String, ? extends T> convert, List<T> defaultValue) {
        Function<String, List<T>> fn = s -> ConfigConverters.splitCsv(s, convert);
        return loadProp(field, fn, defaultValue, "csv list");
    }

    @Override
    public Map<String, String> rawMap() {
        return Collections.unmodifiableMap(props);
    }

    @Override
    public boolean containsKey(String key) {
        return props.containsKey(key);
    }

    @Override
    public Set<Map.Entry<String, String>> entrySet() {
        return props.entrySet();
    }

    @Override
    public Set<String> keySet() {
        return props.keySet();
    }

    @Override
    public void forEach(BiConsumer<? super String, ? super String> action) {
        props.forEach(action);
    }

    @Override
    public int size() {
        return props.size();
    }

    @Override
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
