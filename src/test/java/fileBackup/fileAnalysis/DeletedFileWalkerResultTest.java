package fileBackup.fileAnalysis;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class DeletedFileWalkerResultTest {
    private DeletedFileWalkerResult deletedFileWalkerResult;

    @Before
    public void setUp() {
        deletedFileWalkerResult = new DeletedFileWalkerResult();
    }

    @Test
    public void incrementTotalFilesDeleted() {
        deletedFileWalkerResult.incrementTotalFilesDeleted();
        deletedFileWalkerResult.incrementTotalFilesDeleted();
        assertThat(deletedFileWalkerResult.getTotalFilesDeleted(), is(2L));
    }

    @Test
    public void incrementTotalDirectoriesDeleted() {
        deletedFileWalkerResult.incrementTotalDirectoriesDeleted();
        deletedFileWalkerResult.incrementTotalDirectoriesDeleted();
        assertThat(deletedFileWalkerResult.getTotalDirectoriesDeleted(), is(2L));
    }

}