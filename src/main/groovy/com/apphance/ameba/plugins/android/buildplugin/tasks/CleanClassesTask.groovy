package com.apphance.ameba.plugins.android.buildplugin.tasks

import org.gradle.api.Project

//TODO refactor/test
class CleanClassesTask {

    private Project project

    CleanClassesTask(Project project) {
        this.project = project
    }

    void cleanClasses() {
        project.ant.delete(dir: project.file('build'))
    }
}