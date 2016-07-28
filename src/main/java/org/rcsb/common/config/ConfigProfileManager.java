package org.rcsb.common.config;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * A manager of config profiles to be specified through system property {@value CONFIG_PROFILE_PROPERTY}.
 * <p>
 * Usage:
 * <pre>
 *     File configProfileDir = ConfigProfileManager.getProfilePath();
 *     File pdbPropertiesFile = new File(configProfileDir, "pdb.properties");
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

    
    private static File profilePath;

    static {
        String profile = System.getProperty(CONFIG_PROFILE_PROPERTY);

        if (profile == null || profile.equals("")) {
            LOGGER.error("No {} system property specified with -D{}. ", CONFIG_PROFILE_PROPERTY, CONFIG_PROFILE_PROPERTY);
            profilePath = null;
        } else {

            profilePath = new File(profile);

            if (!profilePath.exists() || !profilePath.isDirectory()) {
                LOGGER.error("The specified profile path {} does not exist or is not a directory.", profilePath.toString());
                profilePath = null;
            } else {
                LOGGER.info("Valid config profile was read from {} system property. Will load config files from dir {}", CONFIG_PROFILE_PROPERTY, profilePath.toString());

            }
        }
        
        if (profilePath==null) {
        	throw new RuntimeException("No configuration profile found! Can't continue!");
        }
    }

    /**
     * Get the config profile path specified in system property {@value CONFIG_PROFILE_PROPERTY} (passing -D parameter to JVM).
     * If no profile is specified in system property or a non-existing dir specified then null is returned.
     * @return the path to the profile or null if no profile specified
     */
    public static File getProfilePath() {
        return profilePath;
    }

    /**
     * Returns an InputStream to a properties file with given name in the config profile directory.
     * @param propertiesFileName
     * @return
     */
    public static InputStream getPropertiesStream(String propertiesFileName) {
        InputStream propstream = null;

        File profilePath = ConfigProfileManager.getProfilePath();
        if (profilePath!=null) {
            File f = new File(profilePath, propertiesFileName);

            if (f.exists()) {
                try {
                    propstream = new FileInputStream(f);
                    LOGGER.info("Reading properties file {}", f.toString());

                } catch (FileNotFoundException e) {
                    // this shouldn't happen because we checked for existence first
                    LOGGER.error("Something is wrong! File {} reported as existing but FileInputStream could not open it!", f.toString());
                }
            } else {
            	String msg = "Could not find "+propertiesFileName+" file in profile path "+ profilePath.toString() +". Can't continue";
                LOGGER.error(msg);
                throw new RuntimeException(msg);
            }

        }
        

        return propstream;
    	
    }
    
    /**
     * Get the {@value #PDB_DB_CONFIG_FILENAME} config file for configuration of connection to primary pdb database.
     * The config file is searched in the config profile directory set through system property {@value CONFIG_PROFILE_PROPERTY}
     * @return
     */
    public static InputStream getPdbDbProperties() {
    	return getPropertiesStream(PDB_DB_CONFIG_FILENAME);
    }
    
    /**
     * Get the {@value #UNIPROT_DB_CONFIG_FILENAME} config file for configuration of connection to primary uniprot database.
     * The config file is searched in the config profile directory set through system property {@value CONFIG_PROFILE_PROPERTY} 
     * @return
     */
    public static InputStream getUniprotDbProperties() {
    	return getPropertiesStream(UNIPROT_DB_CONFIG_FILENAME);
    }

}
