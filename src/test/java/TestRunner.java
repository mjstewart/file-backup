import fileBackup.fileAnalysis.FileAnalysisResultTest;
import fileBackup.fileAnalysis.ModifiedFileWalkerResultTest;
import fileBackup.fileAnalysis.FilePathInfoTest;
import fileBackup.monitoring.pathMapping.PathMappingStrategyTest;
import fileBackup.monitoring.pathMapping.CurrentToBackupPathMappingTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Created by matt on 01-Jul-17.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        FileAnalysisResultTest.class,
        ModifiedFileWalkerResultTest.class,
        FilePathInfoTest.class,
        PathMappingStrategyTest.class,
        CurrentToBackupPathMappingTest.class
})
public class TestRunner {
}
