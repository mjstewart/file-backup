package fileBackup.fileAnalysis;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Created by matt on 02-Jul-17.
 */
public class FileAnalysisResultTest {

    private ModifiedFileWalkerResult modifiedFileWalkerResult;

    @Before
    public void setUp() {
        modifiedFileWalkerResult = new ModifiedFileWalkerResult();
    }

    @Test
    public void incrementTotalFilesScanned() {
        modifiedFileWalkerResult.incrementTotalFilesScanned();
        modifiedFileWalkerResult.incrementTotalFilesScanned();
        assertThat(modifiedFileWalkerResult.getTotalFilesScanned(), is(2L));
    }

    @Test
    public void incrementTotalDirectoriesScanned() {
        modifiedFileWalkerResult.incrementTotalDirectoriesScanned();
        modifiedFileWalkerResult.incrementTotalDirectoriesScanned();
        assertThat(modifiedFileWalkerResult.getTotalDirectoriesScanned(), is(2L));
    }

}