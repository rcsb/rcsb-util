package org.rcsb.common.jsonschema2pojo.rules;

import com.fasterxml.jackson.databind.JsonNode;
import com.sun.codemodel.*;
import org.jsonschema2pojo.Schema;
import org.jsonschema2pojo.rules.Rule;
import org.jsonschema2pojo.rules.RuleFactory;

import javax.annotation.Nonnull;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Yana Valasatava on 8/13/18.
 */
public class CustomRequiredArrayRule implements Rule<JDefinedClass, JDefinedClass> {

    private final RuleFactory ruleFactory;

    public static final String REQUIRED_COMMENT_TEXT = "\nThis property is not nullable";

    protected CustomRequiredArrayRule(RuleFactory ruleFactory) {
        this.ruleFactory = ruleFactory;
    }

    @Override
    public JDefinedClass apply(String nodeName, JsonNode node, JDefinedClass jclass, Schema schema) {

        List<String> requiredFieldMethods = new ArrayList<>();

        JsonNode properties = schema.getContent().get("properties");

        for (Iterator<JsonNode> iterator = node.elements(); iterator.hasNext(); ) {
            String requiredArrayItem = iterator.next().asText();
            if (requiredArrayItem.isEmpty()) {
                continue;
            }

            JsonNode propertyNode = null;

            if (properties != null) {
                propertyNode = properties.findValue(requiredArrayItem);
            }

            String fieldName = ruleFactory.getNameHelper().getPropertyName(requiredArrayItem, propertyNode);

            JFieldVar field = jclass.fields().get(fieldName);

            if (field == null) {
                continue;
            }

            addJavaDoc(field);

            if (ruleFactory.getGenerationConfig().isIncludeJsr303Annotations()) {
                addNotNullAnnotation(field);
            }

            if (ruleFactory.getGenerationConfig().isIncludeJsr305Annotations()) {
                addNonnullAnnotation(field);
            }

            requiredFieldMethods.add(getGetterName(fieldName, field.type(), node));
            requiredFieldMethods.add(getSetterName(fieldName, node));
        }

        updateGetterSetterAnnotation(jclass, requiredFieldMethods);

        return jclass;
    }

    private void updateGetterSetterAnnotation(JDefinedClass jclass, List<String> requiredFieldMethods) {

        for (Iterator<JMethod> methods = jclass.methods().iterator(); methods.hasNext();) {
            JMethod method = methods.next();
            if (requiredFieldMethods.contains(method.name())) {

                addJavaDoc(method);

                if (ruleFactory.getGenerationConfig().isIncludeJsr303Annotations()) {
                    addNotNullAnnotation(method);
                }

                if (ruleFactory.getGenerationConfig().isIncludeJsr305Annotations()) {
                    addNonnullAnnotation(method);
                }
            }
        }
    }

    private void addNotNullAnnotation(JFieldVar field) {
        field.annotate(NotNull.class);
    }

    private void addNotNullAnnotation(JMethod method) {
        method.annotate(NotNull.class);
    }

    private void addNonnullAnnotation(JFieldVar field) {
        field.annotate(Nonnull.class);
    }

    private void addNonnullAnnotation(JMethod method) {
        method.annotate(Nonnull.class);
    }

    private void addJavaDoc(JDocCommentable docCommentable) {
        JDocComment javadoc = docCommentable.javadoc();
        javadoc.append(REQUIRED_COMMENT_TEXT);
    }

    private String getSetterName(String propertyName, JsonNode node) {
        return ruleFactory.getNameHelper().getSetterName(propertyName, node);
    }

    private String getGetterName(String propertyName, JType type, JsonNode node) {
        return ruleFactory.getNameHelper().getGetterName(propertyName, type, node);
    }
}
