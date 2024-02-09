package org.rcsb.common.config;

import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public interface ConfigMap {

    /**
     * Returns a new ConfigMap containing only keys that begin with {@code prefix}.
     */
    ConfigMapImpl subsetByKeyPrefix(String prefix);

    /**
     * Returns a new ConfigMap containing only keys that match the regex pattern {@code pattern}.
     */
    ConfigMapImpl subsetByRegex(String pattern);

    /**
     * Returns a new ConfigMap containing only keys that match the regex pattern {@code pattern}.
     */
    ConfigMapImpl subsetByRegex(Pattern pattern);

    /**
     * Returns a new ConfigMap containing only properties where {@code predicate(key, value)} is {@code true}.
     */
    ConfigMapImpl subset(Predicate<? super Map.Entry<String, String>> predicate);

    /**
     * @see ConfigConverters#convertPath(String)
     */
    Path getPath(String field);

    /**
     * @see ConfigConverters#convertExtantFile(String)
     */
    Path getExtantFile(String field);

    /**
     * @see ConfigConverters#convertNonextantPath(String)
     */
    Path getNonextantPath(String field);

    /**
     * @see ConfigConverters#convertUri(String)
     */
    URI getUri(String field);

    /**
     * @see ConfigConverters#convertUrl(String)
     */
    URL getUrl(String field);

    boolean getBool(String field);

    boolean getBool(String field, boolean defaultValue);

    double getDouble(String field);

    double getDouble(String field, double defaultValue);

    int getInt(String field);

    int getInt(String field, int defaultValue);

    long getLong(String field);

    long getLong(String field, long defaultValue);

    String getStr(String field);

    String getStr(String field, String defaultValue);

    /**
     * Loads a value, throwing a {@link ConfigKeyMissingException} if it is not provided.
     *
     * @see #get(String, Function, T).
     */
    <T> T get(String field, Function<String, ? extends T> convert);

    /**
     * Example 1: Get the double or 0.0 if not specified
     * {@code double coefficients = load("my.field", Double::parseDouble, 0.0); }
     * <p>
     * Example 2: Get the double, or throw a {@link ConfigKeyMissingException} if it is not specified.
     * {@code double coefficients = loadList("my.field", Double::parseDouble); }
     *
     * @param field the property name
     * @param defaultValue the default value, if null the property is considered non-optional
     * @param convert A function mapping the read string value |--> type {@link T}
     * @param <T> The type of the returned value
     * @return A list of {@link T}
     * @throws ConfigKeyMissingException if {@code defaultValue} is null and the property does not exist
     * @throws ParseException if the property could not be converted to {@link T}
     */
    <T> T get(String field, Function<String, ? extends T> convert, T defaultValue);

    double[] getDoubleArray(String field);

    double[] getDoubleArray(String field, double[] defaultValue);

    int[] getIntArray(String field);

    int[] getIntArray(String field, int[] defaultValue);

    long[] getLongArray(String field);

    long[] getLongArray(String field, long[] defaultValue);

    String[] getStrArray(String field);

    String[] getStrArray(String field, String[] defaultValue);

    Set<String> getStrSet(String field);

    Set<String> getStrSet(String field, Set<String> defaultValue);

    /**
     * Loads a string list, throwing a {@link ConfigKeyMissingException} if it is not provided.
     *
     * @see #getList(String, Function, List).
     */
    List<String> getStrList(String field);

    /**
     * Loads a string list, falling back to {@code defaultValue}.
     *
     * @see #getList(String, Function, List).
     */
    List<String> getStrList(String field, List<String> defaultValue);

    <T> Set<T> getSet(String field, Function<String, ? extends T> convert);

    /**
     * Loads a list, throwing a {@link ConfigKeyMissingException} if it is not provided.
     *
     * @see #getList(String, Function, List).
     */
    <T> List<T> getList(String field, Function<String, ? extends T> convert);

    <T> Set<T> getSet(String field, Function<String, ? extends T> convert, Set<? extends T> defaultValue);

    /**
     * Loads a list of values.
     * <p>
     * Example 1: Get the list or an empty list if not specified
     * {@code List<Double> coefficients = loadList("my.field", Double::parseDouble, Collections.emptyList()); }
     * <p>
     * Example 2: Get the list, or throw a {@link ConfigKeyMissingException} if it is not specified.
     * {@code List<Double> coefficients = loadList("my.field", Double::parseDouble); }
     *
     * @param field the property name
     * @param defaultValue the default value, if null the property is considered non-optional
     * @param convert A function mapping CSV element |--> type {@link T}
     * @param <T> The type of elements in the list
     * @return A list of {@link T}
     * @throws ConfigKeyMissingException if {@code defaultValue} is null and the property does not exist
     * @throws ParseException if a CSV element could not be converted to {@link T}
     */
    <T> List<T> getList(String field, Function<String, ? extends T> convert, List<T> defaultValue);

    Map<String, String> rawMap();

    boolean containsKey(String key);

    Set<Map.Entry<String, String>> entrySet();

    Set<String> keySet();

    void forEach(BiConsumer<? super String, ? super String> action);

    int size();

    boolean isEmpty();
}
