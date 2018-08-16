package org.rcsb.common.jsonschema2pojo.annotations;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.databind.JsonNode;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JMethod;
import org.jsonschema2pojo.AbstractAnnotator;
import org.jsonschema2pojo.GenerationConfig;

import java.util.HashMap;
import java.util.Map;

/**
 * Annotates generated Java types using the custom annotations. When used in Maven plugin a fully qualified class name,
 * referring to this instance, should be passed with <customAnnotator> attribute
 * See {@linktourl http://joelittlejohn.github.io/jsonschema2pojo/site/0.5.1/generate-mojo.html#customAnnotator}.
 *
 * @author Yana Valasatava
 * @since 1.4.0
 */
public class CustomAnnotator extends AbstractAnnotator {

    private Map<String, String> description = new HashMap<>();

    public CustomAnnotator(GenerationConfig generationConfig) {
        super(generationConfig);
    }

    public void propertyField(JFieldVar field, JDefinedClass clazz, String propertyName, JsonNode propertyNode) {

        if (propertyNode.has("description")) {
            description.put(propertyName, propertyNode.get("description").asText());
        }
    }

    @Override
    public void propertyGetter(JMethod getter, String propertyName) {
        if (description.containsKey(propertyName))
            getter.annotate(JsonPropertyDescription.class).param("value", description.get(propertyName));
    }

    @Override
    public void propertySetter(JMethod setter, String propertyName) {
        if (description.containsKey(propertyName))
            setter.annotate(JsonPropertyDescription.class).param("value", description.get(propertyName));
    }
}