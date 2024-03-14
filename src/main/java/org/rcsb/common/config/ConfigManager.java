package org.rcsb.common.config;

import java.net.URL;
import java.nio.file.Path;

/**
 * A utility that finds and reads config profiles.
 *
 * @author Douglas Myers-Turnbull
 * @since 2.0.0
 */
public interface ConfigManager {

    /**
     * Reads the {@code .properties} file at URL {@code urlOrPath}.
     *
     * @param urlOrPath Must start with {@code https://}, {@code http://}, or {@code file://}.
     * @throws ConfigProfileException If the URL is malformed or the profile could not be read
     * @see #read(URL)
     */
    ConfigMap read(String urlOrPath);

    /**
     * @throws ConfigProfileException If the profile could not be read
     * @see #read(URL)
     */
    ConfigMap read(Path path);

    /**
     * Reads a properties file from a URL.
     *
     * @throws ConfigProfileException If the profile could not be read
     */
    ConfigMap read(URL url);

    /**
     * Converts a profile URL string into a URL, checking that it can contact the URL by HTTP or file access.
     *
     * @throws ConfigProfileException If the URL is invalid or inaccessible/unreadable
     */
    URL validate(String profile);
}
