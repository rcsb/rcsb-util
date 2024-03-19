package org.rcsb.common.config;

import org.junit.jupiter.api.*;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

public class TestConfigManager {

    private static final String file = "/test_config.properties";
    private static ConfigManager stage;

    @BeforeAll
    public static void setUp() {
        stage = new ConfigManagerImpl();
    }

    @Test
    public void testBasicRead() throws IOException {
        URL url = Objects.requireNonNull(getClass().getResource(file));
        ConfigMap map = stage.read(url);
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
