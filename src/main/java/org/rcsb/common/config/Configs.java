package org.rcsb.common.config;

import java.util.HashMap;
import java.util.Map;

/**
 * Public-facing set of static methods to create instances of {@link org.rcsb.common.config}.
 * Makes {@link ConfigManager ConfigManagers} and {@link ConfigMap ConfigMaps}.
 * <em>Users of this library should generally start with methods here.</em>
 *
 * @author Douglas Myers-Turnbull
 * @since 2.0.0
 */
public final class Configs {

    private Configs() {}

    public static ConfigManager manager() {
        return new ConfigManagerImpl();
    }

    public static ConfigMap emptyMap() {
        return new ConfigMapImpl(new HashMap<>());
    }

    public static ConfigMap mapOf(Map<?, ?> props) {
        return new ConfigMapImpl(props);
    }

}
