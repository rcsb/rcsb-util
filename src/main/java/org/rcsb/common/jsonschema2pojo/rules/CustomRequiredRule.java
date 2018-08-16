package org.rcsb.common.jsonschema2pojo.rules;

import com.fasterxml.jackson.databind.JsonNode;
import com.sun.codemodel.JDocCommentable;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JMethod;
import org.jsonschema2pojo.Schema;
import org.jsonschema2pojo.rules.Rule;
import org.jsonschema2pojo.rules.RuleFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

/**
 * Applies the custom "required" schema rule that attaches annotations to the fields as well as getters/setters.
 *
 * @see <a
 *  href="https://tools.ietf.org/html/draft-zyp-json-schema-03#section-5.7">https://tools.ietf.org/html/draft-zyp-json-schema-03#section-5.7</a>
 *
 * @author Yana Valasatava
 * @since 1.4.0
 */
public class CustomRequiredRule implements Rule<JDocCommentable, JDocCommentable> {

    /**
     * Text added to JavaDoc to indicate that a field is not required
     */
    public static final String REQUIRED_COMMENT_TEXT = "\nThis property is not nullable";

    private final RuleFactory ruleFactory;

    protected CustomRequiredRule(RuleFactory ruleFactory) {
        this.ruleFactory = ruleFactory;
    }

    @Override
    public JDocCommentable apply(String s, JsonNode node, JDocCommentable generatableType, Schema schema) {

        if (node.asBoolean()) {

            generatableType.javadoc().append(REQUIRED_COMMENT_TEXT);

            if (ruleFactory.getGenerationConfig().isIncludeJsr303Annotations()) {
                if (generatableType instanceof JFieldVar)
                    ((JFieldVar) generatableType).annotate(NotNull.class);
                else if (generatableType instanceof JMethod)
                    ((JMethod) generatableType).annotate(NotNull.class);

            } else if (ruleFactory.getGenerationConfig().isIncludeJsr305Annotations()) {
                if (generatableType instanceof JFieldVar)
                    ((JFieldVar) generatableType).annotate(Nonnull.class);
                else if (generatableType instanceof JMethod)
                    ((JMethod) generatableType).annotate(Nonnull.class);
            }

        } else {
            if (ruleFactory.getGenerationConfig().isIncludeJsr305Annotations()) {
                if (generatableType instanceof JFieldVar)
                    ((JFieldVar) generatableType).annotate(Nullable.class);
                else if (generatableType instanceof JMethod)
                    ((JMethod) generatableType).annotate(Nullable.class);
            }
        }

        return generatableType;
    }
}
