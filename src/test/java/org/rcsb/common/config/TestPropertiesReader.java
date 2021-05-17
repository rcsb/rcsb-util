package org.rcsb.common.config;

import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

import static org.junit.Assert.assertEquals;

public class TestPropertiesReader {

    private static final String CONFIG_FILE = "/test_config.properties";

    private static URL url;

    @BeforeClass
    public static void setUrl() throws MalformedURLException {
        url = new URL("file:///" + CONFIG_FILE);
    }

    @Test
    public void testReadIntArrayGoodInput() throws IOException {
        Properties props = new Properties();
        props.load(TestPropertiesReader.class.getResourceAsStream(CONFIG_FILE));
        PropertiesReader propsReader = new PropertiesReader(props, url);
        int[] array = propsReader.loadIntArrayField("my.int.array.field", null);
        assertEquals(10, array.length);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testReadIntArrayBadInput() throws IOException {
        Properties props = new Properties();
        props.load(TestPropertiesReader.class.getResourceAsStream(CONFIG_FILE));
        PropertiesReader propsReader = new PropertiesReader(props, url);
        propsReader.loadIntArrayField("my.bad.int.array.field", null);
    }

    @Test
    public void testReadDoubleArray() throws IOException  {
        Properties props = new Properties();
        props.load(TestPropertiesReader.class.getResourceAsStream(CONFIG_FILE));
        PropertiesReader propsReader = new PropertiesReader(props, url);
        double[] array = propsReader.loadDoubleArrayField("my.double.array.field", null);
        assertEquals(11, array.length);
    }

    @Test
    public void testReadDouble() throws IOException {
        Properties props = new Properties();
        props.load(TestPropertiesReader.class.getResourceAsStream(CONFIG_FILE));
        PropertiesReader propsReader = new PropertiesReader(props, url);
        double d = propsReader.loadDoubleField("my.double.field", null);
        assertEquals(2.345, d, 0.000001);
        d = propsReader.loadDoubleField("my.double.scientific.field", null);
        assertEquals(2.345E-129, d, 0.000001);
    }

    @Test
    public void testReadInteger() throws IOException {
        Properties props = new Properties();
        props.load(TestPropertiesReader.class.getResourceAsStream(CONFIG_FILE));
        PropertiesReader propsReader = new PropertiesReader(props, url);
        int i = propsReader.loadIntegerField("my.int.field", null);
        assertEquals(56790, i);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testReadIntegerBadInput() throws IOException {
        Properties props = new Properties();
        props.load(TestPropertiesReader.class.getResourceAsStream(CONFIG_FILE));
        PropertiesReader propsReader = new PropertiesReader(props, url);
        propsReader.loadIntegerField("my.bad.int.field", null);
    }

    @Test
    public void testReadString() throws IOException {
        Properties props = new Properties();
        props.load(TestPropertiesReader.class.getResourceAsStream(CONFIG_FILE));
        PropertiesReader propsReader = new PropertiesReader(props, url);
        String s = propsReader.loadStringField("my.string.field", null);
        assertEquals("abcde", s);
    }

    @Test
    public void testReadStringArray() throws IOException  {
        Properties props = new Properties();
        props.load(TestPropertiesReader.class.getResourceAsStream(CONFIG_FILE));
        PropertiesReader propsReader = new PropertiesReader(props, url);
        String[] array = propsReader.loadStringArrayField("my.string.array.field", null);
        assertEquals(7, array.length);
        assertEquals("four", array[3]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testReadEmptyStringArray() throws IOException  {
        Properties props = new Properties();
        props.load(TestPropertiesReader.class.getResourceAsStream(CONFIG_FILE));
        PropertiesReader propsReader = new PropertiesReader(props, url);
        propsReader.loadStringArrayField("my.empty.string.array.field", null);
    }

    @Test
    public void testReadStringAsArray() throws IOException  {
        Properties props = new Properties();
        props.load(TestPropertiesReader.class.getResourceAsStream(CONFIG_FILE));
        PropertiesReader propsReader = new PropertiesReader(props, url);
        String[] array = propsReader.loadStringArrayField("my.string.field", null);
        assertEquals(1, array.length);
    }

    @Test
    public void testUseArrayStringDefault() throws IOException  {
        Properties props = new Properties();
        props.load(TestPropertiesReader.class.getResourceAsStream(CONFIG_FILE));
        PropertiesReader propsReader = new PropertiesReader(props, url);
        String[] array = propsReader.loadStringArrayField("non.existing.string.field", new String[]{});
        assertEquals(0, array.length);
    }
}