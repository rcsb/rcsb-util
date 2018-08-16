package org.rcsb.common.jsonschema2pojo.rules;

import org.jsonschema2pojo.DefaultGenerationConfig;

/**
 * Overrides the default configuration of jsonschema2pojo tool.
 *
 * @author Yana Valasatava
 * @since 1.4.0
 */
public class CustomGenerationConfig extends DefaultGenerationConfig {

    @Override
    public boolean isIncludeJsr303Annotations() {
        return true;
    }

    @Override
    public boolean isIncludeToString() {
        return false;
    }

    @Override
    public boolean isInitializeCollections() {
        return false;
    }

    @Override
    public boolean isSerializable() {
        return true;
    }

    @Override
    public boolean isIncludeAdditionalProperties() {
        return false;
    }
}