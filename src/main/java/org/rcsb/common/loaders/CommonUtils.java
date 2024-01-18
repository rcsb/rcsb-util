package org.rcsb.common.loaders;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Created by ap3 on 08/11/2016.

 * @deprecated These methods are highly specific and not needed
 */
@Deprecated(since="1.9.0", forRemoval = true)
public final class CommonUtils {

    private CommonUtils() {
    }

    /**
     * Read a file of ids (e.g. pdb ids, or ligand ids). Only the first token (before any spaces) of each line will be read.
     * Empty lines and lines starting with '#' are ignored.
     *
     * All IDs will be returned as upper-case IDs.
     *
     * @param maxToRead the maximum number of identifiers to read or -1 to read all
     * @throws IOException
     */
    public static SortedSet<String> readIdsFromFile(File file, int maxToRead) throws IOException {
        SortedSet<String> ids = new TreeSet<>();

        try (BufferedReader br = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
            String line;
            while ( (line = br.readLine()) != null && (maxToRead <= 0 || ids.size() < maxToRead)) {
                if (line.startsWith("#")) continue;
                if (line.isEmpty()) continue;
                String[] tokens = line.split("\\s+");
                ids.add(tokens[0].toUpperCase());
            }
        }
        return ids;
    }
}
