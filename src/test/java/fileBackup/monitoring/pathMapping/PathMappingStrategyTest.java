package fileBackup.monitoring.pathMapping;

import fileBackup.monitoring.pathMapping.PathMappingStrategy;
import org.junit.Test;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Predicate;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assume.assumeTrue;

/**
 * Created by matt on 13-Jul-17.
 */
public class PathMappingStrategyTest {

    private Predicate<Path> isDirectory = p -> true;
    private Predicate<Path> isNotDirectory = p -> false;

    @Test
    public void map_Windows_doesNotMapDirectoryToParentPath_IsValid() {
        assumeTrue(File.separator.equals("\\"));
        PathMappingStrategy pathMappingStrategy = new PathMappingStrategy(isDirectory);
        // If the path is to a directory, the path must not be mapped to its parent path.
        Path path = Paths.get("C:\\Users\\me\\Desktop\\rootDirectory\\important\\stuff");
        assertThat(pathMappingStrategy.map(path), is(path));
    }

    @Test
    public void map_Windows_fileIsMappedToParentPath_IsValid() {
        /*
         * If the path is to a file, map it to its parent path which means the 'parent' path has seen file activity
         * and can be added to the database to be scanned during backup.
         */
        assumeTrue(File.separator.equals("\\"));
        PathMappingStrategy pathMappingStrategy = new PathMappingStrategy(isNotDirectory);
        Path path = Paths.get("C:\\Users\\me\\Desktop\\rootDirectory\\important\\stuff\\file.txt");
        // Simulate the mapping of getting the parent given its a file.
        Path parentPath = path.getParent();
        assertThat(pathMappingStrategy.map(path), is(parentPath));
    }

    @Test
    public void getMappedHashCode_Windows_DirectoryMapped_IsValid() {
        /*
         * Test that getMappedHashCode applies mapping and uses toString for the hashCode correctly when its a directory.
         * This prevents using Path.hashCode which is different from Path.toString().hashCode().
         */
        assumeTrue(File.separator.equals("\\"));
        PathMappingStrategy pathMappingStrategy = new PathMappingStrategy(isDirectory);
        Path path = Paths.get("C:\\Users\\me\\Desktop\\rootDirectory\\important\\stuff");
        assertThat(pathMappingStrategy.getMappedHashCode(path), is(path.toString().hashCode()));
    }

    @Test
    public void getMappedHashCode_Windows_FileMapped_IsValid() {
        /*
         * Test that getMappedHashCode applies mapping and uses toString for the hashCode correctly when its a file.
         * This prevents using Path.hashCode which is different from Path.toString().hashCode().
         */
        assumeTrue(File.separator.equals("\\"));
        PathMappingStrategy pathMappingStrategy = new PathMappingStrategy(isNotDirectory);
        Path path = Paths.get("C:\\Users\\me\\Desktop\\rootDirectory\\important\\stuff\\file.txt");
        // Simulate the mapping of getting the parent given its a file.
        Path parentPath = path.getParent();
        assertThat(pathMappingStrategy.getMappedHashCode(path), is(parentPath.toString().hashCode()));
    }

    @Test
    public void getMappedPathString_Windows_Directory_IsValid() {
        assumeTrue(File.separator.equals("\\"));
        PathMappingStrategy pathMappingStrategy = new PathMappingStrategy(isDirectory);
        Path path = Paths.get("C:\\Users\\me\\Desktop\\rootDirectory\\important\\stuff");
        assertThat(pathMappingStrategy.getMappedPathString(path), is(path.toString()));
    }

    @Test
    public void getMappedPathString_Windows_File_IsValid() {
        assumeTrue(File.separator.equals("\\"));
        PathMappingStrategy pathMappingStrategy = new PathMappingStrategy(isNotDirectory);
        Path path = Paths.get("C:\\Users\\me\\Desktop\\rootDirectory\\important\\stuff\\file.txt");
        // Simulate the mapping of getting the parent given its a file.
        Path parentPath = path.getParent();
        assertThat(pathMappingStrategy.getMappedPathString(path), is(parentPath.toString()));
    }


    @Test
    public void getUnmappedHashCode_Windows_DirectoryMapped_IsValid() {
        assumeTrue(File.separator.equals("\\"));
        // Tests that no mapping is applied, the opposite of its mapped version.
        PathMappingStrategy pathMappingStrategy = new PathMappingStrategy(isDirectory);
        Path path = Paths.get("C:\\Users\\me\\Desktop\\rootDirectory\\important\\stuff");
        assertThat(pathMappingStrategy.getUnmappedHashCode(path), is(path.toString().hashCode()));
    }

    @Test
    public void getUnmappedHashCode_Windows_FileMapped_IsValid() {
        assumeTrue(File.separator.equals("\\"));
        // Tests that no mapping is applied, the opposite of its mapped version.
        PathMappingStrategy pathMappingStrategy = new PathMappingStrategy(isNotDirectory);
        Path path = Paths.get("C:\\Users\\me\\Desktop\\rootDirectory\\important\\stuff\\file.txt");
        assertThat(pathMappingStrategy.getUnmappedHashCode(path), is(path.toString().hashCode()));
    }

    @Test
    public void getUnmappedPathString_Windows_Directory_IsValid() {
        assumeTrue(File.separator.equals("\\"));
        // Tests that no mapping is applied, the opposite of its mapped version.
        PathMappingStrategy pathMappingStrategy = new PathMappingStrategy(isDirectory);
        Path path = Paths.get("C:\\Users\\me\\Desktop\\rootDirectory\\important\\stuff");
        assertThat(pathMappingStrategy.getUnmappedPathString(path), is(path.toString()));
    }

    @Test
    public void getUnmappedPathString_Windows_File_IsValid() {
        assumeTrue(File.separator.equals("\\"));
        // Tests that no mapping is applied, the opposite of its mapped version.
        PathMappingStrategy pathMappingStrategy = new PathMappingStrategy(isNotDirectory);
        Path path = Paths.get("C:\\Users\\me\\Desktop\\rootDirectory\\important\\file.txt");
        assertThat(pathMappingStrategy.getUnmappedPathString(path), is(path.toString()));
    }

    // linux

    @Test
    public void map_Linux_doesNotMapDirectoryToParentPath_IsValid() {
        assumeTrue(File.separator.equals("/"));
        PathMappingStrategy pathMappingStrategy = new PathMappingStrategy(isDirectory);
        // If the path is to a directory, the path must not be mapped to its parent path.
        Path path = Paths.get("/home/me/Desktop/rootDirectory/important/stuff");
        assertThat(pathMappingStrategy.map(path), is(path));
    }

    @Test
    public void map_Linux_fileIsMappedToParentPath_IsValid() {
        /*
         * If the path is to a file, map it to its parent path which means the 'parent' path has seen file activity
         * and can be added to the database to be scanned during backup.
         */
        assumeTrue(File.separator.equals("/"));
        PathMappingStrategy pathMappingStrategy = new PathMappingStrategy(isNotDirectory);
        Path path = Paths.get("/home/me/Desktop/rootDirectory/important/stuff/file.txt");
        // Simulate the mapping of getting the parent given its a file.
        Path parentPath = path.getParent();
        assertThat(pathMappingStrategy.map(path), is(parentPath));
    }

    @Test
    public void getMappedHashCode_Linux_DirectoryMapped_IsValid() {
        /*
         * Test that getMappedHashCode applies mapping and uses toString for the hashCode correctly when its a directory.
         * This prevents using Path.hashCode which is different from Path.toString().hashCode().
         */
        assumeTrue(File.separator.equals("/"));
        PathMappingStrategy pathMappingStrategy = new PathMappingStrategy(isDirectory);
        Path path = Paths.get("/home/me/Desktop/rootDirectory/important/stuff");
        assertThat(pathMappingStrategy.getMappedHashCode(path), is(path.toString().hashCode()));
    }

    @Test
    public void getMappedHashCode_Linux_FileMapped_IsValid() {
        /*
         * Test that getMappedHashCode applies mapping and uses toString for the hashCode correctly when its a file.
         * This prevents using Path.hashCode which is different from Path.toString().hashCode().
         */
        assumeTrue(File.separator.equals("/"));
        PathMappingStrategy pathMappingStrategy = new PathMappingStrategy(isNotDirectory);
        Path path = Paths.get("/home/me/Desktop/rootDirectory/important/stuff/file.txt");
        // Simulate the mapping of getting the parent given its a file.
        Path parentPath = path.getParent();
        assertThat(pathMappingStrategy.getMappedHashCode(path), is(parentPath.toString().hashCode()));
    }

    @Test
    public void getMappedPathString_Linux_Directory_IsValid() {
        assumeTrue(File.separator.equals("/"));
        PathMappingStrategy pathMappingStrategy = new PathMappingStrategy(isDirectory);
        Path path = Paths.get("/home/me/Desktop/rootDirectory/important/stuff");
        assertThat(pathMappingStrategy.getMappedPathString(path), is(path.toString()));
    }

    @Test
    public void getMappedPathString_Linux_File_IsValid() {
        assumeTrue(File.separator.equals("/"));
        PathMappingStrategy pathMappingStrategy = new PathMappingStrategy(isNotDirectory);
        Path path = Paths.get("/home/me/Desktop/rootDirectory/important/stuff/file.txt");
        // Simulate the mapping of getting the parent given its a file.
        Path parentPath = path.getParent();
        assertThat(pathMappingStrategy.getMappedPathString(path), is(parentPath.toString()));
    }


    @Test
    public void getUnmappedHashCode_Linux_DirectoryMapped_IsValid() {
        assumeTrue(File.separator.equals("/"));
        // Tests that no mapping is applied, the opposite of its mapped version.
        PathMappingStrategy pathMappingStrategy = new PathMappingStrategy(isDirectory);
        Path path = Paths.get("/home/me/Desktop/rootDirectory/important/stuff");
        assertThat(pathMappingStrategy.getUnmappedHashCode(path), is(path.toString().hashCode()));
    }

    @Test
    public void getUnmappedHashCode_Linux_FileMapped_IsValid() {
        assumeTrue(File.separator.equals("/"));
        // Tests that no mapping is applied, the opposite of its mapped version.
        PathMappingStrategy pathMappingStrategy = new PathMappingStrategy(isNotDirectory);
        Path path = Paths.get("/home/me/Desktop/rootDirectory/important/stuff/file.txt");
        assertThat(pathMappingStrategy.getUnmappedHashCode(path), is(path.toString().hashCode()));
    }

    @Test
    public void getUnmappedPathString_Linux_Directory_IsValid() {
        assumeTrue(File.separator.equals("/"));
        // Tests that no mapping is applied, the opposite of its mapped version.
        PathMappingStrategy pathMappingStrategy = new PathMappingStrategy(isDirectory);
        Path path = Paths.get("/home/me/Desktop/rootDirectory/important/stuff");
        assertThat(pathMappingStrategy.getUnmappedPathString(path), is(path.toString()));
    }

    @Test
    public void getUnmappedPathString_Linux_File_IsValid() {
        assumeTrue(File.separator.equals("/"));
        // Tests that no mapping is applied, the opposite of its mapped version.
        PathMappingStrategy pathMappingStrategy = new PathMappingStrategy(isNotDirectory);
        Path path = Paths.get("/home/me/Desktop/rootDirectory/important/stuff/file.txt");
        assertThat(pathMappingStrategy.getUnmappedPathString(path), is(path.toString()));
    }
}