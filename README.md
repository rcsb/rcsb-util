# RCSB utilities

Provides configuration management via `.properties` files.


### Usage example

```java
import java.lang.System;
import java.nio.file.Path;
import java.util.Set;
import java.util.Map;
import java.net.URL;

import org.rcsb.common.config.ConfigMap;
import org.rcsb.common.config.Configs;

/**
 * Stores config data.
 */
public record ConfigData(
    Path dataFile,
    URL resource,
    Set<String> keys,
    double[] coefficients,
    Map<String, String> extras
) {}

/**
 * Singleton that reads the config from a system property path.
 */
public final class ConfigSingleton {

    // better to be greedy and fail early
    private ConfigData instance = create();

    public ConfigData get() {
        return instance;
    }

    private static void create() {
        String systemVar = System.getProperty("project_config_path");
        ConfigMap config = Configs.manager().read(systemVar);
        instance = new ConfigData(
            config.getExtantFile("project.data-file", "/var/lib/data.xml"),
            config.getUrl("project.resource-url"),
            config.getStrSet("project.keys"),
            config.getOptionalStr("project.xx"),  // Optional<String>
            config.getLazy("project.yy", Path::of), // convert manually
            config.getLazy("project.zz", Path::of, () -> null), // use a Supplier
            config.getDoubleArray("project.coeffs", new double[]{1.0}),
            config.subsetByKeyPrefix("project.extras").rawMap()
        );
    }
}
```

### Make a config from a map

```java
import java.util.Map;
import org.rcsb.common.config.Configs;
import org.rcsb.common.config.ConfigValueConversionException;

public class CacheHandler {

    private static boolean useCache(Map<String, String> data) {
        var props = Configs.mapOf(data);
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
