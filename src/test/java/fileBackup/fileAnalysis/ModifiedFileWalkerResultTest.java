package fileBackup.fileAnalysis;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

/**
 * Created by matt on 01-Jul-17.
 */
public class ModifiedFileWalkerResultTest {

    private ModifiedFileWalkerResult modifiedFileWalkerResult;

    @Before
    public void setUp() {
        modifiedFileWalkerResult = new ModifiedFileWalkerResult();
    }

    @Test
    public void incrementTotalFilesModified() {
        modifiedFileWalkerResult.incrementTotalFilesModified();
        modifiedFileWalkerResult.incrementTotalFilesModified();
        assertThat(modifiedFileWalkerResult.getTotalFilesModified(), is(2L));
    }

    @Test
    public void incrementTotalFilesUnmodified() {
        modifiedFileWalkerResult.incrementTotalFilesUnmodified();
        modifiedFileWalkerResult.incrementTotalFilesUnmodified();
        assertThat(modifiedFileWalkerResult.getTotalFilesUnmodified(), is(2L));
    }

    @Test
    public void incrementTotalNewFiles() {
        modifiedFileWalkerResult.incrementTotalNewFiles();
        modifiedFileWalkerResult.incrementTotalNewFiles();
        assertThat(modifiedFileWalkerResult.getTotalNewFiles(), is(2L));
    }

    @Test
    public void incrementTotalNewDirectories() {
        modifiedFileWalkerResult.incrementTotalNewDirectories();
        modifiedFileWalkerResult.incrementTotalNewDirectories();
        assertThat(modifiedFileWalkerResult.getTotalNewDirectories(), is(2L));
    }


}