package org.rcsb.common.config;

import java.io.FileNotFoundException;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A collection of static methods that convert string-typed property values to their correct types.
 * These are meant to be passed as {@code convert} in {@link ConfigMapImpl#getLazy}.
 *
 * @author Douglas Myers-Turnbull
 * @since 2.0.0
 */
public final class ConfigConverters {

    private static final Pattern csvPattern = Pattern.compile(",\\s*");

    private ConfigConverters() {}

    public static List<String> splitCsv(String value) {
        return List.of(csvPattern.split(value));
    }

    public static <T> List<T> splitCsv(String value, Function<String, ? extends T> convert) {
        return Stream.of(csvPattern.split(value)).map(convert).collect(Collectors.toUnmodifiableList());
    }

    /**
     * Converts to a {@link Path}, making sure that the path either does not exist or is a directory.
     * (Prefer for output directories.)
     * @throws UncheckedIOException If the directory exists and is not a directory
     * @throws ConfigValueConversionException If the path is invalid (will have a InvalidPathException as its cause)
     * @see #convertDirectory(String): The directory must exist (prefer for input directories)
     */
    public static Path convertDirectory(String value) {
        try {
            Path path = Paths.get(value);
            if (Files.exists(path) && !Files.isDirectory(path)) {
                throw fnfe("Path '%s' exists but is not a directory.", value);
            }
            return path;
        } catch (InvalidPathException e) {
            throw cvce("Could not parse '%s' as a path.", value, e);
        }
    }

    /**
     * Converts to a {@link Path}, making sure that the path exists and is a directory.
     * (Prefer for input directories.)
     * @throws UncheckedIOException If the directory is not found or not a directory
     * @throws ConfigValueConversionException If the path is invalid (will have a InvalidPathException as its cause)
     * @see #convertDirectory(String): The path can also not exist (prefer for output directories)
     */
    public static Path convertExtantDirectory(String value) {
        Path path = convertDirectory(value);
        if (!Files.isDirectory(path)) {
            throw fnfe("Path '%s' exists but is not a directory.", value);
        }
        return path;
    }

    /**
     * Converts to a Path, making sure it exists, is a regular file, and is readable.
     * @throws UncheckedIOException If the file is not found, not a file, or not readable
     * @throws ConfigValueConversionException If the path is invalid (will have a InvalidPathException as its cause)
     */
    public static Path convertExtantFile(String value) {
        Path path = convertPath(value);
        if (!Files.exists(path)) {
            throw fnfe("Path '%s' does not exist.", value);
        }
        if (!Files.isRegularFile(path)) {
            throw fnfe("Path '%s' exists but is not a regular file.", value);
        }
        if (!Files.isReadable(path)) {
            throw fse("Path '%s' is not readable.", value);
        }
        return path;
    }

    /**
     * Converts to a Path, making sure it does not exist.
     * @throws UncheckedIOException If the path exists
     * @throws ConfigValueConversionException If the path is invalid (will have a InvalidPathException as its cause)
     */
    public static Path convertNonextantPath(String value) {
        try {
            Path path = Paths.get(value);
            if (Files.exists(path)) {
                throw faee("Path '%s' already exists.", value);
            }
            return path;
        } catch (InvalidPathException e) {
            throw cvce("Could not parse '%s' as a path.", value, e);
        }
    }

    /**
     * Converts to a Path.
     * @throws ConfigValueConversionException If the path is invalid (has a InvalidPathException as its cause)
     */
    public static Path convertPath(String value) {
        try {
            return Paths.get(value);
        } catch (InvalidPathException e) {
            throw cvce("Could not parse '%s' as a path.", value, e);
        }
    }

    /**
     * Converts to a URI.
     * @throws ConfigValueConversionException If the URI is invalid (has a URISyntaxException as its cause)
     */
    public static URI convertUri(String value) {
        try {
            return new URI(value);
        } catch (URISyntaxException e) {
            throw cvce("Could not parse '%s' as a URI.", value, e);
        }
    }

    /**
     * Converts to a URL.
     * @throws ConfigValueConversionException If the URL is invalid (has a MalformedURLException as its cause)
     */
    public static URL convertUrl(String value) {
        try {
            return new URL(value);
        } catch (MalformedURLException e) {
            throw cvce("Could not parse '%s' as a URL.", value, e);
        }
    }

    private static UncheckedIOException fnfe(String msg, String value) {
        return new UncheckedIOException(new FileNotFoundException(String.format(msg, value)));
    }

    private static UncheckedIOException faee(String msg, String value) {
        return new UncheckedIOException(new FileAlreadyExistsException(String.format(msg, value)));
    }

    private static UncheckedIOException fse(String msg, String value) {
        return new UncheckedIOException(new FileSystemException(String.format(msg, value)));
    }

    private static ConfigValueConversionException cvce(String msg, String value, Exception e) {
        return new ConfigValueConversionException(String.format(msg, value), e);
    }

}
