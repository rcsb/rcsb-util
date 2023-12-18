package org.rcsb.common.io;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A FileVisitor that will walk through a directory tree storing all files found within a
 * structure/ligand directory into a map.
 * Metadata directories specifed in {@link #DIRS_TO_SKIP} (at the moment
 * CVS and .git) are skipped.
 *
 * @author Jose Duarte
 * @since 1.1.0
 */
public class SandboxFileVisitor extends SimpleFileVisitor<Path> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SandboxFileVisitor.class);

    private static final String[] DIRS_TO_SKIP = {"CVS", ".git"};

    private SortedMap<String, Map<String, File>> map;

    private boolean isStructureDir;

    private static final Pattern structureIdPattern = Pattern.compile("^\\d\\w\\w\\w$");
    private static final Pattern ligandIdPattern = Pattern.compile("^\\w{1,3}$");

    /**
     * Create a new SandboxFileVisitor
     *
     * @param isStructureDir if true the walk will be treated as a structure directory walk
     *                       if false the walk will be treated as a ligand directory walk
     */
    public SandboxFileVisitor(boolean isStructureDir) {
        this.isStructureDir = isStructureDir;
        map = new TreeMap<>();
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {


        if (file.toFile().isFile()) {
            String parentDirName = new File(file.toFile().getParent()).getName().toUpperCase();

            Pattern p = null;
            if (isStructureDir) {
                p = structureIdPattern;
            } else {
                p = ligandIdPattern;
            }

            Matcher m = p.matcher(parentDirName);
            if (m.matches()) {
                Map<String, File> files = null;
                if (map.containsKey(parentDirName)) {
                    files = map.get(parentDirName);
                } else {
                    files = new HashMap<>();
                    map.put(parentDirName, files);
                }
                files.put(file.toFile().getName(), file.toFile());

            }

        }

        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) {
        LOGGER.warn("Could not get info for file {}. Problem: {}", file.toString(), exc.getMessage());
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
        if (skipDir(dir)) {
            return FileVisitResult.SKIP_SUBTREE;
        } else {
            return FileVisitResult.CONTINUE;
        }
    }

    /**
     * Get the Set of unique file names that match the pattern.
     * @return the set of file names
     */
    public SortedMap<String, Map<String, File>> getAllFilesMap() {
        return map;
    }

    private static boolean skipDir(Path dir) {
        Path name = dir.getFileName();
        return Arrays.stream(DIRS_TO_SKIP)
            .map(pattern -> FileSystems.getDefault().getPathMatcher("glob:" + pattern))
            .anyMatch(m -> name != null && m.matches(name));
    }

}

