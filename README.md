# File Backup

[![Youtube demo](https://github.com/mjstewart/file-backup/blob/master/src/main/resources/demo-vid-thumb.png)](https://www.youtube.com/watch?v=SH6Xg-AH67I&feature=youtu.be "Youtube demo")

Given a master directory containing 'active working files', this application allows many
slave directories to remain in sync with the master through 2 modes of backup.

## Why?
When dealing with 50k+ files containing over 500+gb of data, using a version control system such as git is not
possible. The main motivation is to provide an alternative to the slow and unreliable windows backup.

The particular use case this application was developed for was to backup 50k+ files. 
The backup takes 1 minute on an i5 processor whereas windows could take up to 3 hours.

## Disclaimer
As with any backup software, use at your own risk and ensure you take precautions. I will not be 
held responsible for data loss, see LICENSE.

## Prerequisites
Java 8

## Installation
`mvn package`

The target folder contains the built jars
* If on windows a .exe is created to run the application which can be copied and pasted onto the desktop.
* To run via CLI, `java -jar file-backup-1.0-jar-with-dependencies.jar`

## Configuration
Repetitive backup tasks can be setup in the json configuration file located in the user home directory
`~/.filebackup/backup-tasks.json`

All symbolic links in `currentWorkingDirectory` will be followed if `followSymlinks` is set to `true`. 

**Important** Windows paths must be escaped with 2 `\\`

```$json
{
  "tasks": [
    {
      "description": "Nightly backup following symbolic links",
      "currentWorkingDirectory": "C:\\Users\\me\\Desktop\\important-backup",
      "backupDirectory": "F:\\important-backup",
      "followSymlinks": true
    },
    {
      "description": "Daily backup using primary usb drive",
      "currentWorkingDirectory": "C:\\Users\\me\\Desktop\\important-backup",
      "backupDirectory": "E:\\important-backup",
      "followSymlinks": false
    }
  ]
}
```

## Constraints
Top level root directories such as `C:\` are not allowed for safety reasons. Copying an entire 
operating system directory is not the aim of this application.

Paths must have the same ending sub directory in common such as *important-backup* to give the backup tool a common starting point.

`C:\Users\me\Desktop\important-backup`

`F:\important-backup` 

The path setup input view will provide feedback on why a path is invalid.

## Setup
Given a current working path of `C:\Users\me\Desktop\important-backup`, copy the entire `important-backup`
directory onto the backup drive `F:\important-backup`. This results in both directories being identical providing
the backup tool a consistent starting point.
 
The minimum requirement is for `F:\important-backup` to exist. If it is empty, 
the application will copy over all files within `C:\Users\me\Desktop\important-backup` to create
an exact clone.

#### Manual backup
Scans all files in both master and slave directory looking for modified, new and deleted files. 
A file is deemed modified if the current working and backup file differ in last modified time.

#### Live monitoring
Rather than perform a full directory scan, live monitoring keeps track of only the files that
have changed resulting in significant performance gains on selected platforms. Linux is the best
platform as there are subtle issues relating to windows (See Issues).

All file changes are persisted to an embedded h2 database allowing a previous session to be resumed in cases of power 
failures. This avoids losing file changes that would require having to run a full manual backup.

**Important** Once live monitoring is stopped a backup should be executed. If files are changed without performing
a backup, the next live monitoring session will not have detected any intermediate changes in which case a manual
backup must be run to sync up the master and slave(s).
    
### Issues

* WatchService https://bugs.openjdk.java.net/browse/JDK-6972833. The windows implementation of the WatchService locks directories from being modified, therefore
do not use Live monitoring on windows if you need to delete, rename or move directories.

* Windows network drives cannot be registered to the watch service. 

* Mac - Avoid running live monitoring on large directories since JDK 7/8 does not yet have a native implementation
 of the WatchService. This results in the entire file system is polled periodically. *This may
 have changed in the future.

* Linux/Mac - Alert dialog boxes cut off text so not all information is visible 
https://bugs.openjdk.java.net/browse/JDK-8087981


### License
This project is licensed under the MIT License - see the LICENSE.md file for details

### Built With
* [vavr](https://github.com/vavr-io)
* java8
* javafx
* hibernate
* h2 db
* jackson
* [WatchService](https://docs.oracle.com/javase/7/docs/api/java/nio/file/WatchService.html)

### FAQ

* How can I create a backup for many directories that do not have a common root?

The easiest thing to do is create a common root directory containing symbolic links to all directories
you wish to backup and set `followSymLinks: true`. The backup drive must follow the same directory structure.

* How can I resolve file permission errors?

This backup tool assumes read and write access is available to all files. If this is not the case
you will need to change the files in windows by 'right click file properties' or on linux do a `chmod`.
 
