package fileBackup.fileAnalysis;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

/**
 * Created by matt on 01-Jul-17.
 */
public class FileChangeResultTest {

    private FileChangeResult fileChangeResult;

    @Before
    public void setUp() {
        fileChangeResult = new FileChangeResult();
    }

    @Test
    public void incrementTotalFilesModified() {
        fileChangeResult.incrementTotalFilesModified();
        fileChangeResult.incrementTotalFilesModified();
        assertThat(fileChangeResult.getTotalFilesModified(), is(2L));
    }

    @Test
    public void incrementTotalFilesUnmodified() {
        fileChangeResult.incrementTotalFilesUnmodified();
        fileChangeResult.incrementTotalFilesUnmodified();
        assertThat(fileChangeResult.getTotalFilesUnmodified(), is(2L));
    }

    @Test
    public void incrementTotalNewFiles() {
        fileChangeResult.incrementTotalNewFiles();
        fileChangeResult.incrementTotalNewFiles();
        assertThat(fileChangeResult.getTotalNewFiles(), is(2L));
    }

    @Test
    public void incrementTotalNewDirectories() {
        fileChangeResult.incrementTotalNewDirectories();
        fileChangeResult.incrementTotalNewDirectories();
        assertThat(fileChangeResult.getTotalNewDirectories(), is(2L));
    }


}