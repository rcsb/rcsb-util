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
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.*;

public class ConfigMapImpl implements ConfigMap {

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
    public Path getPath(String field, Path fallback) {
        return get(field, ConfigConverters::convertPath, fallback);
    }

    @Override
    public Optional<Path> getOptionalPath(String field) {
        return Optional.ofNullable(getLazy(field, ConfigConverters::convertPath, () -> null));
    }

    @Override
    public Path getExtantFile(String field) {
        return get(field, ConfigConverters::convertExtantFile);
    }

    @Override
    public Path getExtantFile(String field, Path fallback) {
        return get(field, ConfigConverters::convertExtantFile, fallback);
    }

    @Override
    public Path getNonextantPath(String field) {
        return get(field, ConfigConverters::convertNonextantPath);
    }

    @Override
    public Path getNonextantPath(String field, Path fallback) {
        return get(field, ConfigConverters::convertNonextantPath, fallback);
    }

    @Override
    public Path getDirectory(String field) {
        return get(field, ConfigConverters::convertDirectory);
    }

    @Override
    public Path getDirectory(String field, Path fallback) {
        return get(field, ConfigConverters::convertDirectory, fallback);
    }

    @Override
    public Path getExtantDirectory(String field) {
        return get(field, ConfigConverters::convertExtantDirectory);
    }

    @Override
    public Path getExtantDirectory(String field, Path fallback) {
        return get(field, ConfigConverters::convertExtantDirectory, fallback);
    }

    @Override
    public URI getUri(String field) {
        return get(field, ConfigConverters::convertUri);
    }

    @Override
    public Optional<URI> getOptionalUri(String field) {
        return Optional.ofNullable(getLazy(field, ConfigConverters::convertUri, () -> null));
    }

    @Override
    public URL getUrl(String field) {
        return get(field, ConfigConverters::convertUrl);
    }

    @Override
    public Optional<URL> getOptionalUrl(String field) {
        return Optional.ofNullable(getLazy(field, ConfigConverters::convertUrl, () -> null));
    }

    @Override
    public boolean getBool(String field) {
        return get(field, Boolean::parseBoolean);
    }

    @Override
    public Optional<Boolean> getOptionalBool(String field) {
        return Optional.ofNullable(getLazy(field, Boolean::parseBoolean, () -> null));
    }

    @Override
    public boolean getBool(String field, boolean fallback) {
        return get(field, Boolean::parseBoolean, fallback);
    }

    @Override
    public double getDouble(String field) {
        return get(field, Double::parseDouble);
    }

    @Override
    public Optional<Double> getOptionalDouble(String field) {
        return Optional.ofNullable(getLazy(field, Double::parseDouble, () -> null));
    }

    @Override
    public double getDouble(String field, double fallback) {
        return get(field, Double::parseDouble, fallback);
    }

    @Override
    public int getInt(String field) {
        return get(field, Integer::parseInt);
    }

    @Override
    public Optional<Integer> getOptionalInt(String field) {
        return Optional.ofNullable(getLazy(field, Integer::parseInt, () -> null));
    }

    @Override
    public int getInt(String field, int fallback) {
        return get(field, Integer::parseInt, fallback);
    }

    @Override
    public long getLong(String field) {
        return get(field, Long::parseLong);
    }

    @Override
    public Optional<Long> getOptionalLong(String field) {
        return Optional.ofNullable(getLazy(field, Long::parseLong, () -> null));
    }

    @Override
    public long getLong(String field, long fallback) {
        return get(field, Long::parseLong, fallback);
    }

    @Override
    public String getStr(String field) {
        return get(field, String::valueOf);
    }

    @Override
    public Optional<String> getOptionalString(String field) {
        return Optional.ofNullable(getLazy(field, String::valueOf, () -> null));
    }

    @Override
    public String getStr(String field, String fallback) {
        return get(field, String::valueOf, fallback);
    }

    @Override
    public <T> T get(String field, Function<String, ? extends T> convert) {
        return get(field, convert, null);
    }

    @Override
    public <T> Optional<T> getOptional(String field, Function<String, ? extends T> convert) {
        return Optional.ofNullable(get(field, convert, null));
    }

    @Override
    public <T> T get(String field, Function<String, ? extends T> convert, T fallback) {
        return loadProp(field, convert, fallback, "string");
    }

    @Override
    public <T> T getLazy(String field, Function<String, ? extends T> convert, Supplier<T> fallback) {
        return loadProp(field, convert, fallback, "string", true);
    }

    // arrays of primitive types are a little tricky, so we provide convenience methods below

    @Override
    public double[] getDoubleArray(String field) {
        return getDoubleArray(field, null);
    }

    @Override
    public double[] getDoubleArray(String field, double[] fallback) {
        var defaults = Optional.ofNullable(fallback)
            .map(value -> DoubleStream.of(value).boxed().collect(Collectors.toUnmodifiableList()))
            .orElse(null);
        // parse values inside loadProp (calling mapToLong after) so that we can throw a ConfigParseException
        return getList(field, Double::parseDouble, defaults).stream().mapToDouble(Double::valueOf).toArray();
    }

    @Override
    public int[] getIntArray(String field) {
        return getIntArray(field, null);
    }

    @Override
    public int[] getIntArray(String field, int[] fallback) {
        var defaults = Optional.ofNullable(fallback)
            .map(value -> IntStream.of(value).boxed().collect(Collectors.toUnmodifiableList()))
            .orElse(null);
        // parse values inside loadProp (calling mapToLong after) so that we can throw a ConfigParseException
        return getList(field, Integer::parseInt, defaults).stream().mapToInt(Integer::valueOf).toArray();
    }

    @Override
    public long[] getLongArray(String field) {
        return getLongArray(field, null);
    }

    @Override
    public long[] getLongArray(String field, long[] fallback) {
        var defaults = Optional.ofNullable(fallback)
            .map(value -> LongStream.of(value).boxed().collect(Collectors.toUnmodifiableList()))
            .orElse(null);
        // parse values inside loadProp (calling mapToLong after) so that we can throw a ConfigParseException
        return getList(field, Long::parseLong, defaults).stream().mapToLong(Long::valueOf).toArray();
    }

    @Override
    public String[] getStrArray(String field) {
        return getStrList(field).toArray(String[]::new);
    }

    @Override
    public String[] getStrArray(String field, String[] fallback) {
        if (fallback == null) {
            return getStrList(field).toArray(String[]::new);
        }
        return getStrList(field, Arrays.asList(fallback)).toArray(String[]::new);
    }

    @Override
    public List<String> getStrList(String field) {
        return getList(field, String::valueOf, null);
    }

    @Override
    public List<String> getStrList(String field, List<String> fallback) {
        return getList(field, String::valueOf, fallback);
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
    public Set<String> getStrSet(String field, Set<String> fallback) {
        return getStrList(field, new ArrayList<>(fallback)).stream().collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public <T> List<T> getList(String field, Function<String, ? extends T> convert) {
        return getList(field, convert, null);
    }

    @Override
    public <T> Set<T> getSet(String field, Function<String, ? extends T> convert, Set<? extends T> fallback) {
        return getList(field, convert, new ArrayList<>(fallback)).stream().collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public <T> List<T> getList(String field, Function<String, ? extends T> convert, List<T> fallback) {
        Function<String, List<T>> fn = s -> ConfigConverters.splitCsv(s, convert);
        return loadProp(field, fn, fallback, "csv list");
    }

    @Override
    public <T> List<T> getListLazy(String field, Function<String, ? extends T> convert, Supplier<List<T>> fallback) {
        Function<String, List<T>> fn = s -> ConfigConverters.splitCsv(s, convert);
        return loadProp(field, fn, fallback, "csv list", true);
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
    public boolean has(String key) {
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

    protected <T> T loadProp(String field, Function<String, ? extends T> convert, T fallback, String typeName) {
        return loadProp(field, convert, () -> fallback, typeName, false);
    }

    protected <T> T loadProp(
        String field,
        Function<String, ? extends T> convert,
        Supplier<T> fallback,
        String typeName,
        boolean defaultIsNullable
    ) {
        String value = props.get(field);
        // option 1: provided a value
        if (value != null && !value.isBlank()) {
            T finalValue;
            try {
                finalValue = convert.apply(value);
            } catch (RuntimeException e) {
                throw new ConfigValueConversionException(
                    "Could not parse " + typeName + " value '" + value + "' from property " + field + ".", e
                );
            }
            logger.info("Setting property {} to '{}'.", field, value);
            return finalValue;
        }
        // option 2: there's a default value
        T fellBack = fallback.get();
        if (defaultIsNullable || fellBack != null) {
            logger.warn("Property {} is not in config file. Using default '{}'.", field, fellBack);
            return fellBack;
        }
        // option 3: uh-oh, there is no default either
        throw new ConfigKeyMissingException("Missing property " + field);
    }

}
