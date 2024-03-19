package org.rcsb.common.config;

import org.junit.jupiter.api.*;

import java.io.IOException;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

public class TestConfigMap {

    private static final String CONFIG_FILE = "/test_config.properties";
    private static ConfigMapImpl config;

    @BeforeAll
    public static void setUp() {
        Properties props = new Properties();
        try {
            props.load(TestConfigMap.class.getResourceAsStream(CONFIG_FILE));
        } catch (IOException e) {
            fail("Could not read resource file '" + CONFIG_FILE + "'", e);
        }
        config = new ConfigMapImpl(props);
    }

    @Test
    public void testReadIntArrayGoodInput() throws IOException {
        int[] array = config.getIntArray("my.int.array.field", null);
        assertEquals(10, array.length);
    }

    @Test
    public void testReadIntArrayBadInput() throws IOException {
        assertThrows(
            ConfigValueConversionException.class,
            () -> config.getIntArray("my.bad.int.array.field",null)
        );
    }

    @Test
    public void testReadDoubleArray() throws IOException  {
        double[] array = config.getDoubleArray("my.double.array.field", null);
        assertEquals(11, array.length);
    }

    @Test
    public void testReadDouble() throws IOException {
        double d = config.getDouble("my.double.field");
        assertEquals(2.345, d, 0.000001);
        d = config.getDouble("my.double.scientific.field");
        assertEquals(2.345E-129, d, 0.000001);
    }

    @Test
    public void testReadInteger() throws IOException {
        int i = config.getInt("my.int.field");
        assertEquals(56790, i);
    }

    @Test()
    public void testReadIntegerBadInput() throws IOException {
        assertThrows(
            ConfigValueConversionException.class,
            () -> config.getInt("my.bad.int.field")
        );
    }

    @Test
    public void testReadString() throws IOException {
        String s = config.getStr("my.string.field", null);
        assertEquals("abcde", s);
    }

    @Test
    public void testReadStringArray() throws IOException  {
        String[] array = config.getStrArray("my.string.array.field");
        assertEquals(7, array.length);
        assertEquals("four", array[3]);
    }

    @Test
    public void testReadEmptyStringArray() throws IOException  {
        assertThrows(
            ConfigKeyMissingException.class,
            () -> config.getStrArray("my.empty.string.array.field", null)
        );
    }

    @Test
    public void testReadStringAsArray() throws IOException  {
        String[] array = config.getStrArray("my.string.field", null);
        assertEquals(1, array.length);
    }

    @Test
    public void testUseArrayStringDefault() throws IOException  {
        String[] array = config.getStrArray("missing.string.field", new String[]{"x"});
        assertEquals(1, array.length);
    }

    @Test
    public void testLazyExists() throws IOException {
        var actual = assertDoesNotThrow(() -> config.getLazy("my.int.field", Integer::parseInt, () -> null));
        assertEquals(56790, actual);
    }

    @Test
    public void testLazyDoesNotExist() throws IOException {
        var actual = assertDoesNotThrow(() -> config.getLazy("my.missing.int.field", Integer::parseInt, () -> null));
        assertNull(actual);
    }

}