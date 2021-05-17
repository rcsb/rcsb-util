package org.rcsb.common.io;

import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Created by jose on 6/10/16.
 *
 * @author Jose Duarte
 */
public class TestFileUtils {

    private static final String extension = "sdf";
    private static final File tmpDir = new File(System.getProperty("java.io.tmpdir"));

    private static Path rootDirFinfFilesTest;

    @BeforeClass
    public static void setupBeforeClass() throws IOException {

        rootDirFinfFilesTest = Files.createTempDirectory(tmpDir.toPath(), "pdbwebapp_testDir_findfiles");
        rootDirFinfFilesTest.toFile().deleteOnExit();

    }

    private void createFindFilesTestFiles() throws IOException {

        // creating a bunch of dirs and files for the testing of FileFinder

        // 25 directories with 25 AAA.sdf, 25 AA.sdf and 1 ABCD.sdf files per dir
        for (char i='A';i<'Z';i++) {
            File subDir = new File(rootDirFinfFilesTest.toFile(), String.valueOf(i));
            subDir.deleteOnExit();
            subDir.mkdirs();


            for (char j ='A';j<'Z';j++) {
                StringBuilder sb = new StringBuilder();
                sb.append(j);
                sb.append(j);
                createFile(subDir, sb.toString()+"."+extension);
                sb.append(j);
                createFile(subDir, sb.toString()+"."+extension);
            }

            // adding a 4-letter file in each subdir
            createFile(subDir, "ABCD."+extension);
        }

        // an extra empty dir: this dir shouldn't be counted as a file match
        File subDir = new File(rootDirFinfFilesTest.toFile(), "ABC."+extension);
        subDir.mkdirs();
        subDir.deleteOnExit();

        // 2 extra metadata dirs: these dirs should be skipped
        File cvsDir = new File(rootDirFinfFilesTest.toFile(), "CVS");
        cvsDir.mkdirs();
        cvsDir.deleteOnExit();
        createFile(cvsDir, "ABC."+extension);
        File gitDir = new File(rootDirFinfFilesTest.toFile(), ".git");
        gitDir.mkdirs();
        gitDir.deleteOnExit();
        createFile(gitDir, "ABC."+extension);
    }

    @Test
    public void testFileFinder() throws IOException {

        createFindFilesTestFiles();

        FileFinder finder = new FileFinder("glob:*."+extension);

        Files.walkFileTree(rootDirFinfFilesTest, finder);

        List<String> list = finder.getMatchingFiles();

        assertTrue(list.contains("AAA."+extension));
        assertFalse(list.contains("ABC."+extension));

        // 26 files in 25 dirs
        assertEquals(1275, list.size());

        // only 25 unique files
        Set<String> set = finder.getUniqueMatchingFiles();

        assertEquals(51, set.size());

        // trying the ??? glob
        finder = new FileFinder("glob:???."+extension);
        Files.walkFileTree(rootDirFinfFilesTest, finder);
        list = finder.getMatchingFiles();
        assertEquals(625, list.size());
        set = finder.getUniqueMatchingFiles();
        assertEquals(25, set.size());
        assertFalse(set.contains("ABCD."+extension));


        // trying a regex pattern
        finder = new FileFinder("regex:\\w{1,3}"+ Pattern.quote("."+extension));
        Files.walkFileTree(rootDirFinfFilesTest, finder);
        list = finder.getMatchingFiles();
        assertEquals(1250, list.size());
        set = finder.getUniqueMatchingFiles();
        assertEquals(50, set.size());
        assertFalse(set.contains("ABCD."+extension));

        // test of FileUtils.findFiles wrapper
        Set<String> set2 = FileUtils.findFiles(rootDirFinfFilesTest.toFile(), "regex:\\w{1,3}"+ Pattern.quote("."+extension));
        assertEquals(50, set2.size());
        assertFalse(set2.contains("ABCD."+extension));

    }

    @Test
    public void testChopExtensions() {
        Set<String> set = new TreeSet<>();
        set.add("ABC.sdf.gz");
        set.add("BCD.sdf.gz");
        set.add("CD.sdf.gz");
        set.add("D.sdf.gz");


        Set<String> chopped = FileUtils.chopExtensions(set, ".sdf.gz", true);
        assertEquals(4, chopped.size());
        assertTrue(chopped.contains("ABC"));
        assertTrue(chopped.contains("BCD"));
        assertTrue(chopped.contains("CD"));
        assertTrue(chopped.contains("D"));

        set = new TreeSet<>();
        set.add("1SMT-deriv.cif.gz");
        chopped = FileUtils.chopExtensions(set, "-deriv.cif.gz", true);
        assertTrue(chopped.contains("1SMT"));

        set = new TreeSet<>();
        set.add("2n8b_cs.str.gz");
        chopped = FileUtils.chopExtensions(set, "_cs.str.gz", true);
        assertTrue(chopped.contains("2N8B"));
    }

    @Test
    public void testChopExtensionsRegex() {
        Set<String> set = new TreeSet<>();
        set.add("1SMT-assembly1.cif.gz");
        set.add("2TRX-assembly3.cif.gz");
        set.add("3HBX-assembly456.cif.gz");
        set.add("HEM-assembly2.cif.gz");

        Set<String> chopped = FileUtils.chopExtensionsRegex(set, "-assembly\\d+"+Pattern.quote(".cif.gz"), true);
        assertTrue(chopped.contains("1SMT"));
        assertTrue(chopped.contains("2TRX"));
        assertTrue(chopped.contains("3HBX"));
        assertTrue(chopped.contains("HEM"));
    }

    @Test
    public void testForUpperLower() {
        Set<String> set = new TreeSet<>();
        set.add("1SMT-assembly1.cif.gz");
        set.add("2trx-assembly3.cif.gz");

        Set<String> chopped = FileUtils.chopExtensionsRegex(set, "-assembly\\d+"+Pattern.quote(".cif.gz"), true);
        assertTrue(chopped.contains("1SMT"));
        assertFalse(chopped.contains("1smt"));

        assertTrue(chopped.contains("2TRX"));
        assertFalse(chopped.contains("2trx"));

        set = new TreeSet<>();
        set.add("ABC.sdf.gz");
        set.add("Bcd.sdf.gz");
        set.add("cd.sdf.gz");
        set.add("D.sdf.gz");

        chopped = FileUtils.chopExtensions(set, ".sdf.gz", false);
        assertEquals(4, chopped.size());
        assertTrue(chopped.contains("abc"));
        assertTrue(chopped.contains("bcd"));
        assertTrue(chopped.contains("cd"));
        assertTrue(chopped.contains("d"));

        set = new TreeSet<>();
        set.add("ABC.sdf.gz");
        set.add("Bcd.sdf.gz");
        set.add("cd.sdf.gz");
        set.add("D.sdf.gz");

        chopped = FileUtils.chopExtensions(set, ".sdf.gz", true);
        assertEquals(4, chopped.size());
        assertTrue(chopped.contains("ABC"));
        assertTrue(chopped.contains("BCD"));
        assertTrue(chopped.contains("CD"));
        assertTrue(chopped.contains("D"));

    }

    private static void createFile(File dir, String name) throws  IOException{
        File f = new File(dir, name);
        FileWriter fw = new FileWriter(f);
        fw.write("The file content");
        fw.close();
        f.deleteOnExit();
    }

 
}
