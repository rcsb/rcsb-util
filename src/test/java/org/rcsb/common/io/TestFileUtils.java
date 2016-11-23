package org.rcsb.common.io;

import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

/**
 * Created by jose on 6/10/16.
 *
 * @author Jose Duarte
 */
public class TestFileUtils {

    private static final String extension = "sdf";
    private static final File tmpDir = new File(System.getProperty("java.io.tmpdir"));

    private static Path rootDirFinfFilesTest;
    private static Path rootDirSandboxFileVisitorTest;

    private static File structsDir;
    private static File ligandsDir;

    private static int totalStructs = 0;
    private static int totalLigands = 0;


    @BeforeClass
    public static void setupBeforeClass() throws IOException {

        rootDirFinfFilesTest = Files.createTempDirectory(tmpDir.toPath(), "pdbwebapp_testDir_findfiles");
        rootDirFinfFilesTest.toFile().deleteOnExit();

        rootDirSandboxFileVisitorTest = Files.createTempDirectory(tmpDir.toPath(), "pdbwebapp_testDir_sandbox");
        rootDirSandboxFileVisitorTest.toFile().deleteOnExit();

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

    private void createStructuresDir() throws IOException {
        // a full structure ids subdir in same layout as sandbox
        structsDir = new File(rootDirSandboxFileVisitorTest.toFile(), "structures");
        structsDir.deleteOnExit();
        for (char i='A';i<'Z';i++) {
            for (char j='A';j<'Z';j++) {
                String hash = String.valueOf(i) + String.valueOf(j);
                File dir = new File(structsDir, hash);
                dir.mkdirs();
                dir.deleteOnExit();

                for (int k=1; k<=9; k++) {
                    totalStructs++;
                    String fakePdbId = k+hash+"A";
                    File singleStructDir = new File(dir, fakePdbId);
                    singleStructDir.mkdirs();
                    singleStructDir.deleteOnExit();
                    createFile(singleStructDir, fakePdbId + ".pdb1.gz");
                    createFile(singleStructDir, fakePdbId + ".cif.gz");
                    createFile(singleStructDir, "pdb" + fakePdbId + ".ent.gz");
                }
            }
        }

    }

    private void createLigandsDir() throws IOException {
        // a full ligand ids subdir in same layout as sandbox
        ligandsDir = new File(rootDirSandboxFileVisitorTest.toFile(), "ligands");
        ligandsDir.deleteOnExit();
        for (char i='A';i<'Z';i++) {
            String hash = String.valueOf(i);
            File dir = new File(ligandsDir, hash);
            dir.deleteOnExit();
            dir.mkdirs();
            for (int k=1; k<=9; k++) {
                totalLigands++;
                String fakeLigandId = String.valueOf(i)+String.valueOf(k)+String.valueOf(k).toUpperCase();
                File singleLigandDir = new File(dir, fakeLigandId);
                singleLigandDir.mkdirs();
                singleLigandDir.deleteOnExit();
                createFile(singleLigandDir, fakeLigandId+".cif.gz");
                createFile(singleLigandDir, fakeLigandId+".sdf.gz");
                createFile(singleLigandDir, fakeLigandId+"-100.jpg");

            }
        }

    }

    @Test
    public void testWriteGzipFile() throws IOException {
        File f = File.createTempFile("RCSBwebapptest",".txt.gz");
        f.deleteOnExit();

        String contents = "hola a todos";

        FileUtils.writeGzipFile(new ByteArrayInputStream(contents.getBytes()), f);


        // let's read the raw file, we should get binary stuff
        BufferedReader br = new BufferedReader(new FileReader(f));

        StringBuilder sb = new StringBuilder();
        String line;
        while ( ( line = br.readLine()) !=null ) {
            sb.append(line);
        }
        br.close();


        assertFalse(contents.equals(sb.toString()));


        // and now let's read it properly through gzip and get the text
        sb = new StringBuilder();
        InputStream is = new GZIPInputStream(new FileInputStream(f));
        BufferedReader br2 = new BufferedReader(new InputStreamReader(is));

        while ( ( line = br2.readLine()) !=null ) {
            sb.append(line);
        }
        br2.close();

        assertEquals(contents, sb.toString());
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
    public void testSandboxFileVisitor() throws IOException {

        if (structsDir==null)
            createStructuresDir();
        if (ligandsDir==null)
            createLigandsDir();

        SandboxFileVisitor visitor = new SandboxFileVisitor(true);
        Files.walkFileTree(structsDir.toPath(), visitor);
        SortedMap<String, Map<String, File>> map = visitor.getAllFilesMap();

        assertEquals(totalStructs, map.size());

        for (Map.Entry<String, Map<String,File>> entry : map.entrySet()) {
            String key = entry.getKey();
            Map<String,File> submap = entry.getValue();
            // 3 matches the number of files we created above
            assertEquals(3,submap.size());
            // the names here match those of files created above
            assertTrue(submap.containsKey(key+".pdb1.gz"));
            assertTrue(submap.containsKey(key+".cif.gz"));
            assertTrue(submap.containsKey("pdb"+key+".ent.gz"));
        }

        visitor = new SandboxFileVisitor(false);
        Files.walkFileTree(ligandsDir.toPath(), visitor);
        map = visitor.getAllFilesMap();

        assertEquals(totalLigands, map.size());

        for (Map.Entry<String, Map<String,File>> entry : map.entrySet()) {
            String key = entry.getKey();
            Map<String,File> submap = entry.getValue();
            // 3 matches the number of files we created above
            assertEquals(3,submap.size());
            // the names here match those of files created above
            assertTrue(submap.containsKey(key+".cif.gz"));
            assertTrue(submap.containsKey(key+".sdf.gz"));
            assertTrue(submap.containsKey(key+"-100.jpg"));
        }


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
