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
    			LOGGER.warn("Something went wrong while converting UL '{}' to file. Considering that URL doesn't exist", url.toString());
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
     * Returns an InputStream to a properties file with given name in the config profile URL directory.
     * @param propertiesFileName
     * @return
     */
    private static InputStream getPropertiesStream(String propertiesFileName) {
        InputStream propstream = null;

        URL profileUrl = ConfigProfileManager.getProfileUrl();
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
                    propstream = f.openStream();
                    LOGGER.info("Reading properties file {}", f.toString());

                } catch (IOException e) {
                    // this shouldn't happen because we checked for existence first
                	String msg = "Something is wrong! URL "+f.toString()+" reported as existing but openStream could not open it!";
                    LOGGER.error(msg);
                    throw new RuntimeException(msg);
                }
            } else {
            	String msg = "Could not find "+propertiesFileName+" file in profile URL "+ profileUrl.toString() +". Can't continue";
                LOGGER.error(msg);
                throw new RuntimeException(msg);
            }

        }
        

        return propstream;
    	
    }
    
    /**
     * Get the {@value #PDB_DB_CONFIG_FILENAME} config file for configuration of connection to primary pdb database.
     * The config file is searched under the config profile URL path specified through system property {@value CONFIG_PROFILE_PROPERTY}
     * @return
     */
    public static InputStream getPdbDbProperties() {
    	return getPropertiesStream(PDB_DB_CONFIG_FILENAME);
    }
    
    /**
     * Get the {@value #UNIPROT_DB_CONFIG_FILENAME} config file for configuration of connection to primary uniprot database.
     * The config file is searched under the config profile URL path specified through system property {@value CONFIG_PROFILE_PROPERTY} 
     * @return
     */
    public static InputStream getUniprotDbProperties() {
    	return getPropertiesStream(UNIPROT_DB_CONFIG_FILENAME);
    }

    /**
     * Get the {@value #YOSEMITE_APP_CONFIG_FILENAME} config file for configuration of the yosemite app.
     * The config file is searched under the config profile URL path specified through system property {@value CONFIG_PROFILE_PROPERTY} 
     * @return
     */
    public static InputStream getYosemiteAppProperties() {
    	return getPropertiesStream(YOSEMITE_APP_CONFIG_FILENAME);
    }

}
