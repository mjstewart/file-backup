package fileBackup.monitoring.persistence;

import javax.persistence.*;

/**
 * Represents a path that has seen CRUD activity.
 *
 * Created by matt on 11-Jul-17.
 */
@Entity
public class WatchedFile {
    @Id
    @GeneratedValue(generator = "WatchedFile_SeqGen", strategy = GenerationType.AUTO)
    @SequenceGenerator(name = "WatchedFile_SeqGen", sequenceName = "WatchedFile_Seq", allocationSize = 1)
    private int id;

    @Column(nullable = false)
    private int hashCode;

    @Column(nullable = false)
    private String path;

    public WatchedFile() {}

    /**
     * @param path The {@code String} representation of this path.
     * @param hashCode The hashCode for the path.
     */
    public WatchedFile(String path, int hashCode) {
        this.path = path;
        this.hashCode = hashCode;
    }

    public int getHashCode() {
        return hashCode;
    }

    public String getPath() {
        return path;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        WatchedFile that = (WatchedFile) o;

        return hashCode == that.hashCode;
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public String toString() {
        return "WatchedFile{" +
                "id=" + id +
                ", getMappedHashCode=" + hashCode +
                ", path='" + path + '\'' +
                '}';
    }
}
