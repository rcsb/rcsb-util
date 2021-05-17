package org.rcsb.common.config;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;


import java.util.Properties;


/**
 * A manager of config profiles to be specified through system property {@value CONFIG_PROFILE_PROPERTY}.
 * <p>
 * Usage:
 * <pre>
 *     URL configProfileDir = ConfigProfileManager.getProfileUrl();
 *     URL pdbPropertiesFile = new URL(configProfileDir.getProtocol(), configProfileDir.getHost(), configProfileDir.getPort(), configProfileDir.getFile() + "/" + "pdb.properties");
 * </pre>
 *
 * @author Jose Duarte
 */
public class ConfigProfileManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigProfileManager.class);

    public static final String CONFIG_PROFILE_PROPERTY = "rcsbConfigProfile";

    /**
     * The filename of the build properties. Each project should produce it in the maven config.
     */
    public static final String BUILD_PROPERTIES_FILENAME = "about.properties";

    
    protected static boolean urlExists(URL url) {

    	if (url.getProtocol().equals("file")) {
    		try {
    			File file = new File(url.toURI());
    			return file.exists();
    		} catch (URISyntaxException e) {
    			LOGGER.warn("Something went wrong while converting URL '{}' to file. Considering that URL doesn't exist", url.toString());
    			return false;
    		}
    	} else {
    		try {
    			HttpURLConnection connection = (HttpURLConnection) url.openConnection(); 
    			connection.setRequestMethod("HEAD");
    			int code = connection.getResponseCode();

                return code == HttpURLConnection.HTTP_OK;

    		} catch (IOException e) {
    			return false;
    		}

    	}
    }

    /**
     * Get the config profile URL specified in system property {@value CONFIG_PROFILE_PROPERTY} (passing -D parameter to JVM).
     * If no profile is specified in system property or a non-existing URL is specified, then null is returned.
     * @return the URL or null if no profile specified
     * @throws IllegalStateException if no valid configuration profile can be found
     */
    public static URL getRcsbConfigUrl() {
        String profile = System.getProperty(CONFIG_PROFILE_PROPERTY);
        URL configUrl;

        if (profile == null || profile.equals("")) {
            LOGGER.error("No {} system property specified with -D{}. ", CONFIG_PROFILE_PROPERTY, CONFIG_PROFILE_PROPERTY);
            configUrl = null;
        } else {

            try {
                configUrl = new URL(profile);

                if (!urlExists(configUrl)) {
                    LOGGER.error("The specified profile URL {} is not reachable.", configUrl.toString());
                    configUrl = null;
                } else {
                    LOGGER.info("Valid config profile was read from {} system property. Will load config file from URL {}", CONFIG_PROFILE_PROPERTY, configUrl.toString());

                }
            } catch (MalformedURLException e) {
                LOGGER.error("The URL '{}' specified with {} system property is malformed: {}", profile, CONFIG_PROFILE_PROPERTY, e.getMessage());
                configUrl = null;
            }
        }

        if (configUrl == null) {
            throw new IllegalStateException("No valid configuration profile found! A valid configuration profile must be provided via JVM parameter -D" + CONFIG_PROFILE_PROPERTY);
        }

        return configUrl;
    }

    /**
     * Returns a Properties object by reading the given configUrl.
     * @param configUrl the URL where the properties are located
     * @return the properties object
     * @throws IllegalStateException if URL is not valid or properties file can't be read
     */
    private static Properties getPropertiesObject(URL configUrl) {

        if (configUrl == null) {
            String msg = "Passed configUrl is null";
            LOGGER.error(msg);
            throw new IllegalArgumentException(msg);
        }

    	Properties props = null;

        if (urlExists(configUrl)) {
            try {
                InputStream propstream = configUrl.openStream();
                props = new Properties();
                props.load(propstream);
                LOGGER.info("Reading properties file {}", configUrl.toString());

            } catch (IOException e) {
                String msg = "Something went wrong reading file from URL "+configUrl.toString()+", although the file was reported as existing";
                LOGGER.error(msg);
                throw new IllegalStateException(msg);
            }
        }

        return props;
    }

    /**
     * Return a {@link PropertiesReader} object reading the given configUrl
     * @param configUrl the full URL of a java properties file
     * @return the PropertiesReader object
     */
    public static PropertiesReader getPropertiesReader(URL configUrl) {
        return new PropertiesReader(getPropertiesObject(configUrl), configUrl);
    }

    /**
     * Return a {@link PropertiesReader} object reading the URL provided in system property {@value CONFIG_PROFILE_PROPERTY} (passing -D parameter to JVM).
     * @return the PropertiesReader object
     */
    public static PropertiesReader getPropertiesReader() {
        URL configUrl = getRcsbConfigUrl();
        return new PropertiesReader(getPropertiesObject(configUrl), configUrl);
    }
    
    /**
     * Converts a Properties object into a Map. Useful for interfacing with JPA (which needs properties as a Map).
     * Example usage:
     * <pre>
     * Properties props = ConfigProfileManager.getSequoiaAppProperties();
     * Persistence.createEntityManagerFactory("myjpa", ConfigProfileManager.getMapFromProperties(props));
     * </pre>
     * @param props the properties
     * @return the map of properties
     */
    public static Map<String,String> getMapFromProperties(Properties props) {
    	Map<String,String> map = new HashMap<>();
    	
    	for (Entry<Object, Object> entry : props.entrySet()) {
    		map.put((String) entry.getKey(), (String) entry.getValue());
    	}
    	
    	return map;
    }
    

    /**
     * Gets the build properties from file {@value #BUILD_PROPERTIES_FILENAME} placed at the root of the resources dir.
     * The file should contain project.version, build.hash and build.timestamp variables populated by maven at buildtime. 
     * @return the build properties
     */
    public static Properties getBuildProperties() {
    	InputStream propstream = Thread.currentThread().getContextClassLoader().getResourceAsStream(BUILD_PROPERTIES_FILENAME);    	
    	Properties props = new Properties();
    	try {
    		props.load(propstream);
    	} catch (IOException e) {
    		LOGGER.warn("Could not get the build properties from {} file! Build information will not be available.", BUILD_PROPERTIES_FILENAME);
    	}
        return props;
    }
}
