package org.rcsb.common.config;

import java.util.HashMap;
import java.util.Map;

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
