package org.rcsb.common.config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * {@link ConfigManager} implementation that supports http/https and file paths.
 *
 * @author Douglas Myers-Turnbull
 * @since 2.0.0
 */
public class ConfigManagerImpl implements ConfigManager {

    @Override
    public ConfigMap read(String where) {
        if (where == null) {
            throw new ConfigProfileException("Config URL/path is null.");
        }
        if (where.isBlank()) {
            throw new ConfigProfileException("Config URL/path is blank.");
        }
        final URL url;
        try {
            if (where.contains(":")) {
                url = new URL(where);
                if (!Set.of("https", "http", "file").contains(url.getProtocol())) {
                    throw new ConfigProfileException(String.format("Unsupported protocol for config URL '%s'.", where));
                }
            } else {
                url = pathToUrl(Paths.get(where));
            }
        } catch (MalformedURLException e) {
            throw new ConfigProfileException(String.format("Config URL/path '%s' is not a valid URL.", where), e);
        }
        return read(url);
    }

    @Override
    public ConfigMap read(Path path) {
        return read(pathToUrl(path));
    }

    @Override
    public ConfigMap read(URL url) {
        var props = new Properties();
        try {
            if (url.getProtocol().equals("file")) {
                try (InputStream stream = url.openStream()) {
                    props.load(stream);
                }
            } else {
                var conn = (HttpURLConnection) url.openConnection();
                int code = conn.getResponseCode();
                if (code != 200) {
                    throw new ConfigProfileException(String.format("Status code %d from config URL '%s'.", code, url));
                }
                try (
                    var br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))
                ) {
                    String body = br.lines().collect(Collectors.joining());
                    props.load(new StringReader(body));
                }
            }
        } catch (IOException e) {
            throw new ConfigProfileException(String.format("Could not read config at '%s'.", url), e);
        }
        return toMap(props);
    }

    protected ConfigMap toMap(Map<?, ?> props) {
        return new ConfigMapImpl(
            props.entrySet().stream()
                .collect(Collectors.toMap(e -> (String) e.getKey(), e -> (String) e.getValue(), (a, b) -> b))
        );
    }

    private static URL pathToUrl(Path path) {
        try {
            return path.toUri().toURL();
        } catch (MalformedURLException e) {
            throw new ConfigProfileException(String.format("Could not convert path '%s' to URL.", path), e);
        }
    }

}
