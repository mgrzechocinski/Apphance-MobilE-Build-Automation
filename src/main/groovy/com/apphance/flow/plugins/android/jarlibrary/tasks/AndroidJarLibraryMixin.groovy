package com.apphance.flow.plugins.android.jarlibrary.tasks

class AndroidJarLibraryMixin {

    String getJarLibraryFilePath(String mainProjectName, String versionString) {
        "bin/${mainProjectName}_${versionString}.jar".toString()
    }
}
