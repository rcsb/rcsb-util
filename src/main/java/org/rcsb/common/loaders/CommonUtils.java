package org.rcsb.common.loaders;

import org.rcsb.common.config.ConfigProfileManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Created by ap3 on 08/11/2016.
 */
public class CommonUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommonUtils.class);


    public static  int getNrThreads(){
        Properties props = ConfigProfileManager.getSequoiaAppProperties();

        String t = (String) props.get("sequoia.threads");

        int availableProcs = Runtime.getRuntime().availableProcessors();

        if ( t == null)
            return availableProcs;

        Integer threads = Integer.parseInt(t);

        if ( threads == null || threads < 1){
            return availableProcs;
        }

        else if ( threads > availableProcs)
            return availableProcs;

        return threads;
    }


    /** read all PDB IDs from the local.pdb.list file
     *
     * @return
     */
    public static SortedSet<String> getPdbIds(){

        Properties props = ConfigProfileManager.getSequoiaAppProperties();

        String pdbList = (String) props.get("local.pdb.list");

        SortedSet<String> pdbIds = new TreeSet<>();
        try {
            File f = new File(pdbList);

            LOGGER.info("reading file " + f.getAbsolutePath());

            pdbIds = readIdsFromFile(f, -1);

        } catch (IOException e) {

            LOGGER.error("Could not read ids from file",e);
        }

        LOGGER.info("got " + pdbIds.size() + " IDs from file.");

        return pdbIds;
    }


    /**
     * Read a file of ids (e.g. pdb ids, or ligand ids). Only the first token (before any spaces) of each line will be read.
     * Empty lines and lines starting with '#' are ignored.
     *
     * All IDs will be returned as upper-case IDs.
     *
     * @param file
     * @param maxToRead the maximum number of identifiers to read or -1 to read all
     * @return
     * @throws IOException
     */
    public static SortedSet<String> readIdsFromFile(File file, int maxToRead) throws IOException {
        SortedSet<String> ids = new TreeSet<>();


        try (BufferedReader br = new BufferedReader(new FileReader(file));){

            String line;
            while ( (line=br.readLine())!=null) {
                if (line.startsWith("#")) continue;
                if (line.isEmpty()) continue;
                String[] tokens = line.split("\\s+");
                ids.add(tokens[0].toUpperCase());

                if (maxToRead>0 && ids.size()>=maxToRead) {
                    break;
                }
            }

            br.close();
        }
        return ids;
    }





}
