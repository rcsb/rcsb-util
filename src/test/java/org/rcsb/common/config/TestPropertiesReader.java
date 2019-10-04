package org.rcsb.common.config;

import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.Properties;

import static org.junit.Assert.assertEquals;

public class TestPropertiesReader {

    @BeforeClass
    public static void setUrl() {
        String tmpDir = System.getProperty("java.io.tmpdir");
        System.setProperty(ConfigProfileManager.CONFIG_PROFILE_PROPERTY, "file://" + tmpDir);
    }

    private static final String CONFIG_FILE = "/test_config.properties";
    @Test
    public void testReadIntArrayGoodInput() throws IOException {
        Properties props = new Properties();
        props.load(TestPropertiesReader.class.getResourceAsStream(CONFIG_FILE));
        PropertiesReader propsReader = new PropertiesReader(props, CONFIG_FILE);
        int[] array = propsReader.loadIntArrayField("my.int.array.field");
        assertEquals(10, array.length);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testReadIntArrayBadInput() throws IOException {
        Properties props = new Properties();
        props.load(TestPropertiesReader.class.getResourceAsStream(CONFIG_FILE));
        PropertiesReader propsReader = new PropertiesReader(props, CONFIG_FILE);
        propsReader.loadIntArrayField("my.bad.int.array.field");
    }

    @Test
    public void testReadDoubleArray() throws IOException  {
        Properties props = new Properties();
        props.load(TestPropertiesReader.class.getResourceAsStream(CONFIG_FILE));
        PropertiesReader propsReader = new PropertiesReader(props, CONFIG_FILE);
        double[] array = propsReader.loadDoubleArrayField("my.double.array.field");
        assertEquals(11, array.length);
    }

    @Test
    public void testReadDouble() throws IOException {
        Properties props = new Properties();
        props.load(TestPropertiesReader.class.getResourceAsStream(CONFIG_FILE));
        PropertiesReader propsReader = new PropertiesReader(props, CONFIG_FILE);
        double d = propsReader.loadDoubleField("my.double.field", null);
        assertEquals(2.345, d, 0.000001);
        d = propsReader.loadDoubleField("my.double.scientific.field", null);
        assertEquals(2.345E-129, d, 0.000001);
    }

    @Test
    public void testReadInteger() throws IOException {
        Properties props = new Properties();
        props.load(TestPropertiesReader.class.getResourceAsStream(CONFIG_FILE));
        PropertiesReader propsReader = new PropertiesReader(props, CONFIG_FILE);
        int i = propsReader.loadIntegerField("my.int.field", null);
        assertEquals(56790, i);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testReadIntegerBadInput() throws IOException {
        Properties props = new Properties();
        props.load(TestPropertiesReader.class.getResourceAsStream(CONFIG_FILE));
        PropertiesReader propsReader = new PropertiesReader(props, CONFIG_FILE);
        propsReader.loadIntegerField("my.bad.int.field", null);
    }

    @Test
    public void testReadString() throws IOException {
        Properties props = new Properties();
        props.load(TestPropertiesReader.class.getResourceAsStream(CONFIG_FILE));
        PropertiesReader propsReader = new PropertiesReader(props, CONFIG_FILE);
        String s = propsReader.loadStringField("my.string.field", null);
        assertEquals("abcde", s);
    }
}