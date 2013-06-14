package com.apphance.flow.plugins.android.apphance

import com.apphance.flow.configuration.android.variants.AndroidVariantConfiguration
import com.apphance.flow.configuration.android.variants.AndroidVariantsConfiguration
import com.apphance.flow.configuration.apphance.ApphanceConfiguration
import com.apphance.flow.plugins.android.apphance.tasks.AddApphance18ToAndroid
import com.apphance.flow.plugins.android.apphance.tasks.AndroidLogsConversionTask
import com.apphance.flow.plugins.android.apphance.tasks.ApphanceLogsConversionTask
import com.apphance.flow.plugins.android.apphance.tasks.UploadAndroidArtifactTask
import com.apphance.flow.plugins.apphance.ApphancePluginCommons
import com.apphance.flow.plugins.release.tasks.ImageMontageTask
import org.gradle.api.Plugin
import org.gradle.api.Project

import javax.inject.Inject

import static com.apphance.flow.configuration.android.AndroidBuildMode.DEBUG
import static com.apphance.flow.plugins.FlowTasksGroups.FLOW_APPHANCE_SERVICE
import static org.gradle.api.logging.Logging.getLogger

/**
 * Adds Apphance in automated way.
 *
 * This is the plugin that links Flow with Apphance service.
 *
 * The plugin provides integration with Apphance service. It performs the
 * following tasks: adding Apphance on-the-fly while building the application
 * (for all Debug builds), removing Apphance on-the-fly while building the application
 * (for all Release builds), submitting the application to apphance at release time.
 *
 */
//TODO this is class is still to be refactored after configuration for android is finished
//TODO Apphance Setup Task
@Mixin(ApphancePluginCommons)
class AndroidApphancePlugin implements Plugin<Project> {

    def log = getLogger(this.class)

    Project project
    @Inject
    AndroidVariantsConfiguration variantsConf
    @Inject
    ApphanceConfiguration apphanceConf
    @Inject
    AddApphance18ToAndroid addAndroidApphance

    @Override
    void apply(Project project) {
        if (apphanceConf.isEnabled()) {
            log.lifecycle("Applying plugin ${this.class.simpleName}")
            this.project = project

            addApphanceConfiguration(project)
            preProcessBuildsWithApphance()

            //TODO probably both to be removed
            def t1 = project.task(ApphanceLogsConversionTask.NAME,
                    group: FLOW_APPHANCE_SERVICE,
                    description: 'Converts all logs to apphance from android logs for the source project')
            t1 << {
                new ApphanceLogsConversionTask(project.ant).convertLogsToApphance(project.rootDir)
            }
            def t2 = project.task(AndroidLogsConversionTask.NAME,
                    group: FLOW_APPHANCE_SERVICE,
                    description: 'Converts all logs to android from apphance logs for the source project')
            t2 << {
                new AndroidLogsConversionTask(project.ant).convertLogsToAndroid(project.rootDir)
            }
        }
    }

    void preProcessBuildsWithApphance() {
        //TODO for each variant add apphance if it's enabled in variant conf
        variantsConf.variants.each { avc ->
            if (avc.mode == DEBUG) {
                log.lifecycle("Adding apphance task for ${avc.name}")
                def task = project.tasks.findByName("build${avc.name}")
                task?.doFirst {
                    addAndroidApphance.addApphance(avc)
                }
                prepareSingleBuildUploadTask(avc, task?.name)
            } else {
                log.lifecycle("Not adding apphance to ${avc.name} because it is not in debug mode")
            }
        }
    }

    private void prepareSingleBuildUploadTask(AndroidVariantConfiguration variant, String buildTaskName) {
        def task = project.task("upload${variant.name}", type: UploadAndroidArtifactTask) as UploadAndroidArtifactTask
        task.variant = variant
        task.dependsOn(buildTaskName)
        task.dependsOn(ImageMontageTask.NAME)
    }
}
