package com.apphance.flow.util.file

import groovy.io.FileType
import org.gradle.api.Project

import java.nio.file.Paths

import static com.apphance.flow.configuration.ProjectConfiguration.BUILD_DIR
import static com.apphance.flow.configuration.ProjectConfiguration.TMP_DIR
import static com.apphance.flow.configuration.release.ReleaseConfiguration.OTA_DIR
import static groovy.io.FileType.DIRECTORIES
import static groovy.io.FileType.FILES
import static java.lang.String.format

class FileManager {
    public static final EXCLUDE_FILTER = ~/.*(${TMP_DIR}|${OTA_DIR}|${BUILD_DIR}|\.git|\.hg).*/
    public static final int MAX_RECURSION_LEVEL = 7
    private static final long MEGABYTE = 1048576L

    public static List getFiles(Project project, Closure filter) {
        return getFilesOrDirectories(project, FILES, filter)
    }

    public static List getDirectories(Project project, Closure filter) {
        return getFilesOrDirectories(project, DIRECTORIES, filter)
    }

    public static List getFilesOrDirectories(Project project, FileType type, Closure filter) {
        List paths = [
                project.file('bin').absolutePath,
                project.file('build').absolutePath,
                project.file('ota').absolutePath,
                project.file('tmp').absolutePath,
                project.file('.hg').absolutePath,
                project.file('.git').absolutePath,
        ]
        def plistFiles = []
        project.rootDir.traverse([type: type, maxDepth: MAX_RECURSION_LEVEL]) {
            def thePath = it.absolutePath
            if (filter(it)) {
                if (!paths.any { path -> thePath.startsWith(path) }) {
                    plistFiles << thePath.substring(project.rootDir.path.length() + 1)
                }
            }
        }
        return plistFiles
    }

    public static void removeMissingSymlinks(File baseDirectory) {
        baseDirectory.traverse([type: FILES, maxDepth: MAX_RECURSION_LEVEL]) {
            if (!it.isDirectory()) {
                File canonicalFile = it.getCanonicalFile()
                if (!canonicalFile.exists()) {
                    it.delete()
                }
            }
        }
    }

    public static getHumanReadableSize(long byteSize) {
        byteSize >= MEGABYTE ? format("%.2f", byteSize * 1.0 / 1024.0 / 1024.0) + " MB" : format("%.2f", byteSize * 1.0 / 1024.0) + " kB"
    }

    public static void findAllPackages(String currentPackage, File directory, currentPackageList) {
        boolean empty = true
        directory.eachFile(FILES, { empty = false })
        if (!empty) {
            currentPackageList << currentPackage
        }
        boolean rootDirectory = (currentPackage == '')
        directory.eachDir {
            findAllPackages(rootDirectory ? it.name : (currentPackage + '.' + it.name), it, currentPackageList)
        }
    }

    public static File relativeTo(String from, String to) {
        Paths.get(from).relativize(Paths.get(to)).toFile()
    }
}
