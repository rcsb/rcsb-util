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
     * The config file for the connection to the primary pdb database.
     */
	public static final String PDB_DB_CONFIG_FILENAME      = "pdb.database.properties";
	
	/**
	 * The config file for the connection to the primary uniprot database.
	 */
	public static final String UNIPROT_DB_CONFIG_FILENAME  = "uniprot.database.properties";
	
	/**
	 * The config file for the yosemite app
	 */
	public static final String YOSEMITE_APP_CONFIG_FILENAME = "yosemite.app.properties";

    /**
     * The config file for the redwood app
     */
    public static final String REDWOOD_APP_CONFIG_FILENAME = "redwood.app.properties";

    /**
     * The config file for the tahoe app
     */
    public static final String TAHOE_APP_CONFIG_FILENAME = "tahoe.app.properties";

    /**
     * The config file for the sequoia app
     */
    public static final String SEQUOIA_APP_CONFIG_FILENAME = "sequoia.app.properties";

    /**
     * The filename of the build properties. Each project should produce it in the maven config.
     */
    public static final String BUILD_PROPERTIES_FILENAME = "about.properties";


    /**
     * The pdb.properties file that is used by the legacy PDB Webapp (usually in /pdb/pdbinabox/pdb.properties
     */

    public static final String PDB_PROPERTIES_FILENAME = "pdb.properties";



    /**
     * The profile URL, can be also in a locally mounted file system if prefixed with file://
     */
    private static URL profileUrl;

    static {
        String profile = System.getProperty(CONFIG_PROFILE_PROPERTY);

        if (profile == null || profile.equals("")) {
            LOGGER.error("No {} system property specified with -D{}. ", CONFIG_PROFILE_PROPERTY, CONFIG_PROFILE_PROPERTY);
            profileUrl = null;
        } else {

        	try {        		        	
        		profileUrl = new URL(profile);
        	} catch (MalformedURLException e) {
        		LOGGER.error("The URL '{}' specified with {} system property is malformed: {}", profile, CONFIG_PROFILE_PROPERTY, e.getMessage());
        		profileUrl = null;
        	}

            if (!urlExists(profileUrl)) {
                LOGGER.error("The specified profile URL {} is not reachable.", profileUrl.toString());
                profileUrl = null;
            } else {
                LOGGER.info("Valid config profile was read from {} system property. Will load config files from URL {}", CONFIG_PROFILE_PROPERTY, profileUrl.toString());

            }
        }
        
        if (profileUrl==null) {
        	
        	LOGGER.error("No valid configuration profile found! Can't continue, exiting JVM!");
        	System.exit(1);
        	
        }
    }
    
    private static boolean urlExists(URL url) {


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

    			if (code == 200) 
    				return true;
    			else
    				return false;
    		} catch (IOException e) {
    			return false;

    		}

    	}

    }

    /**
     * Get the config profile path specified in system property {@value CONFIG_PROFILE_PROPERTY} (passing -D parameter to JVM).
     * If no profile is specified in system property or a non-existing dir specified then null is returned.
     * @return the path to the profile or null if no profile specified
     */
    public static URL getProfileUrl() {
        return profileUrl;
    }

    /**
     * Returns a Properties object by reading the given propertiesFileName from config profile URL directory specified in {@value CONFIG_PROFILE_PROPERTY}.
     * @param propertiesFileName
     * @return
     */
    private static Properties getPropertiesObject(String propertiesFileName) {

    	Properties props = null;
    	
        URL profileUrl = getProfileUrl();
        if (profileUrl!=null) {
            URL f = null;
            try {
            	f = new URL(profileUrl.getProtocol(), profileUrl.getHost(), profileUrl.getPort(), profileUrl.getFile() + "/" + propertiesFileName);
            } catch (MalformedURLException e) {
            	String msg = "Unexpected error! Malformed URL for properties file "+propertiesFileName+". Error: " + e.getMessage();
            	LOGGER.error(msg);
            	throw new RuntimeException(msg);
            }

            if (urlExists(f)) {
                try {
                	InputStream propstream = f.openStream();
                	props = new Properties();
                	props.load(propstream);
                    LOGGER.info("Reading properties file {}", f.toString());

                } catch (IOException e) {
                	String msg = "Something went wrong reading file from URL "+f.toString()+", although the file was reported as existing";
                    LOGGER.error(msg);
                    throw new RuntimeException(msg);
                }
            } else {
            	String msg = "Could not find "+propertiesFileName+" file in profile URL "+ profileUrl.toString() +". Can't continue";
                LOGGER.error(msg);
                throw new RuntimeException(msg);
            }

        }
        
        return props;
    	
    }
    
    /**
     * Converts a Properties object into a Map. Useful for interfacing with JPA (which needs properties as a Map).
     * Example usage:
     * <pre>
     * Properties props = ConfigProfileManager.getSequoiaAppProperties();
     * Persistence.createEntityManagerFactory("myjpa", ConfigProfileManager.getMapFromProperties(props));
     * </pre>
     * @param props the properties
     * @return
     */
    public static Map<String,String> getMapFromProperties(Properties props) {
    	Map<String,String> map = new HashMap<>();
    	
    	for (Entry<Object, Object> entry : props.entrySet()) {
    		map.put((String) entry.getKey(), (String) entry.getValue());
    	}
    	
    	return map;
    }
    
    /**
     * Gets the Properties object corresponding to the {@value #PDB_DB_CONFIG_FILENAME} config file for configuration of connection to primary pdb database.
     * The config file is searched under the config profile URL path specified through system property {@value CONFIG_PROFILE_PROPERTY}
     * @return
     */
    public static Properties getPdbDbProperties() {
    	return getPropertiesObject(PDB_DB_CONFIG_FILENAME);
    }
    
    /**
     * Gets the Properties object corresponding to the {@value #UNIPROT_DB_CONFIG_FILENAME} config file for configuration of connection to primary uniprot database.
     * The config file is searched under the config profile URL path specified through system property {@value CONFIG_PROFILE_PROPERTY} 
     * @return
     */
    public static Properties getUniprotDbProperties() {
    	return getPropertiesObject(UNIPROT_DB_CONFIG_FILENAME);
    }

    /**
     * Gets the Properties object corresponding to the {@value #YOSEMITE_APP_CONFIG_FILENAME} config file for configuration of the yosemite app.
     * The config file is searched under the config profile URL path specified through system property {@value CONFIG_PROFILE_PROPERTY} 
     * @return
     */
    public static Properties getYosemiteAppProperties() {
    	return getPropertiesObject(YOSEMITE_APP_CONFIG_FILENAME);
    }

    /**
     * Gets the Properties object corresponding to the {@value #REDWOOD_APP_CONFIG_FILENAME} config file for configuration of the redwood app.
     * The config file is searched under the config profile URL path specified through system property {@value CONFIG_PROFILE_PROPERTY}
     * @return
     */
    public static Properties getRedwoodAppProperties() {
        return getPropertiesObject(REDWOOD_APP_CONFIG_FILENAME);
    }

    /**
     * Gets the Properties object corresponding to the {@value #TAHOE_APP_CONFIG_FILENAME} config file for configuration of the tahoe app.
     * The config file is searched under the config profile URL path specified through system property {@value CONFIG_PROFILE_PROPERTY}
     * @return
     */
    public static Properties getTahoeAppProperties() {
        return getPropertiesObject(TAHOE_APP_CONFIG_FILENAME);
    }
    
    /**
     * Gets the Properties object corresponding to the {@value #SEQUOIA_APP_CONFIG_FILENAME} config file for configuration of the sequoia app.
     * The config file is searched under the config profile URL path specified through system property {@value CONFIG_PROFILE_PROPERTY}
     * @return
     */
    public static Properties getSequoiaAppProperties() {
        return getPropertiesObject(SEQUOIA_APP_CONFIG_FILENAME);
    }


    /** Gets the content of a pdb.properties file as it gets used by the legacy PDB webapp
     *
     * @return
     */
    public static Properties getLegacyPdbProperties() { return getPropertiesObject(PDB_PROPERTIES_FILENAME);}


    /**
     * Gets the build properties from file {@value #BUILD_PROPERTIES_FILENAME} placed at the root of the resources dir.
     * The file should contain project.version, build.hash and build.timestamp variables populated by maven at buildtime. 
     * @return
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
