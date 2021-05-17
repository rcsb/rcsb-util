package org.rcsb.common.config;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.Assert;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Properties;

public class TestUrlReading {

    private static final String CONFIG_FILE = "/test_config.properties";

    private static String configUrl;


    @BeforeClass
    public static void setUp() throws IOException {
        File file = File.createTempFile("rcsb-util-test", ".properties");
        file.deleteOnExit();
        InputStream is = TestUrlReading.class.getResourceAsStream(CONFIG_FILE);
        if (is == null) throw new IOException("Could not load resource " + CONFIG_FILE);
        Files.copy(is, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
        configUrl = "file://" + file.getAbsolutePath();
        System.setProperty(ConfigProfileManager.CONFIG_PROFILE_PROPERTY, configUrl);
    }

    @Test
    public void shouldExist() throws IOException {
        Assert.assertTrue(ConfigProfileManager.urlExists(new URL(configUrl)));
    }

    @Test
    public void shouldExistPassedBySystemProperty() throws IOException {
        Assert.assertTrue(ConfigProfileManager.urlExists(ConfigProfileManager.getRcsbConfigUrl()));
    }

    @Test
    public void canReadProperty() {
        Properties properties = ConfigProfileManager.getPropertiesReader().getProperties();
        Assert.assertEquals("abcde", properties.getProperty("my.string.field"));
    }
}
