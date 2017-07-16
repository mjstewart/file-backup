package fileBackup.monitoring.pathMapping;

import fileBackup.fileAnalysis.FilePathInfo;
import fileBackup.fileAnalysis.FileValidator;
import fileBackup.monitoring.persistence.WatchedFile;
import io.vavr.control.Either;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;

/**
 * Created by matt on 15-Jul-17.
 */
@RunWith(MockitoJUnitRunner.class)
public class CurrentToBackupPathMappingTest {
    @Mock
    private FileValidator validator;

    @Test
    public void getMappedFiles_Windows_ManySubPaths_IsValid() {
        assumeTrue(File.separator.equals("\\"));

        String rootDirectory = "project";

        Path currentWorkingRootPath = Paths.get("C:\\Users\\me\\Desktop\\" + rootDirectory);
        Path backupRootPath = Paths.get("F:\\Folder\\" + rootDirectory);


        Mockito.when(validator.fileExists(currentWorkingRootPath)).thenReturn(true);
        Mockito.when(validator.fileExists(backupRootPath)).thenReturn(true);

        FilePathInfo filePathInfo = FilePathInfo.of(currentWorkingRootPath, backupRootPath, validator).get();

        Path fullCurrentPath = Paths.get("C:\\Users\\me\\Desktop\\" + rootDirectory + "\\stuff\\cc\\TestDir\\Orders\\Freight\\Shipping\\costs.txt");

        /*
         * fullCurrentPath is transformed into all possible paths leading up to the rootDirectory as per filePathInfo.getAllPaths(fullCurrentPath).
         * CurrentToBackupPathMapping creates the mapping from the c:\ path to the backup drive f:\ path to create the reverse mirrored path.
         * Make sure the result contains all of the expected paths.
         */
        List<Path> allMirroredPathMappings = filePathInfo.getAllPaths(fullCurrentPath).stream()
                .map(path -> new CurrentToBackupPathMapping(path, filePathInfo))
                .flatMap(mapping -> mapping.getMappedFiles().stream().map(watchedFile -> Paths.get(watchedFile.getPath())))
                .collect(Collectors.toList());

        List<Path> expected = Arrays.asList(
                Paths.get("C:\\Users\\me\\Desktop\\" + rootDirectory + "\\stuff\\cc\\TestDir\\Orders\\Freight\\Shipping\\costs.txt"),
                Paths.get("C:\\Users\\me\\Desktop\\" + rootDirectory + "\\stuff\\cc\\TestDir\\Orders\\Freight\\Shipping"),
                Paths.get("C:\\Users\\me\\Desktop\\" + rootDirectory + "\\stuff\\cc\\TestDir\\Orders\\Freight"),
                Paths.get("C:\\Users\\me\\Desktop\\" + rootDirectory + "\\stuff\\cc\\TestDir\\Orders"),
                Paths.get("C:\\Users\\me\\Desktop\\" + rootDirectory + "\\stuff\\cc\\TestDir"),
                Paths.get("C:\\Users\\me\\Desktop\\" + rootDirectory + "\\stuff\\cc"),
                Paths.get("C:\\Users\\me\\Desktop\\" + rootDirectory + "\\stuff"),
                Paths.get("C:\\Users\\me\\Desktop\\" + rootDirectory),
                Paths.get("F:\\Folder\\" + rootDirectory + "\\stuff\\cc\\TestDir\\Orders\\Freight\\Shipping\\costs.txt"),
                Paths.get("F:\\Folder\\" + rootDirectory + "\\stuff\\cc\\TestDir\\Orders\\Freight\\Shipping"),
                Paths.get("F:\\Folder\\" + rootDirectory + "\\stuff\\cc\\TestDir\\Orders\\Freight"),
                Paths.get("F:\\Folder\\" + rootDirectory + "\\stuff\\cc\\TestDir\\Orders"),
                Paths.get("F:\\Folder\\" + rootDirectory + "\\stuff\\cc\\TestDir"),
                Paths.get("F:\\Folder\\" + rootDirectory + "\\stuff\\cc"),
                Paths.get("F:\\Folder\\" + rootDirectory + "\\stuff"),
                Paths.get("F:\\Folder\\" + rootDirectory)
        );

        assertThat(allMirroredPathMappings, hasSize(16));
        assertThat(allMirroredPathMappings, containsInAnyOrder(expected.toArray()));
    }

    @Test
    public void getMappedFiles_Linux_ManySubPaths_IsValid() {
        assumeTrue(File.separator.equals("/"));

        String rootDirectory = "project";

        Path currentWorkingRootPath = Paths.get("/home/me/stuff/" + rootDirectory);
        Path backupRootPath = Paths.get("/media/me/USB DISK/" + rootDirectory);


        Mockito.when(validator.fileExists(currentWorkingRootPath)).thenReturn(true);
        Mockito.when(validator.fileExists(backupRootPath)).thenReturn(true);

        FilePathInfo filePathInfo = FilePathInfo.of(currentWorkingRootPath, backupRootPath, validator).get();

        Path fullCurrentPath = Paths.get("/home/me/stuff/" + rootDirectory + "/stuff/cc/TestDir/Orders/Freight/Shipping/costs.txt");

        /*
         * fullCurrentPath is transformed into all possible paths leading up to the rootDirectory as per filePathInfo.getAllPaths(fullCurrentPath).
         * CurrentToBackupPathMapping creates the mapping from the /home path to the backup drive /media path to create the reverse mirrored path.
         * Make sure the result contains all of the expected paths.
         */
        List<Path> allMirroredPathMappings = filePathInfo.getAllPaths(fullCurrentPath).stream()
                .map(path -> new CurrentToBackupPathMapping(path, filePathInfo))
                .flatMap(mapping -> mapping.getMappedFiles().stream().map(watchedFile -> Paths.get(watchedFile.getPath())))
                .collect(Collectors.toList());

        List<Path> expected = Arrays.asList(
                Paths.get("/home/me/stuff/" + rootDirectory + "/stuff/cc/TestDir/Orders/Freight/Shipping/costs.txt"),
                Paths.get("/home/me/stuff/" + rootDirectory + "/stuff/cc/TestDir/Orders/Freight/Shipping"),
                Paths.get("/home/me/stuff/" + rootDirectory + "/stuff/cc/TestDir/Orders/Freight"),
                Paths.get("/home/me/stuff/" + rootDirectory + "/stuff/cc/TestDir/Orders"),
                Paths.get("/home/me/stuff/" + rootDirectory + "/stuff/cc/TestDir"),
                Paths.get("/home/me/stuff/" + rootDirectory + "/stuff/cc"),
                Paths.get("/home/me/stuff/" + rootDirectory + "/stuff"),
                Paths.get("/home/me/stuff/" + rootDirectory),
                Paths.get("/media/me/USB DISK/" + rootDirectory + "/stuff/cc/TestDir/Orders/Freight/Shipping/costs.txt"),
                Paths.get("/media/me/USB DISK/" + rootDirectory + "/stuff/cc/TestDir/Orders/Freight/Shipping"),
                Paths.get("/media/me/USB DISK/" + rootDirectory + "/stuff/cc/TestDir/Orders/Freight"),
                Paths.get("/media/me/USB DISK/" + rootDirectory + "/stuff/cc/TestDir/Orders"),
                Paths.get("/media/me/USB DISK/" + rootDirectory + "/stuff/cc/TestDir"),
                Paths.get("/media/me/USB DISK/" + rootDirectory + "/stuff/cc"),
                Paths.get("/media/me/USB DISK/" + rootDirectory + "/stuff"),
                Paths.get("/media/me/USB DISK/" + rootDirectory)
        );

        assertThat(allMirroredPathMappings, hasSize(16));
        assertThat(allMirroredPathMappings, containsInAnyOrder(expected.toArray()));
    }


    @Test
    public void getMappedFiles_Windows_SingleSubPath_IsValid() {
        assumeTrue(File.separator.equals("\\"));

        String rootDirectory = "project";

        Path currentWorkingRootPath = Paths.get("C:\\Users\\me\\Desktop\\" + rootDirectory);
        Path backupRootPath = Paths.get("F:\\Folder\\" + rootDirectory);


        Mockito.when(validator.fileExists(currentWorkingRootPath)).thenReturn(true);
        Mockito.when(validator.fileExists(backupRootPath)).thenReturn(true);

        FilePathInfo filePathInfo = FilePathInfo.of(currentWorkingRootPath, backupRootPath, validator).get();

        Path fullCurrentPath = Paths.get("C:\\Users\\me\\Desktop\\" + rootDirectory + "\\stuff");

        /*
         * fullCurrentPath is transformed into all possible paths leading up to the rootDirectory as per filePathInfo.getAllPaths(fullCurrentPath).
         * CurrentToBackupPathMapping creates the mapping from the c:\ path to the backup drive f:\ path to create the reverse mirrored path.
         * Make sure the result contains all of the expected paths.
         */
        List<Path> allMirroredPathMappings = filePathInfo.getAllPaths(fullCurrentPath).stream()
                .map(path -> new CurrentToBackupPathMapping(path, filePathInfo))
                .flatMap(mapping -> mapping.getMappedFiles().stream().map(watchedFile -> Paths.get(watchedFile.getPath())))
                .collect(Collectors.toList());

        List<Path> expected = Arrays.asList(
                Paths.get("C:\\Users\\me\\Desktop\\" + rootDirectory + "\\stuff"),
                Paths.get("C:\\Users\\me\\Desktop\\" + rootDirectory),
                Paths.get("F:\\Folder\\" + rootDirectory + "\\stuff"),
                Paths.get("F:\\Folder\\" + rootDirectory)
        );

        assertThat(allMirroredPathMappings, hasSize(4));
        assertThat(allMirroredPathMappings, containsInAnyOrder(expected.toArray()));
    }

    @Test
    public void getMappedFiles_Linux_SingleSubPath_IsValid() {
        assumeTrue(File.separator.equals("/"));

        String rootDirectory = "project";

        Path currentWorkingRootPath = Paths.get("/home/me/stuff/" + rootDirectory);
        Path backupRootPath = Paths.get("/media/me/USB DISK/" + rootDirectory);

        Mockito.when(validator.fileExists(currentWorkingRootPath)).thenReturn(true);
        Mockito.when(validator.fileExists(backupRootPath)).thenReturn(true);

        FilePathInfo filePathInfo = FilePathInfo.of(currentWorkingRootPath, backupRootPath, validator).get();

        Path fullCurrentPath = Paths.get("/home/me/stuff/" + rootDirectory + "/stuff");

        /*
         * fullCurrentPath is transformed into all possible paths leading up to the rootDirectory as per filePathInfo.getAllPaths(fullCurrentPath).
         * CurrentToBackupPathMapping creates the mapping from the /home path to the backup drive /media path to create the reverse mirrored path.
         * Make sure the result contains all of the expected paths.
         */
        List<Path> allMirroredPathMappings = filePathInfo.getAllPaths(fullCurrentPath).stream()
                .map(path -> new CurrentToBackupPathMapping(path, filePathInfo))
                .flatMap(mapping -> mapping.getMappedFiles().stream().map(watchedFile -> Paths.get(watchedFile.getPath())))
                .collect(Collectors.toList());

        List<Path> expected = Arrays.asList(
                Paths.get("/home/me/stuff/" + rootDirectory + "/stuff"),
                Paths.get("/home/me/stuff/" + rootDirectory),
                Paths.get("/media/me/USB DISK/" + rootDirectory + "/stuff"),
                Paths.get("/media/me/USB DISK/" + rootDirectory)
        );

        assertThat(allMirroredPathMappings, hasSize(4));
        assertThat(allMirroredPathMappings, containsInAnyOrder(expected.toArray()));
    }

    @Test
    public void getMappedFiles_Windows_RootPathOnly_IsValid() {
        assumeTrue(File.separator.equals("\\"));

        String rootDirectory = "project";

        Path currentWorkingRootPath = Paths.get("C:\\Users\\me\\Desktop\\" + rootDirectory);
        Path backupRootPath = Paths.get("F:\\Folder\\" + rootDirectory);


        Mockito.when(validator.fileExists(currentWorkingRootPath)).thenReturn(true);
        Mockito.when(validator.fileExists(backupRootPath)).thenReturn(true);

        FilePathInfo filePathInfo = FilePathInfo.of(currentWorkingRootPath, backupRootPath, validator).get();

        Path fullCurrentPath = Paths.get("C:\\Users\\me\\Desktop\\" + rootDirectory);

        /*
         * fullCurrentPath is transformed into all possible paths leading up to the rootDirectory as per filePathInfo.getAllPaths(fullCurrentPath).
         * CurrentToBackupPathMapping creates the mapping from the c:\ path to the backup drive f:\ path to create the reverse mirrored path.
         * Make sure the result contains all of the expected paths.
         */
        List<Path> allMirroredPathMappings = filePathInfo.getAllPaths(fullCurrentPath).stream()
                .map(path -> new CurrentToBackupPathMapping(path, filePathInfo))
                .flatMap(mapping -> mapping.getMappedFiles().stream().map(watchedFile -> Paths.get(watchedFile.getPath())))
                .collect(Collectors.toList());

        List<Path> expected = Arrays.asList(
                Paths.get("C:\\Users\\me\\Desktop\\" + rootDirectory),
                Paths.get("F:\\Folder\\" + rootDirectory)
        );

        assertThat(allMirroredPathMappings, hasSize(2));
        assertThat(allMirroredPathMappings, containsInAnyOrder(expected.toArray()));
    }

    @Test
    public void getMappedFiles_Linux_RootPathOnly_IsValid() {
        assumeTrue(File.separator.equals("/"));

        String rootDirectory = "project";

        Path currentWorkingRootPath = Paths.get("/home/me/stuff/" + rootDirectory);
        Path backupRootPath = Paths.get("/media/me/USB DISK/" + rootDirectory);

        Mockito.when(validator.fileExists(currentWorkingRootPath)).thenReturn(true);
        Mockito.when(validator.fileExists(backupRootPath)).thenReturn(true);

        FilePathInfo filePathInfo = FilePathInfo.of(currentWorkingRootPath, backupRootPath, validator).get();

        Path fullCurrentPath = Paths.get("/home/me/stuff/" + rootDirectory);

        /*
         * fullCurrentPath is transformed into all possible paths leading up to the rootDirectory as per filePathInfo.getAllPaths(fullCurrentPath).
         * CurrentToBackupPathMapping creates the mapping from the /home path to the backup drive /media path to create the reverse mirrored path.
         * Make sure the result contains all of the expected paths.
         */
        List<Path> allMirroredPathMappings = filePathInfo.getAllPaths(fullCurrentPath).stream()
                .map(path -> new CurrentToBackupPathMapping(path, filePathInfo))
                .flatMap(mapping -> mapping.getMappedFiles().stream().map(watchedFile -> Paths.get(watchedFile.getPath())))
                .collect(Collectors.toList());

        List<Path> expected = Arrays.asList(
                Paths.get("/home/me/stuff/" + rootDirectory),
                Paths.get("/media/me/USB DISK/" + rootDirectory)
        );

        assertThat(allMirroredPathMappings, hasSize(2));
        assertThat(allMirroredPathMappings, containsInAnyOrder(expected.toArray()));
    }
}