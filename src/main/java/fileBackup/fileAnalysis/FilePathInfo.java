package fileBackup.fileAnalysis;

import fileBackup.monitoring.pathMapping.PathMappingStrategy;
import fileBackup.monitoring.persistence.converters.PathConverter;
import io.vavr.Tuple2;
import io.vavr.collection.List;
import io.vavr.control.Either;

import javax.persistence.*;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

/**
 * Contains the required path information for the program to begin performing a valid backup. It is also persisted
 * when running live file monitoring such that the saved monitored files are associated to their backup path information.
 *
 * <p>Additional methods are provided to map between file systems so versions of each file can be compared for
 * existence or modification.</p>
 *
 * <p>This object is the foundation of the entire application and therefore cannot be constructed in an illegal state
 * since its replied upon containing valid paths by many application classes.</p>
 *
 * Created by matt on 30-Jun-17.
 */
@Entity
public class FilePathInfo {
    @Id
    @GeneratedValue(generator = "FilePathInfo_SeqGen", strategy = GenerationType.AUTO)
    @SequenceGenerator(name = "FilePathInfo_SeqGen", sequenceName = "FilePathInfo_Seq", allocationSize = 1)
    private int id;

    @Column(nullable = false)
    @Convert(converter = PathConverter.class)
    private Path currentWorkingRootPath;

    @Column(nullable = false)
    @Convert(converter = PathConverter.class)
    private Path backupRootPath;

    @Column(nullable = false)
    private String rootDirectoryName;

    private static final PathMappingStrategy pathMappingStrategy = PathMappingStrategy.create();

    /**
     * For hibernate only.
     */
    public FilePathInfo() {}

    public Path getBackupRootPath() {
        return backupRootPath;
    }

    public Path getCurrentWorkingRootPath() {
        return currentWorkingRootPath;
    }

    public String getRootDirectoryName() {
        return rootDirectoryName;
    }

    private FilePathInfo(Path currentWorkingRootPath, Path backupRootPath, String rootDirectoryName) {
        this.currentWorkingRootPath = currentWorkingRootPath;
        this.backupRootPath = backupRootPath;
        this.rootDirectoryName = rootDirectoryName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FilePathInfo that = (FilePathInfo) o;

        if (currentWorkingRootPath != null ? !currentWorkingRootPath.equals(that.currentWorkingRootPath) : that.currentWorkingRootPath != null)
            return false;
        if (backupRootPath != null ? !backupRootPath.equals(that.backupRootPath) : that.backupRootPath != null)
            return false;
        return rootDirectoryName != null ? rootDirectoryName.equals(that.rootDirectoryName) : that.rootDirectoryName == null;
    }

    @Override
    public int hashCode() {
        int result = currentWorkingRootPath != null ? currentWorkingRootPath.hashCode() : 0;
        result = 31 * result + (backupRootPath != null ? backupRootPath.hashCode() : 0);
        result = 31 * result + (rootDirectoryName != null ? rootDirectoryName.hashCode() : 0);
        return result;
    }

    /**
     * Supplied {@code Path}s must be existing directories prior to constructing an instance.
     * These can't be validated here since unit testing uses dummy paths to test path structure rather than access
     * underlying {@code File}. The {@code FileValidator} isolates this logic to make class easily testable.
     *
     * @param currentWorkingRootPath The current working root directory.
     * @param backupRootPath The backup root directory.
     * @param fileValidator How to validate if a {@code File} exists.
     * @return {@code Either.Left} explaining the error otherwise {@code Either.Right} contains a valid {@code FilePathInfo}.
     */
    public static Either<String, FilePathInfo> of(Path currentWorkingRootPath, Path backupRootPath, FileValidator fileValidator) {
        if (currentWorkingRootPath == null || backupRootPath == null) {
            return Either.left("Both paths to check must be non null");
        }
        if (!fileValidator.fileExists(currentWorkingRootPath)) {
            return Either.left("Current path does not exist");
        }
        if (!fileValidator.fileExists(backupRootPath)) {
            return Either.left("Backup path does not exist");
        }
        if (currentWorkingRootPath.equals(backupRootPath)) {
            return Either.left("Current and backup path must be different");
        }
        Either<String, String> matchingRootDirectory = getMatchingRootDirectory(currentWorkingRootPath, backupRootPath);
        return matchingRootDirectory
                .map(rootDirectoryName -> new FilePathInfo(currentWorkingRootPath, backupRootPath, rootDirectoryName))
                .orElse(() -> Either.left(matchingRootDirectory.getLeft()));
    }

    public PathMappingStrategy getPathMappingStrategy() {
        return pathMappingStrategy;
    }

    /**
     * Maps the current file to what its representation would be on the backup for comparison.
     *
     * <p> <b>Example:</b>
     * Given the {@code currentPath}, get its trailing path segments from {@code this.rootDirectoryName} onwards
     * \FolderA\FolderB\document.txt and append to the {@code backupRootPath}.
     *
     * <pre>
     *     currentPath = C:\Users\me\Desktop\rootDirectoryName\FolderA\FolderB\document.txt
     *     leading segments = C:\Users\me\Desktop\
     *     trailing segments = \FolderA\FolderB\document.txt
     *
     *     currentWorkingRootPath = C:\FolderA\FolderB\rootDirectoryName
     *     backupRootPath         = F:\FolderC\rootDirectoryName
     *
     *     result = backupRootPath + trailing segments
     *            = F:\FolderC\rootDirectoryName\FolderA\FolderB\document.txt
     * </pre>
     *
     * @param currentPath The full current file path
     * @return The converted to backup path
     */
    public Path fromCurrentToBackupPath(Path currentPath) {
        String backupPath = getPathComponents(currentPath)
                .map2(trailingSegments -> backupRootPath.toString() + trailingSegments)
                ._2;
        return Paths.get(backupPath);
    }

    /**
     * Maps the backup file to what its representation would be on the current drive for comparison.
     *
     * <p> <b>Example:</b>
     * Given the {@code backupPath}, get its trailing path segments from {@code this.rootDirectoryName} onwards
     * \FolderA\FolderB\document.txt and append to the {@code currentWorkingRootPath}.
     *
     * <pre>
     *     backupPath = F:\FolderC\rootDirectoryName\FolderA\FolderB\document.txt
     *     leading segments = F:\FolderC\
     *     trailing segments = \FolderA\FolderB\document.txt
     *
     *     currentWorkingRootPath = C:\FolderA\FolderB\rootDirectoryName
     *     backupRootPath         = F:\FolderC\rootDirectoryName
     *
     *     result = currentWorkingRootPath + trailing segments
     *            = C:\FolderA\FolderB\rootDirectoryName\FolderA\FolderB\document.txt
     * </pre>
     *
     * @param backupPath The full current file path
     * @return The converted to backup path
     */
    public Path fromBackupToCurrentPath(Path backupPath) {
        String currentPath = getPathComponents(backupPath)
                .map2(trailingSegments -> currentWorkingRootPath.toString() + trailingSegments)
                ._2;
        return Paths.get(currentPath);
    }

    /**
     * Splits the supplied path into leading and trailing segments where leading is defined as all path segments
     * before the first {@code rootDirectoryName} and trailing includes all segments after the first {@code rootDirectoryName}.
     * It is possible there could be multiple occurrences of {@code rootDirectoryName} which is why the first
     * occurrence is used since it denotes the top most root directory.
     *
     * <p>this {@code FilePathInfo} can only be constructed in a valid state, therefore it is guaranteed all paths
     * are valid.
     *
     * <p> <b>Example:</b>
     * {@code rootDirectoryName} is the highest directory used to create the sub segments. Note that trailing and
     * leading path separators are added to each segment to make it easier to append back in the {@code rootDirectoryName}.
     *
     * <pre>
     *     path = C:\Users\me\Desktop\rootDirectoryName\FolderA\FolderB\document.txt
     *
     *     C:\Users\me\Desktop\   rootDirectoryName    \FolderA\FolderB\document.txt
     *           leading            span boundary               trailing
     * </pre>
     *
     * @param path The path to split into segments.
     * @return {@code Tuple<leading segment, trailing segment>}.
     */
    public Tuple2<String, String> getPathComponents(Path path) {
        Function<List<Path>, String> leadingMapper = leadingMapper().apply(path.getRoot().toString());
        Predicate<Path> firstRootDirectory = segment -> !segment.toString().equals(rootDirectoryName);

        return List.ofAll(StreamSupport.stream(path.spliterator(), false))
                .span(firstRootDirectory)
                .map(Function.identity(), List::pop) // remove rootDirectoryName
                .map(leadingMapper, trailingMapper());
    }

    /**
     * If the path segment list is empty only the root path is returned, otherwise the full leading path and
     * additional file separator is returned.
     *
     * @return A function that accepts the root path and returns another function accepting a list of path segments.
     */
    private Function<String, Function<List<Path>, String>> leadingMapper() {
        return root -> pathSegments -> {
            if (pathSegments.isEmpty()) return root;
            return root + toStringPath(pathSegments) + File.separator;
        };
    }

    /**
     * If the path segment list is empty then an empty String is returned. Otherwise the trailing path with a prepended
     * file separator is returned.
     *
     * @return A function that accepts the list of path segments and returns the trailing path.
     */
    private Function<List<Path>, String> trailingMapper() {
        return pathSegments -> {
            if (pathSegments.isEmpty()) return "";
            return File.separator + toStringPath(pathSegments);
        };
    }

    private String toStringPath(List<Path> pathSegments) {
        return pathSegments
                .map(Objects::toString)
                .collect(Collectors.joining(File.separator));
    }

    /**
     * Checks if both paths have the same last path segment. Root directories such as 'C:/' are not permitted meaning
     * there must be at least 1 path segment after the root drive such as C:/backup. This is to prevent copying the
     * entire drive over which is not practical.
     *
     * <p>This is to verify the current working and backup version begin at the same root directory as it is assumed from
     * that point on all directory structures are the same.
     *
     * @param a The first path
     * @param b The second path
     * @return {@code Either.Left} containing the error of why the 2 paths don't match, otherwise {@code Either.Right}
     * containing the matching last segment.
     */
    private static Either<String, String> getMatchingRootDirectory(Path a, Path b) {
        return getLastSegmentInPath(a)
                .flatMap(lastA -> getLastSegmentInPath(b).flatMap(lastB -> {
                    if (lastA.equals(lastB) && !lastA.isEmpty()) {
                        return Either.right(lastA);
                    } else {
                        return Either.left(String.format("'%s' and '%s' do not match", lastA, lastB));
                    }
                }));
    }

    /**
     * @param path The path to get the last segment.
     * @return {@code Either.Left} containing the error otherwise {@code Either.Right} with the last segment name.
     */
    private static Either<String, String> getLastSegmentInPath(Path path) {
        if (path.getNameCount() <= 0) {
            return Either.left(path.toString() + " is not allowed as it is a root path");
        }
        return Either.right(path.getName(path.getNameCount() - 1).toString());
    }

    /**
     * Given project is the project root.
     *
     * <pre>
     *     C:\\Users\\me\\Desktop\\project\\special\\superSpecial\\ultraSpecial
     *                        trailing -> \\special\\superSpecial\\ultraSpecial
     *
     * A list of 4 paths is returned.
     *
     *     // edge cases - include the currentWorkingRootPath.
     *     C:\\Users\\me\\Desktop\\project
     *
     *     // The all subdirectories to final file.
     *     C:\\Users\\me\\Desktop\\project\\special
     *     C:\\Users\\me\\Desktop\\project\\special\\superSpecial
     *     C:\\Users\\me\\Desktop\\project\\special\\superSpecial\\ultraSpecial
     * </pre>
     *
     * <p>The single edge case must include currentWorkingRootPath otherwise the initial file scan won't enter the
     * initial root directory. When a file is modified, all the nested paths going to the project root must be marked as active
     * otherwise the file walk can exit too early.</p>
     *
     * <p>Consider this example.</p>
     *
     * <pre>
     *     1. C:\\Users\\me\\Desktop\\project\\special\\superSpecial\\ultraSpecial - active
     *
     *     2. Start backup analysis at the currentWorkingRootPath.
     *     C:\\Users\\me\\Desktop\\project
     *
     *     3. Now this path 'C:\\Users\\me\\Desktop\\project' was never added to the database watched file list so it
     *     is skipped... but now nothing gets scanned!. This is why all paths heading back up to the root must
     *     be added which this method does.
     * </pre>
     *
     * <p><b>Important</b></p>
     * <p>The supplied {@code Path} MUST be run through the {@code PathMappingStrategy} if you do not want to
     * include the filename itself for non directory paths.</p>
     *
     * @param path The {@code Path} to get all paths for.
     * @return The list of {@code Path}s.
     */
    public java.util.List<Path> getAllPaths(Path path) {
        Tuple2<String, String> pathComponents = getPathComponents(path);

        if (pathComponents._2.isEmpty()) {
            // no trailing segment implying no sub directories.
            return Arrays.asList(currentWorkingRootPath);
        }

        List<String> trailingPathElements = getTrailingPathElements(Paths.get(pathComponents._2));

        String leadingPath = pathComponents._1 + rootDirectoryName;
        java.util.List<Path> result = IntStream.rangeClosed(1, trailingPathElements.length())
                .mapToObj(trailingPathElements::take)
                .map(list -> list.prepend(leadingPath))
                .map(list -> list.collect(Collectors.joining(File.separator)))
                .map(Paths::get)
                .collect(Collectors.toList());

        // Include the project root edge case.
        result.add(currentWorkingRootPath);
        return result;
    }

    /**
     * Returns a vavr list to get functional behavior for easy manipulation.
     *
     * <p>If trailing path is '\\special\\superSpecial\\ultraSpecial' then a list of [special, superSpecial, ultraSpecial]
     * will be returned.</p>
     *
     * @param trailingPath The trailing {@code Path}.
     * @return vavr List containing the individual path names making up the supplied {@code trailingPath}.
     */
    private List<String> getTrailingPathElements(Path trailingPath) {
        List<String> trailingSubNamesList = List.of();
        for (int i = 0; i < trailingPath.getNameCount(); i++) {
            trailingSubNamesList = trailingSubNamesList.append(trailingPath.getName(i).toString());
        }
        return trailingSubNamesList;
    }
}
