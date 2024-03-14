package org.rcsb.common.config;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

public class ConfigManagerImpl implements ConfigManager {

    private static final Logger logger = LoggerFactory.getLogger(ConfigManagerImpl.class);

    @Override
    public ConfigMap read(String urlOrPath) {
        if (urlOrPath.startsWith("https://")) {
            // convert to a URL
            return read(validate(urlOrPath));
        }
        if (urlOrPath.startsWith("http://")) {
            // convert to a URL
            logger.warn("Accessing insecure http URL {}", urlOrPath);
            return read(validate(urlOrPath));
        }
        // can't be a URL, so try converting to Path
        return read(Paths.get(urlOrPath));
    }

    @Override
    public ConfigMap read(Path path) {
        try {
            return read(path.toUri().toURL());
        } catch (MalformedURLException e) {
            throw new ConfigProfileException("The path '" + path + "' could not be converted to a URL.", e);
        }
    }

    @Override
    public ConfigMap read(URL url) {
        validate(url.toExternalForm());
        Properties props = new Properties();
        try (InputStream stream = url.openStream()) {
            props.load(url.openStream());
        } catch (IOException e) {
            throw new ConfigProfileException(
                "Could not read config at '" + url + "', although it was reported to exist / be reachable.", e
            );
        }
        return toMap(props);
    }

    @Override
    public URL validate(String profile) {
        if (profile == null) {
            throw new ConfigProfileException("Config profile is null");
        }
        if (profile.isBlank()) {
            throw new ConfigProfileException("Config profile is blank");
        }
        final URL url;
        try {
            url = new URL(profile);
        } catch (MalformedURLException e) {
            throw new ConfigProfileException("Could not convert profile '" + profile + "' to URL", e);
        }
        try {
            checkThatUrlIsReadable(url);
        } catch (IllegalArgumentException e) {
            throw new ConfigProfileException("The config profile '" + url + "' is invalid.", e);
        } catch (IOException e) {
            throw new ConfigProfileException("The config profile '" + url + "' could not be read.", e);
        }
        return url;
    }

    /**
     * Checks the file exists (if a file URL), or runs an HTTP HEAD and requires a 200 (HTTP).
     * @param url A URL with a {@code https} or {@code file} protocol
     *
     * @throws IllegalArgumentException If the URL is invalid or neither https nor file.
     * @throws IOException If it's not an extant file, the connection failed, the server didn't respond, or
     * the server returned a non-200 status code.
     * @throws SecurityException If this JVM lacks sufficient privileges
     */
    private void checkThatUrlIsReadable(URL url) throws IOException {
        logger.info("URL {} has protocol {}", url, url.getProtocol());
        if ("file".equals(url.getProtocol())) {
            Path path;
            try {
                path = Paths.get(url.toURI());
            } catch (IllegalArgumentException | URISyntaxException ignored) {
                throw new IllegalArgumentException(
                    "Could not convert URL '" + url + "' to file: " + ignored.getMessage()
                );
            }
            if (!Files.isRegularFile(path)) {
                throw new IOException("Path '" + path + "' does not exist or is not a regular file.");
            }
        } else if ("https".equals(url.getProtocol())) {
            int code;
            try {
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("HEAD");
                code = connection.getResponseCode();
            } catch (IOException e) {
                throw new IOException("Failed to get an HTTP reponse from '" + url + "'", e);
            }
            if (code != 200) {
                throw new IOException("HTTP HEAD to " + url + " returned status code " + code + ", not 200.");
            }
        } else {
            throw new IllegalArgumentException("URL '" + url + "' is neither https nor file.");
        }
    }

    protected ConfigMap toMap(Map<?, ?> props) {
        return new ConfigMapImpl(props.entrySet().stream()
            .collect(Collectors.toMap(entry -> (String) entry.getKey(),
                entry -> (String) entry.getValue(),
                (a, b) -> b
            )));
    }
}
