package org.rcsb.common.config;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TestConfigMapImpl {

    private static final String CONFIG_FILE = "/test_config.properties";

    static {
        try {
            URL url = new URL("file:///");
        } catch (MalformedURLException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Test
    public void testReadIntArrayGoodInput() throws IOException {
        Properties props = new Properties();
        props.load(TestConfigMapImpl.class.getResourceAsStream(CONFIG_FILE));
        ConfigMapImpl config = new ConfigMapImpl(props);
        int[] array = config.getIntArray("my.int.array.field", null);
        assertEquals(10, array.length);
    }

    @Test
    public void testReadIntArrayBadInput() throws IOException {
        Properties props = new Properties();
        props.load(TestConfigMapImpl.class.getResourceAsStream(CONFIG_FILE));
        ConfigMapImpl config = new ConfigMapImpl(props);
        assertThrows(
            ConfigValueConversionException.class,
            () -> config.getIntArray("my.bad.int.array.field",null)
        );
    }

    @Test
    public void testReadDoubleArray() throws IOException  {
        Properties props = new Properties();
        props.load(TestConfigMapImpl.class.getResourceAsStream(CONFIG_FILE));
        ConfigMapImpl config = new ConfigMapImpl(props);
        double[] array = config.getDoubleArray("my.double.array.field", null);
        assertEquals(11, array.length);
    }

    @Test
    public void testReadDouble() throws IOException {
        Properties props = new Properties();
        props.load(TestConfigMapImpl.class.getResourceAsStream(CONFIG_FILE));
        ConfigMapImpl config = new ConfigMapImpl(props);
        double d = config.getDouble("my.double.field");
        assertEquals(2.345, d, 0.000001);
        d = config.getDouble("my.double.scientific.field");
        assertEquals(2.345E-129, d, 0.000001);
    }

    @Test
    public void testReadInteger() throws IOException {
        Properties props = new Properties();
        props.load(TestConfigMapImpl.class.getResourceAsStream(CONFIG_FILE));
        ConfigMapImpl config = new ConfigMapImpl(props);
        int i = config.getInt("my.int.field");
        assertEquals(56790, i);
    }

    @Test()
    public void testReadIntegerBadInput() throws IOException {
        Properties props = new Properties();
        props.load(TestConfigMapImpl.class.getResourceAsStream(CONFIG_FILE));
        ConfigMapImpl config = new ConfigMapImpl(props);
        assertThrows(
            ConfigValueConversionException.class,
            () -> config.getInt("my.bad.int.field")
        );
    }

    @Test
    public void testReadString() throws IOException {
        Properties props = new Properties();
        props.load(TestConfigMapImpl.class.getResourceAsStream(CONFIG_FILE));
        ConfigMapImpl config = new ConfigMapImpl(props);
        String s = config.getStr("my.string.field", null);
        assertEquals("abcde", s);
    }

    @Test
    public void testReadStringArray() throws IOException  {
        Properties props = new Properties();
        props.load(TestConfigMapImpl.class.getResourceAsStream(CONFIG_FILE));
        ConfigMapImpl config = new ConfigMapImpl(props);
        String[] array = config.getStrArray("my.string.array.field");
        assertEquals(7, array.length);
        assertEquals("four", array[3]);
    }

    @Test
    public void testReadEmptyStringArray() throws IOException  {
        Properties props = new Properties();
        props.load(TestConfigMapImpl.class.getResourceAsStream(CONFIG_FILE));
        ConfigMapImpl config = new ConfigMapImpl(props);
        assertThrows(
            ConfigKeyMissingException.class,
            () -> config.getStrArray("my.empty.string.array.field", null)
        );
    }

    @Test
    public void testReadStringAsArray() throws IOException  {
        Properties props = new Properties();
        props.load(TestConfigMapImpl.class.getResourceAsStream(CONFIG_FILE));
        ConfigMapImpl config = new ConfigMapImpl(props);
        String[] array = config.getStrArray("my.string.field", null);
        assertEquals(1, array.length);
    }

    @Test
    public void testUseArrayStringDefault() throws IOException  {
        Properties props = new Properties();
        props.load(TestConfigMapImpl.class.getResourceAsStream(CONFIG_FILE));
        ConfigMapImpl config = new ConfigMapImpl(props);
        String[] array = config.getStrArray("non.existing.string.field", new String[]{"x"});
        assertEquals(1, array.length);
    }
}