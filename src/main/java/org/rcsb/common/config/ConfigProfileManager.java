package org.rcsb.common.config;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.*;
import java.util.*;
import java.util.Map.Entry;

import java.util.stream.Collectors;

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
public final class ConfigProfileManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigProfileManager.class);

    public static final String CONFIG_PROFILE_PROPERTY = "rcsbConfigProfile";

    /**
     * The config file for the connection to the primary pdb database.
     */
	public static final String PDB_DB_CONFIG_FILENAME = "pdb.database.properties";
	
	/**
	 * The config file for the connection to the primary uniprot database.
	 */
	public static final String UNIPROT_DB_CONFIG_FILENAME  = "uniprot.database.properties";
	
	/**
	 * The config file for the yosemite app
	 */
	public static final String YOSEMITE_APP_CONFIG_FILENAME = "yosemite.app.properties";

	/**
	 * The config file for the borrego app
	 */
	public static final String BORREGO_APP_CONFIG_FILENAME = "borrego.app.properties";

    /**
     * The config file for the shape fast search structure app
     */
	public static final String SHAPE_APP_CONFIG_FILENAME = "shape.app.properties";

    /**
     * The config file for the text search indexer app
     */
    public static final String EVERGLADES_APP_CONFIG_FILENAME = "everglades.app.properties";

    /**
     * The config file for the redwood app
     */
    public static final String REDWOOD_APP_CONFIG_FILENAME = "redwood.app.properties";

    /**
     * The config file for the arches app
     */
    public static final String ARCHES_APP_CONFIG_FILENAME = "arches.app.properties";

    /**
     * The config file for the pecos app
     */
    public static final String PECOS_APP_CONFIG_FILENAME = "pecos.app.properties";

    /**
     * The config file for the seqmotif app
     */
    public static final String SEQMOTIF_APP_CONFIG_FILENAME = "seqmotif.app.properties";

   /**
    * The config file for the seqmotif app
    */
   public static final String DOWNLOAD_APP_CONFIG_FILENAME = "download.app.properties";

    /**
     * The filename of the build properties. Each project should produce it in the maven config.
     */
    public static final String BUILD_PROPERTIES_FILENAME = "about.properties";

    /**
     * The pdb.properties file that is used by the legacy PDB Webapp (usually in /pdb/pdbinabox/pdb.properties
     */

    public static final String PDB_PROPERTIES_FILENAME = "pdb.properties";

    private ConfigProfileManager() {
    }

    private static boolean urlExists(URL url) {

    	if ("file".equals(url.getProtocol())) {
    		try {
    			File file = new File(url.toURI());
    			return file.exists();
    		} catch (URISyntaxException e) {
    			LOGGER.warn(
                    "Something went wrong while converting URL '{}' to file. Considering that URL doesn't exist",
                    url.toString(), e
                );
    			return false;
    		}
    	}
        try {
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("HEAD");
            int code = connection.getResponseCode();
            return code == 200;
        } catch (ProtocolException e) {
            throw new UncheckedIOException(e);
        } catch (IOException e) {
            return false;
        }

    }

    /**
     * Gets the config profile path specified in system property {@value CONFIG_PROFILE_PROPERTY}
     * (passing -D parameter to JVM).
     * If no profile is specified in system property or a non-existing dir specified then null is returned.
     * @return The URL to the profile or empty if no profile is specified in the system property,
     *         the local path does not exist,
     *         or an HTTP status code other than 200 was received in response to a HEAD
     * @throws ConfigException if no valid configuration profile can be found
     */
    public static Optional<URL> getProfileUrlOrEmpty() {
        return Optional.ofNullable(getProfileUrl());
    }

    /**
     * See {@link #getProfileUrlOrEmpty()}, which is preferred.
     */
    public static URL getProfileUrl() {
        String profile = System.getProperty(CONFIG_PROFILE_PROPERTY);

        if (profile == null || "".equals(profile)) {
            LOGGER.error(
                "No {} system property specified with -D{}. ",
                CONFIG_PROFILE_PROPERTY,
                CONFIG_PROFILE_PROPERTY
            );
            return null;
        }
        URL profileUrl = null;
        try {
            profileUrl = new URL(profile);
        } catch (MalformedURLException e) {
            LOGGER.error("The URL '{}' specified with {} system property is malformed: {}", profile, CONFIG_PROFILE_PROPERTY, e.getMessage());
        }
        if (profileUrl == null) {
            throw new ConfigException(
                "No valid configuration profile found! A valid configuration profile must be provided via JVM "
                    + "parameter -D" + CONFIG_PROFILE_PROPERTY
            );
        }
        if (urlExists(profileUrl)) {
            LOGGER.info(
                "Valid config profile was read from {} system property. Will load config files from URL {}",
                CONFIG_PROFILE_PROPERTY,
                profileUrl.toString()
            );
            return profileUrl;
        }
        LOGGER.error("The specified profile URL {} is not reachable.", profileUrl.toString());
        return null;
    }

    /**
     * Returns a Properties object by reading the given propertiesFileName from config profile URL directory specified in {@value CONFIG_PROFILE_PROPERTY}.
     * @param propertiesFileName the file name of the properties file located in the given profileUrl
     * @param profileUrl the URL where propertiesFileName is located
     * @return the properties object
     * @throws ConfigException if URL is not valid or properties file can't be read
     */
    private static Properties getPropertiesObject(String propertiesFileName, URL profileUrl) {
        Objects.requireNonNull(propertiesFileName);
        Objects.requireNonNull(profileUrl);
        URL f;
        try {
            f = new URL(profileUrl.getProtocol(), profileUrl.getHost(), profileUrl.getPort(), profileUrl.getFile() + "/" + propertiesFileName);
        } catch (MalformedURLException e) {
            String msg = "Unexpected error! Malformed URL for properties file "+propertiesFileName+". Error: " + e.getMessage();
            LOGGER.error(msg);
            throw new ConfigException(msg, e);
        }

        Properties props = new Properties();
        if (urlExists(f)) {
            try {
                InputStream propstream = f.openStream();
                props.load(propstream);
            } catch (IOException e) {
                String msg = "Something went wrong reading file from URL "+f.toString()+", although the file was reported as existing";
                LOGGER.error(msg);
                throw new ConfigException(msg, e);
            }
            LOGGER.info("Reading properties file {}", f.toString());
        } else {
            String msg = "Could not find "+propertiesFileName+" file in profile URL "+ profileUrl.toString() +". Can't continue";
            LOGGER.error(msg);
            throw new ConfigException(msg);
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
     */
    public static Map<String,String> getMapFromProperties(Properties props) {
    	return props.entrySet()
            .stream()
            .collect(
                Collectors.toMap(
                    entry -> (String) entry.getKey(),
                    entry -> (String) entry.getValue(),
                    (a, b) -> b)
            );
    }
    
    /**
     * Gets the Properties object corresponding to the {@value #PDB_DB_CONFIG_FILENAME} config file for configuration of connection to primary pdb database.
     * The config file is searched under the config profile URL path specified through system property {@value CONFIG_PROFILE_PROPERTY}
     */
    public static Properties getPdbDbProperties() {
    	return getPropertiesObject(PDB_DB_CONFIG_FILENAME, getProfileUrl());
    }

    /**
     * Gets the Properties object corresponding to the {@value #PDB_DB_CONFIG_FILENAME} config file for configuration of connection to primary pdb database.
     * The config file is searched under the config profile URL path specified through system property {@value CONFIG_PROFILE_PROPERTY}
     * @return
     */
    public static PropertiesReader getPdbDbPropertiesReader() {
        URL profileUrl = getProfileUrl();
        return new PropertiesReader(getPropertiesObject(PDB_DB_CONFIG_FILENAME, profileUrl), PDB_DB_CONFIG_FILENAME, profileUrl);
    }

    /**
     * Gets the Properties object corresponding to the {@value #UNIPROT_DB_CONFIG_FILENAME} config file for configuration of connection to primary uniprot database.
     * The config file is searched under the config profile URL path specified through system property {@value CONFIG_PROFILE_PROPERTY} 
     * @return
     */
    public static Properties getUniprotDbProperties() {
    	return getPropertiesObject(UNIPROT_DB_CONFIG_FILENAME, getProfileUrl());
    }

    /**
     * Gets the PropertiesReader object corresponding to the {@value #UNIPROT_DB_CONFIG_FILENAME} config file for configuration of connection to primary uniprot database.
     * The config file is searched under the config profile URL path specified through system property {@value CONFIG_PROFILE_PROPERTY}
     * @return
     */
    public static PropertiesReader getUniprotDbPropertiesReader() {
        URL profileUrl = getProfileUrl();
        return new PropertiesReader(getPropertiesObject(UNIPROT_DB_CONFIG_FILENAME, profileUrl), UNIPROT_DB_CONFIG_FILENAME, profileUrl);
    }

    /**
     * Gets the Properties object corresponding to the {@value #YOSEMITE_APP_CONFIG_FILENAME} config file for configuration of the yosemite app.
     * The config file is searched under the config profile URL path specified through system property {@value CONFIG_PROFILE_PROPERTY} 
     * @return
     */
    public static Properties getYosemiteAppProperties() {
    	return getPropertiesObject(YOSEMITE_APP_CONFIG_FILENAME, getProfileUrl());
    }

    /**
     * Gets the PropertiesReader object corresponding to the {@value #YOSEMITE_APP_CONFIG_FILENAME} config file for configuration of the yosemite app.
     * The config file is searched under the config profile URL path specified through system property {@value CONFIG_PROFILE_PROPERTY}
     */
    public static PropertiesReader getYosemiteAppPropertiesReader() {
        URL profileUrl = getProfileUrl();
        return new PropertiesReader(getPropertiesObject(YOSEMITE_APP_CONFIG_FILENAME, profileUrl), YOSEMITE_APP_CONFIG_FILENAME, profileUrl);
    }

    /**
     * Gets the Properties object corresponding to the {@value #BORREGO_APP_CONFIG_FILENAME} config file for configuration of the borrego app.
     * The config file is searched under the config profile URL path specified through system property {@value CONFIG_PROFILE_PROPERTY}
     */
    public static Properties getBorregoAppProperties() {
    	return getPropertiesObject(BORREGO_APP_CONFIG_FILENAME, getProfileUrl());
    }

    /**
     * Gets the PropertiesReader object corresponding to the {@value #BORREGO_APP_CONFIG_FILENAME} config file for configuration of the borrego app.
     * The config file is searched under the config profile URL path specified through system property {@value CONFIG_PROFILE_PROPERTY}
     */
    public static PropertiesReader getBorregoAppPropertiesReader() {
        URL profileUrl = getProfileUrl();
        return new PropertiesReader(getPropertiesObject(BORREGO_APP_CONFIG_FILENAME, profileUrl), BORREGO_APP_CONFIG_FILENAME, profileUrl);
    }

    /**
     * Gets the Properties object corresponding to the {@value #SHAPE_APP_CONFIG_FILENAME} config file for configuration of the shape app.
     * The config file is searched under the config profile URL path specified through system property {@value CONFIG_PROFILE_PROPERTY}
     */
    public static Properties getShapeAppProperties() {
        return getPropertiesObject(SHAPE_APP_CONFIG_FILENAME, getProfileUrl());
    }

    /**
     * Gets the PropertiesReader object corresponding to the {@value #SHAPE_APP_CONFIG_FILENAME} config file for configuration of the shape app.
     * The config file is searched under the config profile URL path specified through system property {@value CONFIG_PROFILE_PROPERTY}
     */
    public static PropertiesReader getShapeAppPropertiesReader() {
        URL profileUrl = getProfileUrl();
        return new PropertiesReader(getPropertiesObject(SHAPE_APP_CONFIG_FILENAME, profileUrl), SHAPE_APP_CONFIG_FILENAME, profileUrl);
    }

    /**
     * Gets the Properties object corresponding to the {@value #EVERGLADES_APP_CONFIG_FILENAME} config file for configuration of the indexer app.
     * The config file is searched under the config profile URL path specified through system property {@value CONFIG_PROFILE_PROPERTY}
     */
    public static Properties getEvergladesAppProperties() {
        return getPropertiesObject(EVERGLADES_APP_CONFIG_FILENAME, getProfileUrl());
    }

    /**
     * Gets the PropertiesReader object corresponding to the {@value #EVERGLADES_APP_CONFIG_FILENAME} config file for configuration of the indexer app.
     * The config file is searched under the config profile URL path specified through system property {@value CONFIG_PROFILE_PROPERTY}
     */
    public static PropertiesReader getEvergladesAppPropertiesReader() {
        URL profileUrl = getProfileUrl();
        return new PropertiesReader(getPropertiesObject(EVERGLADES_APP_CONFIG_FILENAME, profileUrl), EVERGLADES_APP_CONFIG_FILENAME, profileUrl);
    }

    /**
     * Gets the Properties object corresponding to the {@value #REDWOOD_APP_CONFIG_FILENAME} config file for configuration of the redwood app.
     * The config file is searched under the config profile URL path specified through system property {@value CONFIG_PROFILE_PROPERTY}
     */
    public static Properties getRedwoodAppProperties() {
        return getPropertiesObject(REDWOOD_APP_CONFIG_FILENAME, getProfileUrl());
    }

    /**
     * Gets the PropertiesReader object corresponding to the {@value #REDWOOD_APP_CONFIG_FILENAME} config file for configuration of the redwood app.
     * The config file is searched under the config profile URL path specified through system property {@value CONFIG_PROFILE_PROPERTY}
     */
    public static PropertiesReader getRedwoodAppPropertiesReader() {
        URL profileUrl = getProfileUrl();
        return new PropertiesReader(getPropertiesObject(REDWOOD_APP_CONFIG_FILENAME, profileUrl), REDWOOD_APP_CONFIG_FILENAME, profileUrl);
    }

    /**
     * Gets the Properties object corresponding to the {@value #ARCHES_APP_CONFIG_FILENAME} config file for configuration of the arches app.
     * The config file is searched under the config profile URL path specified through system property {@value CONFIG_PROFILE_PROPERTY}
     */
    public static Properties getArchesAppProperties() {
        return getPropertiesObject(ARCHES_APP_CONFIG_FILENAME, getProfileUrl());
    }

    /**
     * Gets the PropertiesReader object corresponding to the {@value #ARCHES_APP_CONFIG_FILENAME} config file for configuration of the arches app.
     * The config file is searched under the config profile URL path specified through system property {@value CONFIG_PROFILE_PROPERTY}
     */
    public static PropertiesReader getArchesAppPropertiesReader() {
        URL profileUrl = getProfileUrl();
        return new PropertiesReader(getPropertiesObject(ARCHES_APP_CONFIG_FILENAME, profileUrl), ARCHES_APP_CONFIG_FILENAME, profileUrl);
    }

    /**
     * Gets the PropertiesReader object corresponding to the {@value #PECOS_APP_CONFIG_FILENAME} config file for configuration of the pecos app.
     * The config file is searched under the config profile URL path specified through system property {@value CONFIG_PROFILE_PROPERTY}
     */
    public static PropertiesReader getPecosAppPropertiesReader() {
        URL profileUrl = getProfileUrl();
        return new PropertiesReader(getPropertiesObject(PECOS_APP_CONFIG_FILENAME, profileUrl), PECOS_APP_CONFIG_FILENAME, profileUrl);
    }

    /**
     * Gets the Properties object corresponding to the {@value #PECOS_APP_CONFIG_FILENAME} config file for configuration of the pecos app.
     * The config file is searched under the config profile URL path specified through system property {@value CONFIG_PROFILE_PROPERTY}
     */
    public static Properties getPecosAppProperties() {
        return getPropertiesObject(PECOS_APP_CONFIG_FILENAME, getProfileUrl());
    }

    /**
     * Gets the Properties object corresponding to the {@value #SEQMOTIF_APP_CONFIG_FILENAME} config file for
     * configuration of the shape app. The config file is searched under the config profile URL path specified through
     * system property {@value CONFIG_PROFILE_PROPERTY}
     */
    public static Properties getSemotifAppProperties() {
        return getPropertiesObject(SEQMOTIF_APP_CONFIG_FILENAME, getProfileUrl());
    }

    /**
     * Gets the PropertiesReader object corresponding to the {@value #SEQMOTIF_APP_CONFIG_FILENAME} config file for
     * configuration of the seqmotif app. The config file is searched under the config profile URL path specified
     * through system property {@value CONFIG_PROFILE_PROPERTY}
     */
    public static PropertiesReader getSeqmotifAppPropertiesReader() {
        URL profileUrl = getProfileUrl();
        return new PropertiesReader(getPropertiesObject(SEQMOTIF_APP_CONFIG_FILENAME, profileUrl), SEQMOTIF_APP_CONFIG_FILENAME, profileUrl);
    }

   /**
    * Gets the PropertiesReader object corresponding to the {@value #DOWNLOAD_APP_CONFIG_FILENAME} config file for configuration of the download app.
    * The config file is searched under the config profile URL path specified through system property {@value CONFIG_PROFILE_PROPERTY}
    */
   public static PropertiesReader getDownloadAppPropertiesReader() {
      URL profileUrl = getProfileUrl();
      return new PropertiesReader(getPropertiesObject(DOWNLOAD_APP_CONFIG_FILENAME, profileUrl), DOWNLOAD_APP_CONFIG_FILENAME, profileUrl);
   }

   /**
    * Gets the Properties object corresponding to the {@value #DOWNLOAD_APP_CONFIG_FILENAME} config file for configuration of the download app.
    * The config file is searched under the config profile URL path specified through system property {@value CONFIG_PROFILE_PROPERTY}
    */
   public static Properties getDownloadAppProperties() {
      return getPropertiesObject(DOWNLOAD_APP_CONFIG_FILENAME, getProfileUrl());
   }

    /**
     * Gets the content of a pdb.properties file as it gets used by the legacy PDB webapp.
     */
    public static Properties getLegacyPdbProperties() {
        return getPropertiesObject(PDB_PROPERTIES_FILENAME, getProfileUrl());
    }


    /**
     * Gets the build properties from file {@value #BUILD_PROPERTIES_FILENAME} placed at the root of the resources dir.
     * The file should contain project.version, build.hash and build.timestamp variables populated by maven at buildtime.
     */
    public static Properties getBuildProperties() {
    	InputStream propstream = Thread.currentThread().getContextClassLoader().getResourceAsStream(BUILD_PROPERTIES_FILENAME);    	
    	Properties props = new Properties();
    	try {
    		props.load(propstream);
    	} catch (IOException e) {
    		LOGGER.warn(
                "Could not get the build properties from {} file! Build information will not be available.",
                BUILD_PROPERTIES_FILENAME, e
            );
    	}
        return props;
    }
}
