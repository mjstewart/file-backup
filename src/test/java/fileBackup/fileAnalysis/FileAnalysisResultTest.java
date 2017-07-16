package fileBackup.fileAnalysis;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Created by matt on 02-Jul-17.
 */
public class FileAnalysisResultTest {

    private FileChangeResult fileChangeResult;

    @Before
    public void setUp() {
        fileChangeResult = new FileChangeResult();
    }

    @Test
    public void incrementTotalFilesScanned() {
        fileChangeResult.incrementTotalFilesScanned();
        fileChangeResult.incrementTotalFilesScanned();
        assertThat(fileChangeResult.getTotalFilesScanned(), is(2L));
    }

    @Test
    public void incrementTotalDirectoriesScanned() {
        fileChangeResult.incrementTotalDirectoriesScanned();
        fileChangeResult.incrementTotalDirectoriesScanned();
        assertThat(fileChangeResult.getTotalDirectoriesScanned(), is(2L));
    }

}