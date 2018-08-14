package org.rcsb.common.jsonschema2pojo.rules;

import com.sun.codemodel.JAnnotationUse;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JMethod;
import org.jsonschema2pojo.Jackson2Annotator;
import org.jsonschema2pojo.SchemaGenerator;
import org.jsonschema2pojo.SchemaMapper;
import org.jsonschema2pojo.SchemaStore;
import org.junit.Test;

import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static junit.framework.TestCase.assertTrue;

/**
 *
 *
 * Created by Yana Valasatava on 8/10/18.
 */
public class CustomRequiredRuleFactoryTest {

    @Test
    public void shouldAddAnnotationToGetters() throws Exception {

        final CustomGenerationConfig config = new CustomGenerationConfig();

        final SchemaMapper mapper = new SchemaMapper(
                new CustomRequiredRuleFactory(config, new Jackson2Annotator(config), new SchemaStore()),
                new SchemaGenerator());

        final URL source = CustomRequiredRuleFactoryTest.class.getResource("/test-schema.json");
        final JCodeModel codeModel = new JCodeModel();
        mapper.generate(codeModel, "MyClass", "com.example", source);

        JDefinedClass jclass = codeModel._getClass("com.example.MyClass");

        List<String> requiredFieldMethods = new ArrayList<>();
        requiredFieldMethods.add("getFoo");

        boolean flag = false;
        for (Iterator<JMethod> methods = jclass.methods().iterator(); methods.hasNext();) {
            JMethod method = methods.next();
            if (requiredFieldMethods.contains(method.name())) {
                for (JAnnotationUse a : method.annotations()) {
                    if (a.getAnnotationClass().name().equals("NotNull")) {
                        flag = true;
                        break;
                    }
                }
            }
        }

        assertTrue(flag);
    }
}
