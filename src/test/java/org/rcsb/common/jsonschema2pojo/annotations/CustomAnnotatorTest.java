package org.rcsb.common.jsonschema2pojo.annotations;

import com.sun.codemodel.JAnnotationUse;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JMethod;
import org.jsonschema2pojo.SchemaGenerator;
import org.jsonschema2pojo.SchemaMapper;
import org.jsonschema2pojo.SchemaStore;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.rcsb.common.jsonschema2pojo.config.CustomGenerationConfig;
import org.rcsb.common.jsonschema2pojo.rules.CustomRuleFactory;
import org.rcsb.common.jsonschema2pojo.rules.CustomRuleFactoryTest;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Yana Valasatava on 8/16/18.
 */
public class CustomAnnotatorTest {

    private static JCodeModel codeModel;

    @BeforeClass
    public static void generate() throws IOException{

        final URL source = CustomRuleFactoryTest.class.getResource("/test-schema.json");
        CustomGenerationConfig config = new CustomGenerationConfig();

        final SchemaMapper mapper = new SchemaMapper(
                new CustomRuleFactory(config, new CustomAnnotator(config), new SchemaStore()),
                new SchemaGenerator());

        codeModel = new JCodeModel();
        mapper.generate(codeModel, "MyClass", "com.example", source);
    }

    @Test
    public void shouldAddDescriptionToGetterSetter() {

        List<String> expected = Arrays.asList("getFoo", "setFoo");
        JDefinedClass jclass = codeModel._getClass("com.example.MyClass");

        List<String> actuals = new ArrayList<>();
        for (Iterator<JMethod> methods = jclass.methods().iterator(); methods.hasNext();) {
            JMethod method = methods.next();
            for (JAnnotationUse a : method.annotations()) {
                if (a.getAnnotationClass().name().equals("JsonPropertyDescription"))
                    actuals.add(method.name());
            }
        }

        Assert.assertArrayEquals(expected.toArray(), actuals.toArray());
    }
}
