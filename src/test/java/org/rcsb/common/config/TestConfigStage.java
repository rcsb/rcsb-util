package org.rcsb.common.config;

import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Disabled;

public class TestConfigStage {
    // TODO migrate tests from TestPropertiesReader in 2.0 release

    private static final String file = "/test_config.properties";

    private static final ConfigStage stage = new ConfigStage();


    @Test
    public void testBasicRead() throws IOException {
        ConfigMap map = stage.read(getClass().getResource(file));
        assertEquals(10, map.size());
        assertIterableEquals(List.of(3.0, 4.0, 6.0, 7.0, 8.0, 13.0, 14.0, 17.0, 21.0, 22.0),
            map.getList("my.int.array.field", Double::parseDouble)
        );
    }

    @Test
    public void testMissingFile() throws IOException {
        assertThrows(ConfigProfileException.class, () -> stage.read("asdfasdf"));
    }

    @Test
    public void testMissingHttp() throws IOException {
        assertThrows(ConfigProfileException.class, () -> stage.read("https://asdfasdrfasd23.qwerqwe3wesqweA"));
    }

    @Test
    @Disabled
    public void testWeirdHttp() throws IOException {
        // I wanted to know what would happen here
        // Apparently, it loads the HTML
        // Maybe properties aren't the way to go
        ConfigMap map = stage.read("https://google.com");
        System.out.println(map);
    }
}
