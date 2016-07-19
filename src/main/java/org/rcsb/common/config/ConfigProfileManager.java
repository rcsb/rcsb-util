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

    public static final String CONFIG_PROFILE_PROPERTY = "pdbConfigProfile";


    public static final String PDB_DB_CONFIG_FILENAME = "pdb.database.properties";
    public static final String UNIPROT_DB_CONFIG_FILENAME = "uniprot.database.properties";


    private static File profilePath;

    static {
        String profile = System.getProperty(CONFIG_PROFILE_PROPERTY);

        String failmsg = "Will fail-back to in-war and pdbinabox config files.";

        if (profile == null || profile.equals("")) {
            LOGGER.warn("No {} specified with -D{}. " + failmsg, CONFIG_PROFILE_PROPERTY, CONFIG_PROFILE_PROPERTY);
            profilePath = null;
        } else {

            profilePath = new File(profile);

            if (!profilePath.exists() || !profilePath.isDirectory()) {
                LOGGER.warn("The specified profile path {} does not exist or is not a directory. " + failmsg, profilePath.toString());
                profilePath = null;
            } else {
                LOGGER.info("Valid config profile was read from {} system property. Will load config files from dir {}", CONFIG_PROFILE_PROPERTY, profilePath.toString());

            }
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



    // some common utility methods, better placed here than elsewhere

    /**
     * Returns an InputStream to the pdb.database.properties file, either from config profile or if not found
     * fails back to an in-war file.
     * @return the input stream to the properties file
     */
    public static InputStream getPdbDbPropertiesStream() {
        InputStream propstream = null;

        String msg = "Overwriting default persistence.xml file from pdbormapping with parameters from";

        boolean fileInProfileOk = false;
        File profilePath = ConfigProfileManager.getProfilePath();
        if (profilePath!=null) {
            File f = new File(profilePath, PDB_DB_CONFIG_FILENAME);

            if (f.exists()) {
                try {
                    propstream = new FileInputStream(f);
                    LOGGER.info(msg + " file {}", f.toString());
                    fileInProfileOk = true;

                } catch (FileNotFoundException e) {
                    // this shouldn't happen because we checked for existence first
                    LOGGER.error("Something is wrong! File {} reported as existing but FileInputStream could not open it! Will fail back to in-war config file", f.toString());
                    fileInProfileOk = false;
                }
            } else {
                LOGGER.warn("Could not find {} file in profile path {}. Will fail back to in-war config file", PDB_DB_CONFIG_FILENAME, profilePath.toString());
                fileInProfileOk = false;
            }

        }


        if (!fileInProfileOk) {

            ClassLoader cloader = Thread.currentThread().getContextClassLoader();
            propstream = cloader.getResourceAsStream(PDB_DB_CONFIG_FILENAME);

            if (propstream == null) {
                LOGGER.warn("Could not read in-war file {} from class context", PDB_DB_CONFIG_FILENAME);
            } else {
                LOGGER.info(msg + " in-war file {}", PDB_DB_CONFIG_FILENAME);
            }
        }

        return propstream;
    }

    /**
     * Returns an InputStream to the uniprot.database.properties file, either from config profile or if not found
     * fails back to an in-war file.
     * @return the input stream to the properties file
     */
    public static InputStream getUniprotDbPropertiesStream() {

        InputStream propstream = null;

        boolean fileInProfileOk = false;
        File profilePath = ConfigProfileManager.getProfilePath();
        if (profilePath!=null) {
            File f = new File(profilePath, UNIPROT_DB_CONFIG_FILENAME);

            if (f.exists()) {
                try {
                    propstream = new FileInputStream(f);
                    LOGGER.info("Configuring UniProt DAO from config profile file {}", f.toString());
                    fileInProfileOk = true;

                } catch (FileNotFoundException e) {
                    // this shouldn't happen because we checked for existence first
                    LOGGER.error("Something is wrong! File {} reported as existing but FileInputStream could not open it! Will fail back to in-war config file", f.toString());
                    fileInProfileOk = false;
                }
            } else {
                LOGGER.warn("Could not find {} file in profile path {}. Will fail back to in-war config file", UNIPROT_DB_CONFIG_FILENAME, profilePath.toString());
                fileInProfileOk = false;
            }

        }


        if (!fileInProfileOk) {
            LOGGER.info("Configuring UniProt DAO from in-war file {}", UNIPROT_DB_CONFIG_FILENAME);

            ClassLoader cloader = Thread.currentThread().getContextClassLoader();

            propstream = cloader.getResourceAsStream(UNIPROT_DB_CONFIG_FILENAME);

            if (propstream == null) {
                LOGGER.error("Could not get file {} from class context! No UniProt DAO config will be available!", UNIPROT_DB_CONFIG_FILENAME);
            }

        }
        return propstream;
    }
}
