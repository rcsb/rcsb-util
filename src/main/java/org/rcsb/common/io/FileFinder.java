package org.rcsb.common.io;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

/**
 * A FileVisitor that will match the given glob pattern,
 * skipping metadata directories specifed in {@link #DIRS_TO_SKIP} (at the moment
 * CVS and .git)
 *
 * @author Jose Duarte
 * @since 1.1.0
 */
public class FileFinder extends SimpleFileVisitor<Path> {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileFinder.class);

    private static final String[] DIRS_TO_SKIP = {"CVS", ".git"};

    private final PathMatcher matcher;
    private List<String> matchingFiles;

    /**
     * Create a new FileFinder given a pattern (either a glob or a regex pattern)
     * @param pattern the pattern, must start with either "glob:" or "regex:" depending of
     *                what kind of pattern is wanted
     *
     * @throws IllegalArgumentException if the pattern doesn't start with "glob:" or "regex:"
     */
    public FileFinder(String pattern) {
        if (!pattern.startsWith("glob:") && !pattern.startsWith("regex:")) {
            throw new IllegalArgumentException("Pattern must start with 'glob:' or 'regex:'");
        }
        matcher = FileSystems.getDefault().getPathMatcher(pattern);
        matchingFiles = new ArrayList<>();
    }

    /**
     * Match against the glob pattern
     * @param file the file
     * @return true if matches, false otherwise
     */
    private boolean matches(Path file) {
        Path name = file.getFileName();
        return (name != null && matcher.matches(name));
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
        if (matches (file)) {
            matchingFiles.add(file.toFile().getName());
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
     * Get a list containing all file names that match the pattern, possibly
     * containing duplicates.
     * @return the list of file names
     */
    public List<String> getMatchingFiles() {
        return matchingFiles;
    }

    /**
     * Get the Set of unique file names that match the pattern.
     * @return the set of file names
     */
    public SortedSet<String> getUniqueMatchingFiles() {
        return new TreeSet<>(matchingFiles);
    }

    private static boolean skipDir(Path dir) {
        Path name = dir.getFileName();
        for (String pattern : DIRS_TO_SKIP) {
            PathMatcher m = FileSystems.getDefault().getPathMatcher("glob:" + pattern);
            if (name!=null && m.matches(name)) {
                return true;
            }
        }
        return false;
    }

}
