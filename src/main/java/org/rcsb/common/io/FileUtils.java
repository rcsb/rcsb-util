package org.rcsb.common.io;

import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPOutputStream;

/**
 * Some file utils of utility methods not found easily in Java 8.
 * 
 * @author Jose Duarte
 * @since 1.1.0
 */
public class FileUtils {

   

   /**
    * Writes the given input stream to a gzipped file.
    * The user is responsible of closing the input stream.
    * @param inStream the input stream
    * @param file the file to write to
    * @throws IOException if something goes wrong while writing file out
    */
    public static void writeGzipFile(InputStream inStream, File file) throws IOException {
        // we use the try-with resources and autocloseable feature of java 7 here
        try (
                OutputStream os = new GZIPOutputStream(new FileOutputStream(file));) {
            byte[] bytes = new byte[1024];
            int length;
            while ((length = inStream.read(bytes)) != -1) {
                os.write(bytes, 0, length);
            }
        }
    }

    /**
     * Find the unique file names within a directory tree that match the given pattern (either
     * a glob or a regex). Metadata dirs .git and CVS are ignored.
     * @param dir the directory where the search starts, all children dirs will be inspected
     * @param pattern the pattern: starting with "glob:" or "regex:" depending of kind of pattern wanted
     * @return a Set of file names matching the pattern
     * @throws IOException if input dir is not readable or if something goes wrong while traversing the directory tree
     */
    public static Set<String> findFiles(File dir, String pattern) throws IOException {
       if (!dir.exists() || !dir.isDirectory()) {
          throw new IOException("Input dir argument '"+dir+"' is either not readable or not a directory");
       }
        FileFinder finder = new FileFinder(pattern);
        Files.walkFileTree(dir.toPath(), finder);
        return finder.getUniqueMatchingFiles();
    }

    /**
     * Given a Set of Strings (usually representing file names) returns a new Set
     * with the given extension chopped.
     * @param set the input set of strings
     * @param extension the extension to chop (must contain the '.' if it must be chopped too). It will be matched
     *                  with the same case as input (no case insensitive matching)
     * @param forceUpper if false the output String will be forced to lower case, if true to upper case
     * @return a Set of Strings with extensions chopped
     */
    public static Set<String> chopExtensions(Set<String> set, String extension, boolean forceUpper) {
        Set<String> newset = new TreeSet<>();
        for (String name : set) {
            if (name.endsWith(extension)) {
                int startIndex = name.lastIndexOf(extension);
                String str = name.substring(0,startIndex);
                if (forceUpper)
                   str = str.toUpperCase();
                else
                   str = str.toLowerCase();

                newset.add(str);
            }
        }
        return newset;
    }

   /**
    * Given a Set of Strings (usually representing file names) returns a new Set
    * with the given extension (expressed as a regex) chopped.
    * @param set the input set of strings
    * @param regex a regex with the extension to chop (must contain the '.' if it must be chopped too). Note that
    *              '.' will have to be escaped with "\\."
    * @param forceUpper if false the output String will be forced to lower case, if true to upper case
    * @return a Set of Strings with extensions chopped
    */
   public static Set<String> chopExtensionsRegex(Set<String> set, String regex, boolean forceUpper) {

      if (regex.contains("(") || regex.contains(")")) throw new IllegalArgumentException("Regexes that contain parenthesis are not supported!");

      Set<String> newset = new TreeSet<>();

      // first we make it a capturing regex
      Pattern pattern = Pattern.compile("^(.*)(" + regex + ")$");

      for (String name : set) {
         Matcher m = pattern.matcher(name);
         if (m.matches()) {
            String str = m.group(1);
            if (forceUpper)
               str = str.toUpperCase();
            else
               str = str.toLowerCase();
            newset.add(str);
         }
      }
      return newset;
   }

   /**
    * Gets a map containing all structure/ligand files in given directory and all subdirs below it.
    *
    * @param dir the directory where to start the crawl
    *
    * @param isStructureDir if true the walk will be treated as a structure directory walk
    *                       if false the walk will be treated as a ligand directory walk
    *
    * @return a map of ids (e.g. structure ids or ligand id) to a map of file names to file objects
    */
   public static SortedMap<String, Map<String, File>> getAllFilesMap(File dir, boolean isStructureDir) throws IOException {


      SandboxFileVisitor visitor = new SandboxFileVisitor(isStructureDir);
      Files.walkFileTree(dir.toPath(), visitor);

      return visitor.getAllFilesMap();
   }

}
