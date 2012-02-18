package com.apphance.ameba.plugins.projectconfiguration;




import java.io.BufferedReader

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.logging.LogLevel
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.logging.StyledTextOutput
import org.gradle.logging.StyledTextOutputFactory
import org.gradle.logging.StyledTextOutput.Style

import com.apphance.ameba.AbstractPrepareSetupTask
import com.apphance.ameba.AmebaArtifact
import com.apphance.ameba.AmebaCommonBuildTaskGroups
import com.apphance.ameba.ProjectConfiguration
import com.apphance.ameba.ProjectHelper
import com.apphance.ameba.PropertyCategory


/**
 * Plugin for Mercurial implementation of VCS system
 *
 */
class ProjectConfigurationPlugin implements Plugin<Project> {

    static Logger logger = Logging.getLogger(ProjectConfigurationPlugin.class)

    static final String PROJECT_NAME_PROPERTY = 'project.name'

    ProjectHelper projectHelper
    ProjectConfiguration conf

    void apply(Project project) {
        projectHelper = new ProjectHelper()
        prepareRepositories(project)
        prepareVerifySetupTask(project)
        readProjectConfigurationTask(project)
        preparePrepareSetupTask(project)
        project.task('checkTests', type: CheckTestsTask.class)
        showProjectConfigurationTask(project)
        prepareCleanConfigurationTask(project)
        prepareShowPropertiesTask(project)
        prepareCopyGalleryFilesTask(project)
        project.task('verifyBaseSetup', type: VerifyBaseSetupTask.class)
        project.task('prepareBaseSetup', type: PrepareBaseSetupTask.class)
        project.task('showBaseSetup', type: ShowBaseSetupTask.class)
    }

    void prepareRepositories(Project project) {
        project.repositories.mavenCentral()
    }

    void prepareVerifySetupTask(Project project) {
        def task = project.task('verifySetup')
        task.description = "Verifies if the project can be build properly"
        task.group = AmebaCommonBuildTaskGroups.AMEBA_SETUP
        task << {
            // this task does nothing. It is there to serve as umbrella task for other setup tasks
        }
    }

    def void preparePrepareSetupTask(Project project) {
        def task = project.task('prepareSetup')
        task.description = "Walk-through wizard for preparing project's configuration"
        task.group = AmebaCommonBuildTaskGroups.AMEBA_SETUP
        project.logging.setLevel(LogLevel.QUIET)
        task.logging.setLevel(LogLevel.QUIET)
        task << {
            use(PropertyCategory) {
                String propertiesToWrite = project.readProperty(AbstractPrepareSetupTask.GENERATED_GRADLE_PROPERTIES,'')
                StyledTextOutput o = task.services.get(StyledTextOutputFactory).create(task.class)
                o.withStyle(Style.Normal).println("About to write new properties to gradle.properties:")
                propertiesToWrite.split('\n').each {
                    if (it.startsWith('#')) {
                        o.withStyle(Style.Info).println(it)
                    } else {
                        o.withStyle(Style.Identifier).println(it)
                    }
                }
                o.withStyle(Style.Normal).println("Are you sure y/n?")
                BufferedReader br = AbstractPrepareSetupTask.getReader()
                File f = new File(project.rootDir,'gradle.properties')
                String answer = ''
                while (!(answer in ['y', 'n'])) {
                    answer = br.readLine()
                }
                if (answer == 'y') {
                    f.delete()
                    f << propertiesToWrite
                    System.out.println("File written: ${f}")
                } else {
                    System.out.println("Skipped writing to file: ${f}")
                }
            }
        }
    }

    void prepareShowPropertiesTask(Project project) {
        def task = project.task('showSetup')
        task.description = "Shows all available project properties"
        task.group = AmebaCommonBuildTaskGroups.AMEBA_SETUP
        task << {
            // this task does nothing. It is there to serve as umbrella task for other setup tasks
        }
    }
    def void readProjectConfigurationTask(Project project) {
        def task = project.task('readProjectConfiguration')
        task.description = "Reads project's configuration and sets it up in projectConfiguration property of project"
        task.group = AmebaCommonBuildTaskGroups.AMEBA_CONFIGURATION
        task << {
            use (PropertyCategory) {
                this.conf = project.getProjectConfiguration()
                // NOTE! conf.versionString and conf.versionCode need to
                // be read before project configuration task -> task reading the version
                // should be injected here
                project.retrieveBasicProjectData()
                prepareGeneratedDirectories(project)
            }
        }
    }

    private prepareGeneratedDirectories(Project project) {
        conf.otaDirectory = new File(project.rootDir,"ota/")
        conf.tmpDirectory = new File(project.rootDir,"tmp/")
    }

    def void showProjectConfigurationTask(Project project) {
        def task = project.task('showProjectConfiguration')
        task.group = AmebaCommonBuildTaskGroups.AMEBA_CONFIGURATION
        task.description = "Shows project's configuration"
        task << {
            logger.lifecycle( "Configuration: " + project[PropertyCategory.PROJECT_CONFIGURATION_KEY])
        }
        task.dependsOn(project.readProjectConfiguration)
    }

    def void prepareCleanConfigurationTask(Project project) {
        def task = project.task('cleanConfiguration')
        task.description = "Cleans configuration before each build"
        task.group = AmebaCommonBuildTaskGroups.AMEBA_CONFIGURATION
        task << {
            conf.buildDirectory.deleteDir()
            conf.tmpDirectory.deleteDir()
            conf.logDirectory.deleteDir()
            conf.buildDirectory.mkdirs()
            conf.logDirectory.mkdirs()
            conf.tmpDirectory.mkdirs()
        }
        task.dependsOn(project.readProjectConfiguration)
    }

    private prepareGalleryArtifacts() {
        conf.galleryCss = new AmebaArtifact(
                name : "CSS Gallery",
                url : new URL(conf.versionedApplicationUrl, "_css/jquery.swipegallery.css"),
                location : new File(conf.targetDirectory, "_css/jquery.swipegallery.css"))
        conf.galleryJs = new AmebaArtifact(
                name : "JS Gallery",
                url : new URL(conf.versionedApplicationUrl, "_res/jquery.swipegallery.js"),
                location : new File(conf.targetDirectory, "_res/jquery.swipegallery.js"))
        conf.galleryTrans = new AmebaArtifact(
                name : "JS Gallery",
                url : new URL(conf.versionedApplicationUrl, "_res/trans.png"),
                location : new File(conf.targetDirectory, "_res/trans.png"))
    }

    def void prepareCopyGalleryFilesTask(Project project) {
        def task = project.task('copyGalleryFiles')
        task.group = AmebaCommonBuildTaskGroups.AMEBA_CONFIGURATION
        task.description = "Copy files required by swipe jquerymobile gallery"
        task << {
            prepareGalleryArtifacts()
            conf.galleryCss.location.parentFile.mkdirs()
            conf.galleryJs.location.parentFile.mkdirs()
            conf.galleryCss.location.setText(this.class.getResourceAsStream("swipegallery/_css/jquery.swipegallery.css").text,"utf-8")
            conf.galleryJs.location.setText(this.class.getResourceAsStream("swipegallery/_res/jquery.swipegallery.js").text,"utf-8")
            conf.galleryTrans.location.setText(this.class.getResourceAsStream("swipegallery/_res/trans.png").text,"utf-8")
        }
        task.dependsOn(project.readProjectConfiguration)
    }
}