package fileBackup.monitoring;

import fileBackup.events.Publisher;
import fileBackup.events.Subscriber;
import fileBackup.fileAnalysis.FileAccessError;
import fileBackup.fileAnalysis.FilePathInfo;
import fileBackup.monitoring.pathMapping.CurrentToBackupPathMapping;
import fileBackup.monitoring.pathMapping.PathMappingStrategy;
import fileBackup.monitoring.persistence.FileBackupRepository;
import fileBackup.monitoring.persistence.LogMessage;
import fileBackup.monitoring.persistence.Repository;
import fileBackup.monitoring.persistence.WatchedFile;
import io.vavr.control.Either;
import ui.tasks.Shutdownable;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.stream.Collectors;

import static java.nio.file.FileVisitOption.FOLLOW_LINKS;
import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.nio.file.StandardWatchEventKinds.*;

/**
 * <p><b>IMPORTANT: </b></p>
 * <p>{@code WatchService} is very delicate and most of it depends on the underlying native event system. In other words
 * nothing is guaranteed so this may not be the best method for backup! but using this in combination with the manual
 * backup option works well. https://bugs.openjdk.java.net/browse/JDK-6972833
 * highlights that windows is a limitation since it wont let you rename/delete/move directories because registering
 * a the directory locks it. Any file operations is fine its just directories which is the issue.
 * Also on mac os there is no event system, the implementation just polls the entire filesystem periodically so its
 * VERY SLOW and doing a manual backup would be faster. Linux seems to be the best operating system for every feature
 * running as intended.</p>
 *
 * The {@code DirectoryWatcher} forms the strategy to detect if a whole directory can be skipped when running the backup.
 * During the day when live monitoring is on, this watcher runs in a separate thread listening for events for each
 * registered directory. The events correspond to a {@code Path} that has been changed based on the watch event kinds
 * such as {@code ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE}.
 *
 * <p>There is a slight issue with these events, for example when a new file is created windows gives it a temporary
 * name such as 'New Document XXX' producing the below events. Even temporary files for word and excel get created
 * causing similar events. There is no guarantee as to how many of these events get created either as per
 * {@code WatchService} docs.</p>
 *
 * <pre>
 *     1. ENTRY_CREATE: C:\Users\me\Desktop\backup\stuff\New Text Document.txt
 *     2. ENTRY_MODIFY: C:\Users\me\Desktop\backup\stuff\New Text Document.txt
 *     3. ENTRY_MODIFY: C:\Users\me\Desktop\backup\stuff
 *     4. ENTRY_DELETE: C:\Users\me\Desktop\backup\stuff\New Text Document.txt
 *     5. ENTRY_CREATE: C:\Users\me\Desktop\backup\stuff\correctlyNamedFile.txt
 *     6. ENTRY_MODIFY: C:\Users\me\Desktop\backup\stuff
 * </pre>
 *
 * <p>Its incorrect to schedule a create file task when the backup occurs for creating the 'New Text Document'
 * since it later on gets modified. If you did do this the backup will end up with a new file for no reason along with
 * another file that was created when it's renamed after creation. To avoid all these corner cases of events
 * a different approach is taken.</p>
 *
 * <b><p>Strategy</p></b>
 * <p>A h2 embedded database is used to track 'directory activity' not individual files. Using the above sequence
 * of events all 6 events can be aggregated into just the 1 event where we have detected activity in
 * 'C:\Users\me\Desktop\backup\stuff' since 'stuff' is the parent directory of the new .txt file.</p>
 *
 * <p>In actual fact, ALL paths leading to the project root need to be added as having seen activity inorder to
 * tell the {@code AbstractFileScanner} to locate the changed paths through a {@code DirectoryFilter}.
 * {@link #processEvent} outlines this process in detail. The process is briefly documented below</p>
 *
 * <pre>
 *      C:\Users\me\Desktop\backup\stuff\important\doc.txt
 *                           root
 *
 *      - All paths leading to the root must be marked as active.
 *      C:\Users\me\Desktop\backup
 *      C:\Users\me\Desktop\backup\stuff
 *      C:\Users\me\Desktop\backup\stuff\important
 *
 *      - The reverse paths for the backup must be included so the DeleteFileCollector works correctly.
 *      F:\Users\backup
 *      F:\Users\backup\stuff
 *      F:\Users\backup\stuff\important
 * </pre>
 *
 * <p>So for example, if 50/500 files were modified in directory C:\Users\me\Desktop\backup\stuff, all we know
 * is there there has been some activity in this directory, we don't know the actual specific files. This is
 * where the {@code ModifiedFileCollector and DeletedFileCollector} come in and find them based on looking at the
 * above active paths.</p>
 *
 * <p>The database keeps the getMappedHashCode of each active path resulting in a {@code HashSet} of these hashCodes
 * representing directories that have had activity in them throughout the day.
 * During the file analysis phase, the {@code ModifiedFileCollector and DeletedFileCollector} can hashCode their current
 * path when in {@code preVisitDirectory} and check if its in the {@code Set} of hashCodes from the database.
 * If its not, the entire directory can be skipped otherwise scan deeper into the file system to pick up the modified files.
 * This results in significantly less work as most of the directories can be skipped. Using the existing file
 * collectors follows exactly the same approach as doing a manual backup where every file is scanned for last modified
 * date comparison, the only difference is here we can take shortcuts and not visit directories that haven't seen
 * activity during the day saving huge amounts of time on deeply nested file system structures.</p>
 *
 * <p>So if a directory has had activity in it, the file collectors will scan that directory by looking at each file
 * and only continuing deeper if its sub directories have seen activity by checking if the paths hashCode is in the
 * {@code Set} containing the days active directories. If no activity is seen on a directory it is
 * completely skipped.</p>
 *
 * <p>The embedded database is used rather than in-memory so power failures wont result in losing the days backup
 * activity which will allow the user to resume live monitoring right from where the computer turned off. Without this
 * the only option would be to run a full manual backup.</p>
 *
 * <p>Before running the backup, all logs should be checked to ensure the user is warned the backup could result in
 * losing data if there exists at least 1 {@code LogMessage} at {@code Level.SEVERE}. All {@code LogMessage}s created
 * in this class therefore follow a convention where {@code Level.SEVERE} is used to signify the backup is likely to
 * lose data if continued and it would be a wiser option to do a manual backup using the fully file system scan.</p>
 *
 * Created by matt on 11-Jul-17.
 */
public class DirectoryWatcher implements Runnable, Publisher<LogMessage>, Shutdownable {
    private WatchService watcher;
    private Map<WatchKey,Path> keys;

    private List<Subscriber<LogMessage>> subscribers;

    private AtomicBoolean running = new AtomicBoolean(false);
    private FilePathInfo filePathInfo;

    /**
     * @param filePathInfo The application {@code FilePathInfo} which provides the currentWorkingRootPath which this
     *                     {@code DirectoryWatcher} will begin file walking from and registering directories to watch.
     */
    public DirectoryWatcher(FilePathInfo filePathInfo) {
        this.filePathInfo = filePathInfo;
    }

    /**
     * First call the empty constructor then this method. It has to be done this way so the caller can bind to the
     * runningProperty so the UI can show feedback while all the directories are registered. This instance is blocked
     * during file walking hence there is no opportunity to bind once file walking begins.
     *
     * @return Either the valid {@code DirectoryWatcher} or the error in Either.left representing a problem
     * in registering all directories indicating live monitoring is compromised as not all directories are being
     * watched.
     */
    public Either<FileAccessError, DirectoryWatcher> create() {
        try {
            watcher = FileSystems.getDefault().newWatchService();
            keys = new HashMap<>();
            subscribers = Collections.synchronizedList(new ArrayList<>());
            registerAll(filePathInfo.getCurrentWorkingRootPath());
            return Either.right(this);
        } catch (Exception e) {
            return Either.left(new FileAccessError("Unable to register all directories for monitoring: " +
                    e.getMessage()));
        }
    }

    private String surroundInQuotes(String value) {
        return "\"" + value + "\"";
    }

    @Override
    public void addSubscriber(Subscriber<LogMessage> subscriber) {
        subscribers.add(subscriber);
    }

    @Override
    public void removeSubscriber(Subscriber<LogMessage> subscriber) {
        subscribers.remove(subscriber);
    }

    private void sendLogMessage(LogMessage logMessage) {
        subscribers.forEach(subscriber -> subscriber.update(logMessage));

        Either<DBError, Serializable> saveResult = Repository.save(logMessage);
        if (saveResult.isLeft()) {
            String reason = saveResult.getLeft().getReason();
            LogMessage failedLogMessage = new LogMessage(Level.SEVERE, "Log message could not be saved: "
                    + reason + ". Performing a backup could result in lost data");
            subscribers.forEach(subscriber -> subscriber.update(failedLogMessage));
        }
    }

    @Override
    public void stop() {
        /*
         * All existing keys must be cancelled as its possible for 'this' DirectoryWatcher instance to still be
         * alive in the UI but the keys are still registered to the underlying native WatchService. By cancelling
         * each existing key, if another DirectoryWatcher is created then we start from scratch each time. If this
         * isn't done existing watch keys will hang around and there will be invalid watch key errors when registerAll
         * tries to register the directory to the WatchService again.
         */
        keys.forEach((key, path) -> key.cancel());
        sendLogMessage(new LogMessage(Level.WARNING, "DirectoryWatcher has received a request to stop"));
        running.set(false);
    }

    /**
     * Register the given directory, and all its sub-directories, with the WatchService.
     *
     * @param startDirectory Register this directory and all its sub directories
     * @throws Exception If a directory could not be registered.
     */
    private void registerAll(Path startDirectory) throws Exception {
        // register directory and sub-directories
        if (filePathInfo.isFollowSymlinks()) {
            EnumSet<FileVisitOption> opts = EnumSet.of(FOLLOW_LINKS);
            Files.walkFileTree(startDirectory, opts, Integer.MAX_VALUE, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                        throws IOException {
                    WatchKey key = dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
                    keys.put(key, dir);
                    return FileVisitResult.CONTINUE;
                }
            });
        } else {
            Files.walkFileTree(startDirectory, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                        throws IOException {
                    WatchKey key = dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
                    keys.put(key, dir);
                    return FileVisitResult.CONTINUE;
                }
            });
        }
    }

    @Override
    public void run() {
        running.set(true);

        while (running.get()) {
            WatchKey key;
            try {
                // sit and block waiting for an event
                key = watcher.take();
            } catch (InterruptedException x) {
                stop();
                sendLogMessage(new LogMessage(Level.SEVERE, "DirectoryWatcher has unexpectedly shutdown: " +
                        x.getMessage()));
                return;
            }

            if (!running.get()) {
                // watcher.take blocks and running could have changed so another check is made.
                return;
            }

            Path dir = keys.get(key);

            if (dir == null) {
                sendLogMessage(new LogMessage(Level.SEVERE, "No WatchKey found for received key. A directory " +
                        "may not have been registered correctly or was cancelled"));
                continue;
            }

            for (WatchEvent<?> event: key.pollEvents()) {
                WatchEvent.Kind eventKind = event.kind();

                if (eventKind == OVERFLOW) {
                    sendLogMessage(new LogMessage(Level.SEVERE, "Overflow received. Events might have been lost " +
                            "resulting in potential data loss"));
                    continue;
                }

                // Context for directory entry event is the file name of entry
                @SuppressWarnings("unchecked")
                WatchEvent<Path> ev = (WatchEvent<Path>)event;

                /*
                 * Resolve the file name against the directory. If filename is 'test' and the directory is 'foo'
                 * the resolved name is 'test/foo'
                 */
                Path fileName = ev.context();
                Path child = dir.resolve(fileName);
                processEvent(child, eventKind);

                // if directory is created, register it and its sub-directories
                if (eventKind == ENTRY_CREATE) {
                    try {
                        if (Files.isDirectory(child, NOFOLLOW_LINKS)) {
                            registerAll(child);
                            sendLogMessage(new LogMessage(Level.INFO, "New directory " + surroundInQuotes(child.toString())
                                    + " created and is now being monitored"));
                        }
                    } catch (Exception x) {
                        sendLogMessage(new LogMessage(Level.SEVERE, "New directory " + surroundInQuotes(child.toString())
                                + " created but all its containing directories could not be registered for monitoring"));
                    }
                }
            }

            /*
             * reset puts the WatchKey back on the queue to wait for more events, if you don't call reset the key no
             * longer listens for events.
             */
            boolean valid = key.reset();
            if (!valid) {
                /*
                 * What an invalid watch key means is difficult to say...
                 *
                 * Based on the WatchService docs, the watch key can be cancelled when the file system is no longer accessible.
                 * The definition of this is unknown so there could be a legitimate issue which would make the backup
                 * at risk of losing data.
                 *
                 * The main use of checking if a key is invalid seems to be on directory deletion where WatchKeys
                 * for sub directories are automatically cancelled correctly on linux but when it comes to Windows
                 * there are issues in deleting directories. Its not advised to use live monitoring on windows
                 * if deleted files need to be tracked.
                 *
                 * In other words, logging severe will stop the backup from occurring as its not really clear that
                 * the backup is still ok, it could be but then again a key for a directory could be invalid but
                 * the directory has not been deleted which will result in no files being monitored for that directory
                 * resulting in potential loss of data.
                 */
                sendLogMessage(new LogMessage(Level.SEVERE, "WatchKey for "  + surroundInQuotes(dir.toString()) + " is invalid" +
                        ", no more events will be received for this path"));
                keys.remove(key);

                if (keys.isEmpty()) {
                    sendLogMessage(new LogMessage(Level.SEVERE, "All directories are inaccessible and no longer registered"));
                    stop();
                }
            }
        }
    }

    /**
     * Persist and log.
     *
     * <p>Log the full path name, user doesn't need to be aware about implementation details of only detecting
     * 'directory activity' rather than individual files.</p>
     *
     * @param path The full {@code Path} including file name.
     */
    private void processEvent(Path path, WatchEvent.Kind eventKind) {
        Either<DBError, List<WatchedFile>> allExistingFilesEither = FileBackupRepository.getAllFiles();
        if (allExistingFilesEither.isLeft()) {
            String reason = allExistingFilesEither.getLeft().getReason();
            sendLogMessage(new LogMessage(Level.SEVERE, "Could not process detected file change, " + reason));
            return;
        }

        List<WatchedFile> allWatchedFiles = allExistingFilesEither.get();
        Set<WatchedFile> existingWatchedFilesSet = new HashSet<>(allWatchedFiles);
        PathMappingStrategy pathMappingStrategy = filePathInfo.getPathMappingStrategy();

        /*
         * Get all the possible paths from the event path trailing back up to the project root to ensure
         * AbstractFileCollectors can scan the file system all the way down to the active directory.
         * CurrentToBackupPathMapping ensures the corresponding mirrored backup path is created for the DeletedFileCollector.
         *
         * Path deletion needs to be handled differently because the file or directory no longer exists on the file system!
         * This means the path cannot be determined if its a file or directory since both return false.
         *
         * Files.isDirectory(path)   = false
         * Files.isRegularFile(path) = false
         *
         * Since all information is lost, the only way we can tell the DeletedFileCollector to remove the contents
         * at the given Path is to keep the full path and not perform any mapping through pathMappingStrategy.map(path).
         * pathMappingStrategy.map(path) relies upon Files.isDirectory to work correctly but in the delete case we know
         * it doesn't actually tell us if the path is a directory.
         *
         * If ENTRY_DELETE wasn't checked for this example highlights the issue.
         * 1. Delete path = C:\Users\me\Desktop\project\stuff
         * 2. pathMappingStrategy.map(path) => will return parent C:\Users\me\Desktop\project
         * 3. DeletedFileCollector will see C:\Users\me\Desktop\project as active, but when it goes deeper it will skip
         *    C:\Users\me\Desktop\project\stuff since it was not marked active which never gives the DeletedFileCollector
         *    a chance to actually compare this file to see if it exists on the current drive.
         */
        Path actualPath = eventKind == ENTRY_DELETE ? path : pathMappingStrategy.map(path);
        Set<WatchedFile> allPathsSet = transformToActivePaths(filePathInfo.getAllPaths(actualPath));

        /*
         * But if this path is a directory, we need to add all the new sub directories as being active, otherwise the
         * AbstractFileCollectors wont pick these up as being active and therefore will skip the subtree...
         * The result can be added to the existing set of paths. Since a set is used, duplicates are gone for free as
         * there will be 2 copies of the path argument to this method which is fine. WatchedFile implements equals
         * and hashCode based on the path strings hashCode.
         *
         * Note: This is only used when ENTRY_CREATE, ENTRY_MODIFY. ENTRY_DELETE will always return a false isDirectory
         * check which is why the full path to the deleted file was already stored so no information is lost.
         */
        List<Path> childPaths = new ArrayList<>();
        if (Files.isDirectory(path)) {
            try {
                DirectoryPathFinder directoryPathFinder = new DirectoryPathFinder();
                Files.walkFileTree(path, directoryPathFinder);
                childPaths = directoryPathFinder.directories;
            } catch (IOException e) {
                sendLogMessage(new LogMessage(Level.SEVERE, "Unable to read directory contents for " + surroundInQuotes(path.toString())));
                return;
            }
        }

        allPathsSet.addAll(transformToActivePaths(childPaths));

        // This is to add non existing active directories to the database by using set difference.
        allPathsSet.removeAll(existingWatchedFilesSet);

        if (FileBackupRepository.saveAll(allPathsSet)) {
            sendLogMessage(new LogMessage(Level.INFO, "File modification activity detected for " + surroundInQuotes(path.toString())));
        } else {
            sendLogMessage(new LogMessage(Level.SEVERE, "Unable to save modified file activity for " + surroundInQuotes(path.toString())));
        }
    }

    /**
     * Given a list of {@code Path}s, convert to a {@code Set} of {@code WatchedFile}s by mapping the current path
     * to its corresponding representation on the backup drive.
     *
     * @param paths The {@code Path}s to transform.
     * @return The {@code Set} of {@code WatchedFile}s.
     */
    private Set<WatchedFile> transformToActivePaths(List<Path> paths) {
        return paths.stream()
                .map(path -> new CurrentToBackupPathMapping(path, filePathInfo))
                .map(CurrentToBackupPathMapping::getMappedFiles)
                .flatMap(List::stream)
                .collect(Collectors.toSet());
    }

    /**
     * Collects all the directory names from the provided starting {@code Path}.
     */
    private class DirectoryPathFinder extends SimpleFileVisitor<Path> {

        private List<Path> directories;

        private DirectoryPathFinder() {
            this.directories = new ArrayList<>();
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            super.preVisitDirectory(dir, attrs);
            directories.add(dir);
            return FileVisitResult.CONTINUE;
        }
    }
}
