package fileBackup.fileAnalysis;

import io.vavr.Tuple2;
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

import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assume.assumeFalse;
import static org.junit.Assume.assumeThat;
import static org.junit.Assume.assumeTrue;

/**
 * Since {@code File.separator} and {@code Path} are used together, both windows and unix are testable and supported.
 *
 * Created by matt on 03-Jul-17.
 */
@RunWith(MockitoJUnitRunner.class)
public class FilePathInfoTest {

    @Mock
    private FileValidator validator;

    @Test
    public void constructPath_CurrentPathFile_FileDoesNotExist() {
        // currentWorkingRootPath underlying File does not exist. Platform independent
        String rootDirectory = "project";

        Path currentWorkingRootPath = Paths.get("C:\\Users\\me\\Desktop\\" + rootDirectory);
        Path backupRootPath = Paths.get("D:\\Stuff\\More\\" + rootDirectory);

        Mockito.when(validator.fileExists(currentWorkingRootPath)).thenReturn(false);
        Mockito.when(validator.fileExists(backupRootPath)).thenReturn(true);

        Either<String, FilePathInfo> filePathInfo = FilePathInfo.of(currentWorkingRootPath, backupRootPath, false, validator);
        assertThat(filePathInfo.isLeft(), is(true));
    }

    @Test
    public void constructPath_BackupPathFile_FileDoesNotExist() {
        // backupRootPath underlying File does not exist. Platform independent
        String rootDirectory = "project";

        Path currentWorkingRootPath = Paths.get("C:\\Users\\me\\Desktop\\" + rootDirectory);
        Path backupRootPath = Paths.get("D:\\Stuff\\More\\" + rootDirectory);

        Mockito.when(validator.fileExists(currentWorkingRootPath)).thenReturn(true);
        Mockito.when(validator.fileExists(backupRootPath)).thenReturn(false);

        Either<String, FilePathInfo> filePathInfo = FilePathInfo.of(currentWorkingRootPath, backupRootPath, false, validator);
        assertThat(filePathInfo.isLeft(), is(true));
    }

    @Test
    public void constructPath_CurrentAndBackupPathFile_FilesDoNotExist() {
        // currentWorkingRootPath and backupRootPath underlying File does not exist. Platform independent
        String rootDirectory = "project";

        Path currentWorkingRootPath = Paths.get("C:\\Users\\me\\Desktop\\" + rootDirectory);
        Path backupRootPath = Paths.get("D:\\Stuff\\More\\" + rootDirectory);

        Mockito.when(validator.fileExists(currentWorkingRootPath)).thenReturn(false);
        Mockito.when(validator.fileExists(backupRootPath)).thenReturn(false);

        Either<String, FilePathInfo> filePathInfo = FilePathInfo.of(currentWorkingRootPath, backupRootPath, false, validator);
        assertThat(filePathInfo.isLeft(), is(true));
    }

    @Test
    public void constructPath_Windows_IdenticalPaths_Invalid() {
        // Don't attempt backup if the paths are the same. Paths must only have the same ending root directory.
        assumeTrue(File.separator.equals("\\"));
        String rootDirectory = "project";

        Path currentWorkingRootPath = Paths.get("C:\\Users\\me\\Desktop\\" + rootDirectory);
        Path backupRootPath =  Paths.get("C:\\Users\\me\\Desktop\\" + rootDirectory);

        Mockito.when(validator.fileExists(currentWorkingRootPath)).thenReturn(true);
        Mockito.when(validator.fileExists(backupRootPath)).thenReturn(true);

        Either<String, FilePathInfo> filePathInfo = FilePathInfo.of(currentWorkingRootPath, backupRootPath, false, validator);
        assertThat(filePathInfo.isLeft(), is(true));
    }

    @Test
    public void constructPath_Linux_IdenticalPaths_Invalid() {
        // Don't attempt backup if the paths are the same. Paths must only have the same ending root directory.
        assumeTrue(File.separator.equals("/"));
        String rootDirectory = "project";

        Path currentWorkingRootPath = Paths.get("/home/me/work/" + rootDirectory);
        Path backupRootPath =  Paths.get("/home/me/work/" + rootDirectory);

        Mockito.when(validator.fileExists(currentWorkingRootPath)).thenReturn(true);
        Mockito.when(validator.fileExists(backupRootPath)).thenReturn(true);

        Either<String, FilePathInfo> filePathInfo = FilePathInfo.of(currentWorkingRootPath, backupRootPath, false, validator);
        assertThat(filePathInfo.isLeft(), is(true));
    }

    @Test
    public void constructPath_Windows_IdenticalRootPaths_Invalid() {
        assumeTrue(File.separator.equals("\\"));
        // Don't attempt backup if the paths are the same. Cannot allow root drives with no project root

        Path currentWorkingRootPath = Paths.get("C:\\");
        Path backupRootPath =  Paths.get("F:\\");

        Mockito.when(validator.fileExists(currentWorkingRootPath)).thenReturn(true);
        Mockito.when(validator.fileExists(backupRootPath)).thenReturn(true);

        Either<String, FilePathInfo> filePathInfo = FilePathInfo.of(currentWorkingRootPath, backupRootPath, false, validator);
        assertThat(filePathInfo.isLeft(), is(true));
    }

    @Test
    public void constructPath_Linux_IdenticalRootPaths_Invalid() {
        assumeTrue(File.separator.equals("/"));
        // Don't attempt backup if the paths are the same. Cannot allow root drives with no project root

        Path currentWorkingRootPath = Paths.get("/");
        Path backupRootPath =  Paths.get("/");

        Mockito.when(validator.fileExists(currentWorkingRootPath)).thenReturn(true);
        Mockito.when(validator.fileExists(backupRootPath)).thenReturn(true);

        Either<String, FilePathInfo> filePathInfo = FilePathInfo.of(currentWorkingRootPath, backupRootPath, false, validator);
        assertThat(filePathInfo.isLeft(), is(true));
    }

    @Test
    public void constructPath_Windows_CommonRootDirectoryWithSegmentsAfterRoot_IsValid() {
        /*
         * Last segment on both current and backup must be the same root directory - valid case
         *
         * The definition of 'Segments after root' is that there are more directories after C:\ and F:\
         * This application enforces a rule where a backup can only be made FROM at least 1 sub directory in from
         * a root directory. So a path like C:\project is fine but C:\ is not.
         *
         * Example: C:\Users\me\Desktop\project and F:\project are fine as they have the same last root directory
         * of 'project'. All the segments before project don't matter.
         */
        assumeTrue(File.separator.equals("\\"));
        String rootDirectory = "project";

        Path currentWorkingRootPath = Paths.get("C:\\Users\\me\\Desktop\\" + rootDirectory);
        Path backupRootPath = Paths.get("F:\\Folder\\" + rootDirectory);

        Mockito.when(validator.fileExists(currentWorkingRootPath)).thenReturn(true);
        Mockito.when(validator.fileExists(backupRootPath)).thenReturn(true);

        Either<String, FilePathInfo> filePathInfo = FilePathInfo.of(currentWorkingRootPath, backupRootPath, false, validator);

        assertThat(filePathInfo.isRight(), is(true));
        assertThat(filePathInfo.get().getRootDirectoryName(), is(rootDirectory));
    }

    @Test
    public void constructPath_Linux_CommonRootDirectoryWithSegmentsAfterRoot_IsValid() {
        /*
         * Last segment on both current and backup must be the same root directory - valid case
         *
         * The definition of 'Segments after root' is that there are more directories after C:\ and F:\
         * This application enforces a rule where a backup can only be made FROM at least 1 sub directory in from
         * a root directory. So a path like C:\project is fine but C:\ is not.
         *
         * Example: C:\Users\me\Desktop\project and F:\project are fine as they have the same last root directory
         * of 'project'. All the segments before project don't matter.
         */
        assumeTrue(File.separator.equals("/"));
        String rootDirectory = "project";

        Path currentWorkingRootPath = Paths.get("/home/me/work/" + rootDirectory);
        Path backupRootPath = Paths.get("/media/me/USB DISK/" + rootDirectory);

        Mockito.when(validator.fileExists(currentWorkingRootPath)).thenReturn(true);
        Mockito.when(validator.fileExists(backupRootPath)).thenReturn(true);

        Either<String, FilePathInfo> filePathInfo = FilePathInfo.of(currentWorkingRootPath, backupRootPath, false, validator);

        assertThat(filePathInfo.isRight(), is(true));
        assertThat(filePathInfo.get().getRootDirectoryName(), is(rootDirectory));
    }

    @Test
    public void constructPath_Windows_CommonRootDirectoryWithSegmentsAfterRoot_Invalid() {
        /*
         * Last segment on both current and backup must be the same root directory - invalid case
         *
         * The definition of 'Segments after root' is that there are more directories after C:\ and F:\
         * This application enforces a rule where a backup can only be made FROM at least 1 sub directory in from
         * a root directory. So a path like C:\project is fine but C:\ is not.
         *
         * Notice how project and project123 are the last root segments but are different which is illegal.
         */
        assumeTrue(File.separator.equals("\\"));
        Path currentWorkingRootPath = Paths.get("C:\\Users\\me\\Desktop\\project");
        Path backupRootPath = Paths.get("F:\\Folder\\project123");

        Mockito.when(validator.fileExists(currentWorkingRootPath)).thenReturn(true);
        Mockito.when(validator.fileExists(backupRootPath)).thenReturn(true);

        Either<String, FilePathInfo> filePathInfo = FilePathInfo.of(currentWorkingRootPath, backupRootPath, false, validator);

        assertThat(filePathInfo.isLeft(), is(true));
    }

    @Test
    public void constructPath_Linux_CommonRootDirectoryWithSegmentsAfterRoot_Invalid() {
        /*
         * Last segment on both current and backup must be the same root directory - invalid case
         *
         * The definition of 'Segments after root' is that there are more directories after the top level / root.
         * This application enforces a rule where a backup can only be made FROM at least 1 sub directory in from
         * a root directory. So a path like /home is fine / is not.
         *
         * Notice how project and project123 are the last root segments but are different which is illegal.
         */
        assumeTrue(File.separator.equals("/"));
        Path currentWorkingRootPath = Paths.get("/home/me/work/project");
        Path backupRootPath = Paths.get("/media/me/USB DISK/project123");

        Mockito.when(validator.fileExists(currentWorkingRootPath)).thenReturn(true);
        Mockito.when(validator.fileExists(backupRootPath)).thenReturn(true);

        Either<String, FilePathInfo> filePathInfo = FilePathInfo.of(currentWorkingRootPath, backupRootPath, false, validator);

        assertThat(filePathInfo.isLeft(), is(true));
    }

    @Test
    public void constructPath_Windows_CommonRootDirectory_BackupDirectoryNoSegmentsAfterRoot_Invalid() {
        /*
         * This is a sanity check more than anything as the FilePathInfo will return Either.Left anyway since both
         * paths don't end in the same project root 'project'. Either way the backup path cannot be a root directory.
         */
        assumeTrue(File.separator.equals("\\"));
        Path currentWorkingRootPath = Paths.get("C:\\Users\\project");
        Path backupRootPath = Paths.get("F:\\");

        Mockito.when(validator.fileExists(currentWorkingRootPath)).thenReturn(true);
        Mockito.when(validator.fileExists(backupRootPath)).thenReturn(true);

        Either<String, FilePathInfo> filePathInfo = FilePathInfo.of(currentWorkingRootPath, backupRootPath, false, validator);
        assertThat(filePathInfo.isLeft(), is(true));
    }

    @Test
    public void constructPath_Linux_CommonRootDirectory_BackupDirectoryNoSegmentsAfterRoot_Invalid() {
        /*
         * This is a sanity check more than anything as the FilePathInfo will return Either.Left anyway since both
         * paths don't end in the same project root 'project'. Either way the backup path cannot be a root directory.
         */
        assumeTrue(File.separator.equals("/"));
        Path currentWorkingRootPath = Paths.get("/home/me/work/project");
        Path backupRootPath = Paths.get("/");

        Mockito.when(validator.fileExists(currentWorkingRootPath)).thenReturn(true);
        Mockito.when(validator.fileExists(backupRootPath)).thenReturn(true);

        Either<String, FilePathInfo> filePathInfo = FilePathInfo.of(currentWorkingRootPath, backupRootPath, false, validator);
        assertThat(filePathInfo.isLeft(), is(true));
    }


    @Test
    public void getPathComponents_Windows_ManySubDirectories_NonEmptyTrailingPathSegment() {
        /*
         * For example. The many subdirectories is all the folders up until 'project'.
         * fullCurrentPath = C:\Users\me\Desktop\project\FolderA\FolderB\document.txt
         *                                               -> trailing segments
         *
         * This test is ensuring that the getPathComponents method works correctly.
         * Path components tuple should contain both path segments.
         * (C:\Users\me\Desktop\, \FolderA\FolderB\document.txt)
         *
         * Notice how the boundary of the split is 'project' in the middle.
         * There must be a trailing/leading separator as it makes application code simpler to reinsert
         * in the project root name.
         */
        assumeTrue(File.separator.equals("\\"));
        String rootDirectory = "project";

        Path currentWorkingRootPath = Paths.get("C:\\Users\\me\\Desktop\\" + rootDirectory);
        Path backupRootPath = Paths.get("F:\\Stuff\\More\\" + rootDirectory);

        String filePath = "\\FolderA\\FolderB\\document.txt";

        Path fullCurrentPath = Paths.get(currentWorkingRootPath.toString() + filePath);

        Mockito.when(validator.fileExists(currentWorkingRootPath)).thenReturn(true);
        Mockito.when(validator.fileExists(backupRootPath)).thenReturn(true);

        Tuple2<String, String> pathComponents = FilePathInfo.of(currentWorkingRootPath, backupRootPath, false, validator)
                .get().getPathComponents(fullCurrentPath);

        assertThat(pathComponents._1, is("C:\\Users\\me\\Desktop\\"));
        assertThat(pathComponents._2, is(filePath));
    }

    public void getPathComponents_Linux_ManySubDirectories_NonEmptyTrailingPathSegment() {
        /*
         * For example. The many subdirectories is all the folders up until 'project'.
         * fullCurrentPath = /home/me/work/project/FolderA/FolderB/document.txt
         *                                         -> trailing segments
         *
         * This test is ensuring that the getPathComponents method works correctly.
         * Path components tuple should contain both path segments.
         * (/home/me/work/, /FolderA/FolderB/document.txt)
         *
         * Notice how the boundary of the split is 'project' in the middle.
         * There must be a trailing/leading separator as it makes application code simpler to reinsert
         * in the project root name.
         */
        assumeTrue(File.separator.equals("/"));
        String rootDirectory = "project";

        Path currentWorkingRootPath = Paths.get("/home/me/work/" + rootDirectory);
        Path backupRootPath = Paths.get("/media/me/USB DISK/" + rootDirectory);

        String filePath = "/FolderA/FolderB/document.txt";

        Path fullCurrentPath = Paths.get(currentWorkingRootPath.toString() + filePath);

        Mockito.when(validator.fileExists(currentWorkingRootPath)).thenReturn(true);
        Mockito.when(validator.fileExists(backupRootPath)).thenReturn(true);

        Tuple2<String, String> pathComponents = FilePathInfo.of(currentWorkingRootPath, backupRootPath, false, validator)
                .get().getPathComponents(fullCurrentPath);

        assertThat(pathComponents._1, is("/home/me/work/"));
        assertThat(pathComponents._2, is(filePath));
    }


    @Test
    public void getPathComponents_Windows_SingleRootDirectory_EmptyTrailingPathSegment() {
        /*
         * For example. The single root directory implies F:\project. Eg project folder is the single root folder.
         * F:\project
         *           -> No trailing segments following project!
         *
         * This test is ensuring that the getPathComponents method works correctly.
         * Path components tuple should contain both path segments. However the second segment should be empty given
         * there are no trailing path segments as illustrated above.
         *
         * (F:\, EMPTY STRING)
         *
         * There must be a trailing/leading separator as it makes application code simpler to reinsert
         * in the project root name.
         */
        assumeTrue(File.separator.equals("\\"));
        String rootDirectory = "project";

        Path currentWorkingRootPath = Paths.get("C:\\Users\\me\\Desktop\\" + rootDirectory);
        Path backupRootPath = Paths.get("F:\\" + rootDirectory);

        Mockito.when(validator.fileExists(currentWorkingRootPath)).thenReturn(true);
        Mockito.when(validator.fileExists(backupRootPath)).thenReturn(true);

        Tuple2<String, String> pathComponents = FilePathInfo.of(currentWorkingRootPath, backupRootPath, false, validator)
                .get().getPathComponents(backupRootPath);

        assertThat(pathComponents._1, is("F:\\"));
        assertThat(pathComponents._2, is(""));
    }

    @Test
    public void getPathComponents_Linux_SingleRootDirectory_EmptyTrailingPathSegment() {
        /*
         * For example. The single root directory implies /media/project. Eg project folder is the single root folder.
         * /media/project
         *           -> No trailing segments following project!
         *
         * This test is ensuring that the getPathComponents method works correctly.
         * Path components tuple should contain both path segments. However the second segment should be empty given
         * there are no trailing path segments as illustrated above.
         *
         * (/media/, EMPTY STRING)
         *
         * There must be a trailing/leading separator as it makes application code simpler to reinsert
         * in the project root name.
         */
        assumeTrue(File.separator.equals("/"));
        String rootDirectory = "project";

        Path currentWorkingRootPath = Paths.get("/home/me/work/" + rootDirectory);
        Path backupRootPath = Paths.get("/media/" + rootDirectory);

        Mockito.when(validator.fileExists(currentWorkingRootPath)).thenReturn(true);
        Mockito.when(validator.fileExists(backupRootPath)).thenReturn(true);

        Tuple2<String, String> pathComponents = FilePathInfo.of(currentWorkingRootPath, backupRootPath, false, validator)
                .get().getPathComponents(backupRootPath);

        assertThat(pathComponents._1, is("/media/"));
        assertThat(pathComponents._2, is(""));
    }

    @Test
    public void getPathComponents_Windows_ManySubDirectories_EmptyTrailingPathSegment() {
        /*
         * For example. The many sub directories means
         * F:\FolderA\FolderB\project
         * many sub directories...... -> No trailing sub directories following project!
         *
         * This test is ensuring that the getPathComponents method works correctly.
         * Path components tuple should contain both path segments. However the second segment should be empty given
         * there are no trailing path segments as illustrated above.
         *
         * (F:\FolderA\FolderB\, EMPTY STRING)
         *
         * There must be a trailing/leading separator as it makes application code simpler to reinsert
         * in the project root name.
         */
        assumeTrue(File.separator.equals("\\"));
        String rootDirectory = "project";

        Path currentWorkingRootPath = Paths.get("C:\\Users\\me\\Desktop\\" + rootDirectory);
        Path backupRootPath = Paths.get("F:\\FolderA\\FolderB\\" + rootDirectory);

        Mockito.when(validator.fileExists(currentWorkingRootPath)).thenReturn(true);
        Mockito.when(validator.fileExists(backupRootPath)).thenReturn(true);

        Tuple2<String, String> pathComponents = FilePathInfo.of(currentWorkingRootPath, backupRootPath, false, validator)
                .get().getPathComponents(backupRootPath);

        assertThat(pathComponents._1, is("F:\\FolderA\\FolderB\\"));
        assertThat(pathComponents._2, is(""));
    }

    @Test
    public void getPathComponents_Linux_ManySubDirectories_EmptyTrailingPathSegment() {
        /*
         * For example. The many sub directories means
         * /media/me/USB DISK/FolderA/FolderB/project
         * many sub directories......                -> No trailing sub directories following project!
         *
         * This test is ensuring that the getPathComponents method works correctly.
         * Path components tuple should contain both path segments. However the second segment should be empty given
         * there are no trailing path segments as illustrated above.
         *
         * (/media/me/USB DISK/FolderA/FolderB/, EMPTY STRING)
         *
         * There must be a trailing/leading separator as it makes application code simpler to reinsert
         * in the project root name.
         */
        assumeTrue(File.separator.equals("/"));
        String rootDirectory = "project";


        Path currentWorkingRootPath = Paths.get("/home/me/work/" + rootDirectory);
        Path backupRootPath = Paths.get("/media/me/USB DISK/FolderA/FolderB/" + rootDirectory);

        Mockito.when(validator.fileExists(currentWorkingRootPath)).thenReturn(true);
        Mockito.when(validator.fileExists(backupRootPath)).thenReturn(true);

        Tuple2<String, String> pathComponents = FilePathInfo.of(currentWorkingRootPath, backupRootPath, false, validator)
                .get().getPathComponents(backupRootPath);

        assertThat(pathComponents._1, is("/media/me/USB DISK/FolderA/FolderB/"));
        assertThat(pathComponents._2, is(""));
    }

    @Test
    public void fromCurrentToBackupPath_Windows_NoDuplicateRootPath_IsValid() {
        // Returns valid path if there is only 1 rootDirectory name in the whole path.
        assumeTrue(File.separator.equals("\\"));
        String rootDirectory = "project";

        Path currentWorkingRootPath = Paths.get("C:\\Users\\me\\Desktop\\" + rootDirectory);
        Path backupRootPath = Paths.get("F:\\FolderC\\" + rootDirectory);

        String filePath = "\\FolderA\\FolderB\\document.txt";

        Path fullCurrentPath = Paths.get(currentWorkingRootPath.toString() + filePath);
        String expectedPathString = backupRootPath.toString() + filePath;

        Mockito.when(validator.fileExists(currentWorkingRootPath)).thenReturn(true);
        Mockito.when(validator.fileExists(backupRootPath)).thenReturn(true);

        Either<String, FilePathInfo> filePathInfo = FilePathInfo.of(currentWorkingRootPath, backupRootPath, false, validator);

        assertThat(filePathInfo.get().fromCurrentToBackupPath(fullCurrentPath).toString(), is(expectedPathString));
    }

    @Test
    public void fromCurrentToBackupPath_Linux_NoDuplicateRootPath_IsValid() {
        // Returns valid path if there is only 1 rootDirectory name in the whole path.
        assumeTrue(File.separator.equals("/"));
        String rootDirectory = "project";

        Path currentWorkingRootPath = Paths.get("/home/me/work/" + rootDirectory);
        Path backupRootPath = Paths.get("/media/me/USB DISK/FolderQ/" + rootDirectory);

        String filePath = "/FolderA/FolderB/document.txt";

        Path fullCurrentPath = Paths.get(currentWorkingRootPath.toString() + filePath);
        String expectedPathString = backupRootPath.toString() + filePath;

        Mockito.when(validator.fileExists(currentWorkingRootPath)).thenReturn(true);
        Mockito.when(validator.fileExists(backupRootPath)).thenReturn(true);

        Either<String, FilePathInfo> filePathInfo = FilePathInfo.of(currentWorkingRootPath, backupRootPath, false, validator);

        assertThat(filePathInfo.get().fromCurrentToBackupPath(fullCurrentPath).toString(), is(expectedPathString));
    }

    @Test
    public void fromCurrentToBackupPath_Windows_DuplicateRootPath_IsValid() {
        /*
         * Checks that a valid path is returned if many duplicate rootDirectory names are found in the complete path.
         * Only the highest parent directory should be used when building the backup path.
         *
         * Why test for this? its completely possible for a file or other directory to be named the same as the
         * root directory. The highest root (rootDirectory) closest to the top most root drive (C:\) for example must be
         * where the joining of the current file path (fullCurrentPath) and the backup path (expectedPathString) occurs.
         */
        assumeTrue(File.separator.equals("\\"));
        String rootDirectory = "project";

        Path currentWorkingRootPath = Paths.get("C:\\Users\\me\\Desktop\\" + rootDirectory);
        Path backupRootPath = Paths.get("F:\\Stuff\\FolderC\\" + rootDirectory);

        String filePath = "\\" + rootDirectory + "\\Fol" + rootDirectory + "der1\\" + rootDirectory + "\\Folder3_" + rootDirectory + "_\\" + rootDirectory + ".txt";

        Path fullCurrentPath = Paths.get(currentWorkingRootPath.toString() + filePath);
        String expectedPathString = backupRootPath.toString() + filePath;

        Mockito.when(validator.fileExists(currentWorkingRootPath)).thenReturn(true);
        Mockito.when(validator.fileExists(backupRootPath)).thenReturn(true);

        Either<String, FilePathInfo> filePathInfo = FilePathInfo.of(currentWorkingRootPath, backupRootPath, false, validator);

        assertThat(filePathInfo.get().fromCurrentToBackupPath(fullCurrentPath).toString(), is(expectedPathString));
    }

    @Test
    public void fromCurrentToBackupPath_Linux_DuplicateRootPath_IsValid() {
        /*
         * Checks that a valid path is returned if many duplicate rootDirectory names are found in the complete path.
         * Only the highest parent directory should be used when building the backup path.
         *
         * Why test for this? its completely possible for a file or other directory to be named the same as the
         * root directory. The highest root (rootDirectory) closest to the top most root drive '/' for example must be
         * where the joining of the current file path (fullCurrentPath) and the backup path (expectedPathString) occurs.
         */
        assumeTrue(File.separator.equals("/"));
        String rootDirectory = "project";

        Path currentWorkingRootPath = Paths.get("/home/me/work/" + rootDirectory);
        Path backupRootPath = Paths.get("/media/me/USB DISK/FolderEE/" + rootDirectory);

        String filePath = "/" + rootDirectory + "/Fol" + rootDirectory + "der1/" + rootDirectory + "/Folder3_" + rootDirectory + "_/" + rootDirectory + ".txt";

        Path fullCurrentPath = Paths.get(currentWorkingRootPath.toString() + filePath);
        String expectedPathString = backupRootPath.toString() + filePath;

        Mockito.when(validator.fileExists(currentWorkingRootPath)).thenReturn(true);
        Mockito.when(validator.fileExists(backupRootPath)).thenReturn(true);

        Either<String, FilePathInfo> filePathInfo = FilePathInfo.of(currentWorkingRootPath, backupRootPath, false, validator);

        assertThat(filePathInfo.get().fromCurrentToBackupPath(fullCurrentPath).toString(), is(expectedPathString));
    }

    @Test
    public void fromBackupToCurrentPath_Windows_NoDuplicateRootPath_IsValid() {
        // Returns valid path if there is only 1 rootDirectory name in the whole path.
        assumeTrue(File.separator.equals("\\"));
        String rootDirectory = "project";

        Path backupRootPath = Paths.get("F:\\FolderC\\" + rootDirectory);
        Path currentWorkingRootPath = Paths.get("C:\\Users\\me\\Desktop\\" + rootDirectory);

        String filePath = "\\FolderA\\FolderB\\document.txt";

        Path fullBackupPath = Paths.get(backupRootPath.toString() + filePath);
        String expectedPathString = currentWorkingRootPath.toString() + filePath;

        Mockito.when(validator.fileExists(currentWorkingRootPath)).thenReturn(true);
        Mockito.when(validator.fileExists(backupRootPath)).thenReturn(true);

        Either<String, FilePathInfo> filePathInfo = FilePathInfo.of(currentWorkingRootPath, backupRootPath, false, validator);

        assertThat(filePathInfo.get().fromBackupToCurrentPath(fullBackupPath).toString(), is(expectedPathString));
    }

    @Test
    public void fromBackupToCurrentPath_Linux_NoDuplicateRootPath_IsValid() {
        // Returns valid path if there is only 1 rootDirectory name in the whole path.
        assumeTrue(File.separator.equals("/"));
        String rootDirectory = "project";

        Path backupRootPath = Paths.get("/media/me/USB DISK/FolderC/" + rootDirectory);
        Path currentWorkingRootPath = Paths.get("/home/me/work/" + rootDirectory);

        String filePath = "/FolderA/FolderB/document.txt";

        Path fullBackupPath = Paths.get(backupRootPath.toString() + filePath);
        String expectedPathString = currentWorkingRootPath.toString() + filePath;

        Mockito.when(validator.fileExists(currentWorkingRootPath)).thenReturn(true);
        Mockito.when(validator.fileExists(backupRootPath)).thenReturn(true);

        Either<String, FilePathInfo> filePathInfo = FilePathInfo.of(currentWorkingRootPath, backupRootPath, false, validator);

        assertThat(filePathInfo.get().fromBackupToCurrentPath(fullBackupPath).toString(), is(expectedPathString));
    }

    @Test
    public void fromBackupToCurrentPath_Windows_DuplicateRootPath_IsValid() {
        /*
         * Checks that a valid path is returned if many duplicate rootDirectory names are found in the complete path.
         * Only the highest parent directory should be used when building the backup path.
         *
         * Why test for this? its completely possible for a file or other directory to be named the same as the
         * root directory. The highest root (rootDirectory) closest to the top most root drive (C:\) for example must be
         * where the joining of the backup file path (fullBackupPath) and the current file path (expectedPathString) occurs.
         *
         * This test is just the opposite of fromCurrentToBackupPath_DuplicateRootPath_IsValid
         */
        assumeTrue(File.separator.equals("\\"));
        String rootDirectory = "project";

        Path backupRootPath = Paths.get("F:\\FolderC\\" + rootDirectory);
        Path currentWorkingRootPath = Paths.get("C:\\Users\\me\\Desktop\\" + rootDirectory);

        String filePath = "\\" + rootDirectory + "\\Fol" + rootDirectory + "der1\\" + rootDirectory + "\\Folder3_" + rootDirectory + "_\\" + rootDirectory + ".txt";

        Path fullBackupPath = Paths.get(backupRootPath.toString() + filePath);
        String expectedPathString = currentWorkingRootPath.toString() + filePath;

        Mockito.when(validator.fileExists(currentWorkingRootPath)).thenReturn(true);
        Mockito.when(validator.fileExists(backupRootPath)).thenReturn(true);

        Either<String, FilePathInfo> filePathInfo = FilePathInfo.of(currentWorkingRootPath, backupRootPath, false, validator);

        assertThat(filePathInfo.get().fromBackupToCurrentPath(fullBackupPath).toString(), is(expectedPathString));
    }

    @Test
    public void fromBackupToCurrentPath_Linux_DuplicateRootPath_IsValid() {
        /*
         * Checks that a valid path is returned if many duplicate rootDirectory names are found in the complete path.
         * Only the highest parent directory should be used when building the backup path.
         *
         * Why test for this? its completely possible for a file or other directory to be named the same as the
         * root directory. The highest root (rootDirectory) closest to the top most root drive '/' for example must be
         * where the joining of the backup file path (fullBackupPath) and the current file path (expectedPathString) occurs.
         *
         * This test is just the opposite of fromCurrentToBackupPath_DuplicateRootPath_IsValid
         */
        assumeTrue(File.separator.equals("/"));
        String rootDirectory = "project";

        Path currentWorkingRootPath = Paths.get("/home/me/work/" + rootDirectory);
        Path backupRootPath = Paths.get("/media/me/USB DISK/FolderEE/" + rootDirectory);

        String filePath = "/" + rootDirectory + "/Fol" + rootDirectory + "der1/" + rootDirectory + "/Folder3_" + rootDirectory + "_/" + rootDirectory + ".txt";

        Path fullBackupPath = Paths.get(backupRootPath.toString() + filePath);
        String expectedPathString = currentWorkingRootPath.toString() + filePath;

        Mockito.when(validator.fileExists(currentWorkingRootPath)).thenReturn(true);
        Mockito.when(validator.fileExists(backupRootPath)).thenReturn(true);

        Either<String, FilePathInfo> filePathInfo = FilePathInfo.of(currentWorkingRootPath, backupRootPath, false, validator);

        assertThat(filePathInfo.get().fromBackupToCurrentPath(fullBackupPath).toString(), is(expectedPathString));
    }

    @Test
    public void getAllPaths_Windows_ManySubDirectories_IsValid() {
        /*
         * See javadoc for FilePathInfo.getAllPaths, this is the normal case of a path that has many sub directories
         * after the main project root.
         *
         * C:\\Users\\me\\Desktop\\project\\special\\superSpecial\\ultraSpecial
         */
        assumeTrue(File.separator.equals("\\"));
        String rootDirectory = "project";
        Path currentWorkingRootPath = Paths.get("C:\\Users\\me\\Desktop\\" + rootDirectory);
        Path backupRootPath = Paths.get("F:\\Stuff\\FolderC\\" + rootDirectory);

        Mockito.when(validator.fileExists(currentWorkingRootPath)).thenReturn(true);
        Mockito.when(validator.fileExists(backupRootPath)).thenReturn(true);
        Either<String, FilePathInfo> filePathInfoEither = FilePathInfo.of(currentWorkingRootPath, backupRootPath, false, validator);
        assertThat(filePathInfoEither.isRight(), is(true));

        Path fullPath = Paths.get("C:\\Users\\me\\Desktop\\" + rootDirectory + "\\special\\superSpecial\\ultraSpecial");

        List<Path> expected = Arrays.asList(
                Paths.get("C:\\Users\\me\\Desktop\\" + rootDirectory + "\\special"),
                Paths.get("C:\\Users\\me\\Desktop\\" + rootDirectory + "\\special\\superSpecial"),
                Paths.get("C:\\Users\\me\\Desktop\\" + rootDirectory + "\\special\\superSpecial\\ultraSpecial"),
                Paths.get("C:\\Users\\me\\Desktop\\" + rootDirectory)
        );
        List<Path> allPaths = filePathInfoEither.get().getAllPaths(fullPath);
        assertThat(allPaths, hasSize(4));
        assertThat(allPaths, containsInAnyOrder(expected.toArray()));
    }

    @Test
    public void getAllPaths_Linux_ManySubDirectories_IsValid() {
        /*
         * See javadoc for FilePathInfo.getAllPaths, this is the normal case of a path that has many sub directories
         * after the main project root.
         *
         * /home/me/work/project/special/superSpecial/ultraSpecial"
         */
        assumeTrue(File.separator.equals("/"));
        String rootDirectory = "project";
        Path currentWorkingRootPath = Paths.get("/home/me/work/" + rootDirectory);
        Path backupRootPath = Paths.get("/media/me/USB DISK/" + rootDirectory);

        Mockito.when(validator.fileExists(currentWorkingRootPath)).thenReturn(true);
        Mockito.when(validator.fileExists(backupRootPath)).thenReturn(true);
        Either<String, FilePathInfo> filePathInfoEither = FilePathInfo.of(currentWorkingRootPath, backupRootPath, false, validator);
        assertThat(filePathInfoEither.isRight(), is(true));

        Path fullPath = Paths.get("/home/me/work/" + rootDirectory + "/special/superSpecial/ultraSpecial");

        List<Path> expected = Arrays.asList(
                Paths.get("/home/me/work/" + rootDirectory + "/special"),
                Paths.get("/home/me/work/" + rootDirectory + "/special/superSpecial"),
                Paths.get("/home/me/work/" + rootDirectory + "/special/superSpecial/ultraSpecial"),
                Paths.get("/home/me/work/" + rootDirectory)
        );
        List<Path> allPaths = filePathInfoEither.get().getAllPaths(fullPath);
        assertThat(allPaths, hasSize(4));
        assertThat(allPaths, containsInAnyOrder(expected.toArray()));
    }

    @Test
    public void getAllPaths_Windows_SingleSubDirectories_IsValid() {
        /*
         * See javadoc for FilePathInfo.getAllPaths, path that has no sub directories.
         * C:\\Users\\me\\Desktop\\project\\stuff
         *
         * 2 paths expected
         * C:\\Users\\me\\Desktop\\project (currentWorkingRootPath)
         * C:\\Users\\me\\Desktop\\project\\stuff
         */
        assumeTrue(File.separator.equals("\\"));
        String rootDirectory = "project";

        Path currentWorkingRootPath = Paths.get("C:\\Users\\me\\Desktop\\" + rootDirectory);
        Path backupRootPath = Paths.get("F:\\FolderC\\" + rootDirectory);

        Mockito.when(validator.fileExists(currentWorkingRootPath)).thenReturn(true);
        Mockito.when(validator.fileExists(backupRootPath)).thenReturn(true);
        Either<String, FilePathInfo> filePathInfoEither = FilePathInfo.of(currentWorkingRootPath, backupRootPath, false, validator);
        assertThat(filePathInfoEither.isRight(), is(true));

        Path fullPath = Paths.get("C:\\Users\\me\\Desktop\\" + rootDirectory + "\\stuff");

        List<Path> expected = Arrays.asList(
                Paths.get("C:\\Users\\me\\Desktop\\" + rootDirectory),
                Paths.get("C:\\Users\\me\\Desktop\\" + rootDirectory + "\\stuff")
        );

        List<Path> allPaths = filePathInfoEither.get().getAllPaths(fullPath);
        assertThat(allPaths, hasSize(2));
        assertThat(allPaths, containsInAnyOrder(expected.toArray()));
    }

    @Test
    public void getAllPaths_Linux_SingleSubDirectories_IsValid() {
        /*
         * See javadoc for FilePathInfo.getAllPaths, path that has no sub directories.
         * /home/me/work/project/stuff
         *
         * 2 paths expected
         * /home/me/work/project (currentWorkingRootPath)
         * /home/me/work/project/stuff
         */
        assumeTrue(File.separator.equals("/"));
        String rootDirectory = "project";

        Path currentWorkingRootPath = Paths.get("/home/me/work/" + rootDirectory);
        Path backupRootPath = Paths.get("/media/me/USB DISK/" + rootDirectory);

        Mockito.when(validator.fileExists(currentWorkingRootPath)).thenReturn(true);
        Mockito.when(validator.fileExists(backupRootPath)).thenReturn(true);
        Either<String, FilePathInfo> filePathInfoEither = FilePathInfo.of(currentWorkingRootPath, backupRootPath, false, validator);
        assertThat(filePathInfoEither.isRight(), is(true));

        Path fullPath = Paths.get("/home/me/work/" + rootDirectory + "/stuff");

        List<Path> expected = Arrays.asList(
                Paths.get("/home/me/work/" + rootDirectory + "/stuff"),
                Paths.get("/home/me/work/" + rootDirectory)
        );

        List<Path> allPaths = filePathInfoEither.get().getAllPaths(fullPath);
        assertThat(allPaths, hasSize(2));
        assertThat(allPaths, containsInAnyOrder(expected.toArray()));
    }


    @Test
    public void getAllPaths_Windows_NoSubDirectories_IsValid() {
        /*
         * See javadoc for FilePathInfo.getAllPaths, supply the currentWorkingRootPath only.
         * C:\\Users\\me\\Desktop\\project (currentWorkingRootPath)
         *
         * Only this currentWorkingRootPath should be returned
         */
        assumeTrue(File.separator.equals("\\"));
        String rootDirectory = "project";

        Path currentWorkingRootPath = Paths.get("C:\\Users\\me\\Desktop\\" + rootDirectory);
        Path backupRootPath = Paths.get("F:\\Stuff\\More\\" + rootDirectory);

        Mockito.when(validator.fileExists(currentWorkingRootPath)).thenReturn(true);
        Mockito.when(validator.fileExists(backupRootPath)).thenReturn(true);

        Either<String, FilePathInfo> filePathInfoEither = FilePathInfo.of(currentWorkingRootPath, backupRootPath, false, validator);
        assertThat(filePathInfoEither.isRight(), is(true));

        Path fullPath = Paths.get("C:\\Users\\me\\Desktop\\" + rootDirectory);

        List<Path> allPaths = filePathInfoEither.get().getAllPaths(fullPath);
        assertThat(allPaths, hasSize(1));
        assertThat(allPaths, hasItem(fullPath));
    }

    @Test
    public void getAllPaths_Linux_NoSubDirectories_IsValid() {
        /*
         * See javadoc for FilePathInfo.getAllPaths, supply the currentWorkingRootPath only.
         * /home/me/work/project (currentWorkingRootPath)
         *
         * Only this currentWorkingRootPath should be returned
         */
        assumeTrue(File.separator.equals("/"));
        String rootDirectory = "project";

        Path currentWorkingRootPath = Paths.get("/home/me/work/" + rootDirectory);
        Path backupRootPath = Paths.get("/media/me/USB DISK/" + rootDirectory);

        Mockito.when(validator.fileExists(currentWorkingRootPath)).thenReturn(true);
        Mockito.when(validator.fileExists(backupRootPath)).thenReturn(true);

        Either<String, FilePathInfo> filePathInfoEither = FilePathInfo.of(currentWorkingRootPath, backupRootPath, false, validator);
        assertThat(filePathInfoEither.isRight(), is(true));

        Path fullPath = Paths.get("/home/me/work/" + rootDirectory);

        List<Path> allPaths = filePathInfoEither.get().getAllPaths(fullPath);
        assertThat(allPaths, hasSize(1));
        assertThat(allPaths, hasItem(fullPath));
    }

    @Test
    public void getAllPaths_Windows_ManySubDirectoriesAndFile_IsValid() {
        /*
         * See javadoc for FilePathInfo.getAllPaths, this is the normal case of a path that has many sub directories
         * after the main project root but with a filename too.
         */
        assumeTrue(File.separator.equals("\\"));
        String rootDirectory = "project";

        Path currentWorkingRootPath = Paths.get("C:\\Users\\me\\Desktop\\" + rootDirectory);
        Path backupRootPath = Paths.get("F:\\Stuff\\More\\" + rootDirectory);

        Mockito.when(validator.fileExists(currentWorkingRootPath)).thenReturn(true);
        Mockito.when(validator.fileExists(backupRootPath)).thenReturn(true);
        Either<String, FilePathInfo> filePathInfoEither = FilePathInfo.of(currentWorkingRootPath, backupRootPath, false, validator);
        assertThat(filePathInfoEither.isRight(), is(true));

        Path fullPath = Paths.get("C:\\Users\\me\\Desktop\\" + rootDirectory + "\\special\\superSpecial\\ultraSpecial\\document.txt");

        List<Path> expected = Arrays.asList(
                Paths.get("C:\\Users\\me\\Desktop\\" + rootDirectory + "\\special"),
                Paths.get("C:\\Users\\me\\Desktop\\" + rootDirectory + "\\special\\superSpecial"),
                Paths.get("C:\\Users\\me\\Desktop\\" + rootDirectory + "\\special\\superSpecial\\ultraSpecial"),
                Paths.get("C:\\Users\\me\\Desktop\\" + rootDirectory + "\\special\\superSpecial\\ultraSpecial\\document.txt"),
                Paths.get("C:\\Users\\me\\Desktop\\" + rootDirectory )
        );

        List<Path> allPaths = filePathInfoEither.get().getAllPaths(fullPath);
        assertThat(allPaths, hasSize(5));
        assertThat(allPaths, containsInAnyOrder(expected.toArray()));
    }

    @Test
    public void getAllPaths_Linux_ManySubDirectoriesAndFile_IsValid() {
        /*
         * See javadoc for FilePathInfo.getAllPaths, this is the normal case of a path that has many sub directories
         * after the main project root but with a filename too.
         */
        assumeTrue(File.separator.equals("/"));
        String rootDirectory = "project";

        Path currentWorkingRootPath = Paths.get("/home/me/work/" + rootDirectory);
        Path backupRootPath = Paths.get("/media/me/USB DISK/" + rootDirectory);

        Mockito.when(validator.fileExists(currentWorkingRootPath)).thenReturn(true);
        Mockito.when(validator.fileExists(backupRootPath)).thenReturn(true);
        Either<String, FilePathInfo> filePathInfoEither = FilePathInfo.of(currentWorkingRootPath, backupRootPath, false, validator);
        assertThat(filePathInfoEither.isRight(), is(true));

        Path fullPath = Paths.get("/home/me/work/" + rootDirectory + "/special/superSpecial/ultraSpecial/document.txt");

        List<Path> expected = Arrays.asList(
                Paths.get("/home/me/work/" + rootDirectory + "/special"),
                Paths.get("/home/me/work/" + rootDirectory + "/special/superSpecial"),
                Paths.get("/home/me/work/" + rootDirectory + "/special/superSpecial/ultraSpecial"),
                Paths.get("/home/me/work/" + rootDirectory + "/special/superSpecial/ultraSpecial/document.txt"),
                Paths.get("/home/me/work/" + rootDirectory )
        );

        List<Path> allPaths = filePathInfoEither.get().getAllPaths(fullPath);
        assertThat(allPaths, hasSize(5));
        assertThat(allPaths, containsInAnyOrder(expected.toArray()));
    }
}