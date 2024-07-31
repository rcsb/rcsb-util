# RCSB utilities

Provides configuration management via `.properties` files.

## Usage examples

```java
import java.lang.System;
import java.nio.file.Path;
import java.util.Set;
import java.util.Map;
import java.net.URI;

import org.rcsb.common.config.ConfigMap;
import org.rcsb.common.config.Configs;

/**
 * Configuration.
 */
public record ConfigData(
    Path dataFile,
    URL resource,
    Set<String> keys,
    double[] coefficients,
    Map<String, String> extras
) {}

/**
 * A {@link ConfigData} factory that reads from the filesystem and provides defaults.
 */
public final class ConfigLoader {

    public void load(Path path) {
        ConfigMap config = Configs.manager().read(path);
        return new ConfigData(
            config.getExtantFile("app.data-file", "/var/lib/data.xml"),
            config.getUri("app.resource-url"),
            config.getStrSet("app.keys"),
            config.getOptionalStr("app.xx"),  // Optional<String>
            config.getLazy("app.yy", Path::of), // convert manually
            config.getLazy("app.zz", Path::of, () -> null), // use a Supplier
            config.getDoubleArray("app.coeffs", () -> new double[]{1.0}), // avoid initializing default
            config.subset(e -> e.getKey().startsWith("app.extras")).rawMap()
        );
    }
}

void main() { // https://openjdk.org/jeps/445
    var path = Paths.get(System.getProperty("app_config_path"));
    ConfigMap map = new ConfigFactory().load(path);
}
```

### Make a config from a map

```java
import java.util.Map;
import org.rcsb.common.config.Configs;
import org.rcsb.common.config.ConfigValueConversionException;

public class CacheHandler {

    private static boolean useCache(Map<String, String> data) {
        ConfigMap props = Configs.mapOf(data);
        try {
            return props.getBool("use-cached", false);
        } catch (ConfigValueConversionException ignored) {
            // e.g., 'falsee' -- no worries, we'll rebuild
            // logger.warn();
            return false;
        }
    }
}
```
