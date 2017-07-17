package fileBackup.fileAnalysis;

/**
 * Possible backup operations that can be performed. The additional description can be used for UI labels etc.
 *
 * Created by matt on 30-Jun-17.
 */
public enum FileType {
    File("File"),
    Directory("Directory"),
    Symbolic("Symbolic Link"),
    Unknown("Unknown");

    private String description;

    FileType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
