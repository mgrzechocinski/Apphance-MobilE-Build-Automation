package com.apphance.flow.plugins.ios.apphance

import com.apphance.flow.configuration.apphance.ApphanceConfiguration
import com.apphance.flow.configuration.ios.variants.IOSVariantsConfiguration
import com.apphance.flow.plugins.ios.apphance.tasks.IOSApphanceUploadTask
import com.google.inject.Inject
import org.gradle.api.Plugin
import org.gradle.api.Project

import static com.apphance.flow.configuration.apphance.ApphanceMode.*
import static com.apphance.flow.configuration.ios.IOSBuildMode.DEVICE
import static org.gradle.api.logging.Logging.getLogger

/**
 * Plugin for all apphance-relate IOS tasks.
 *
 * This plugins provides automated adding of Apphance libraries to the project
 */
class IOSApphancePlugin implements Plugin<Project> {

    private logger = getLogger(getClass())

    @Inject ApphanceConfiguration apphanceConf
    @Inject IOSVariantsConfiguration variantsConf
    @Inject IOSApphanceEnhancerFactory iosApphanceEnhancerFactory

    @Override
    void apply(Project project) {
        if (apphanceConf.enabled) {
            logger.lifecycle("Applying plugin ${getClass().simpleName}")

            variantsConf.variants.each { variant ->

                if (variant.apphanceMode.value in [QA, PROD, SILENT] && variant.mode.value == DEVICE) {
                    def enhance = { iosApphanceEnhancerFactory.create(variant).enhanceApphance() }

                    project.tasks[variant.buildTaskName].doFirst(enhance)
                    project.tasks[variant.archiveTaskName].doFirst(enhance)

                    def uploadTask =
                        project.task(variant.uploadTaskName,
                                type: IOSApphanceUploadTask,
                                dependsOn: variant.buildTaskName) as IOSApphanceUploadTask
                    uploadTask.variant = variant
                } else {
                    logger.info("Apphance is disabled for variant '$variant.name'")
                }
            }
        }
    }
}
