package org.rcsb.common.jsonschema2pojo.rules;

import org.jsonschema2pojo.DefaultGenerationConfig;

/**
 * Overrides the default configuration of jsonschema2pojo tool to include include JSR-303/349 annotations
 * in generated Java types
 *
 * Created by Yana Valasatava on 8/10/18.
 */
public class CustomGenerationConfig extends DefaultGenerationConfig {

    @Override
    public boolean isIncludeJsr303Annotations() {
        return true;
    }
}