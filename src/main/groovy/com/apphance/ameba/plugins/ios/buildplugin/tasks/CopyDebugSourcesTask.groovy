package com.apphance.ameba.plugins.ios.buildplugin.tasks

import com.apphance.ameba.plugins.ios.IOSProjectConfiguration
import com.apphance.ameba.plugins.projectconfiguration.ProjectConfiguration
import org.gradle.api.Project

import static com.apphance.ameba.PropertyCategory.getProjectConfiguration
import static com.apphance.ameba.plugins.ios.buildplugin.IOSConfigurationRetriever.getIosProjectConfiguration

class CopyDebugSourcesTask {

    private Project project
    private ProjectConfiguration conf
    private IOSProjectConfiguration iosConf

    CopyDebugSourcesTask(Project project) {
        this.project = project
        this.conf = getProjectConfiguration(project)
        this.iosConf = getIosProjectConfiguration(project)
    }

    void copyDebugSources() {
        String debugConfiguration = 'Debug'
        iosConf.allTargets.each { target ->
            project.ant.sync(toDir: tmpDir(target, debugConfiguration),
                    failonerror: false, overwrite: true, verbose: false) {
                fileset(dir: "${project.rootDir}/") {
                    exclude(name: tmpDir(target, debugConfiguration).absolutePath + '/**/*')
                    conf.sourceExcludes.each { exclude(name: it) }
                }
            }
        }
    }

    private File tmpDir(String target, String configuration) {
        project.file("../tmp-${project.rootDir.name}-${target}-${configuration}")
    }
}
