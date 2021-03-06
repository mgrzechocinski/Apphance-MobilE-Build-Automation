package com.apphance.flow.plugins.ios.buildplugin

import com.apphance.flow.configuration.ios.IOSConfiguration
import com.apphance.flow.configuration.ios.variants.IOSVariant
import com.apphance.flow.configuration.ios.variants.IOSVariantsConfiguration
import com.apphance.flow.configuration.properties.IOSBuildModeProperty
import com.apphance.flow.plugins.ios.buildplugin.tasks.CopyMobileProvisionTask
import com.apphance.flow.plugins.ios.buildplugin.tasks.CopySourcesTask
import com.apphance.flow.plugins.ios.buildplugin.tasks.UnlockKeyChainTask
import com.apphance.flow.plugins.project.tasks.CleanFlowTask
import spock.lang.Specification

import static com.apphance.flow.configuration.ios.IOSBuildMode.DEVICE
import static com.apphance.flow.configuration.ios.IOSBuildMode.SIMULATOR
import static com.apphance.flow.plugins.FlowTasksGroups.FLOW_BUILD
import static com.apphance.flow.plugins.ios.buildplugin.IOSPlugin.*
import static org.gradle.testfixtures.ProjectBuilder.builder

class IOSPluginSpec extends Specification {

    def 'tasks defined in plugin available when configuration is active'() {
        given:
        def project = builder().build()

        and:
        project.task(CleanFlowTask.NAME)

        and:
        def conf = GroovyMock(IOSConfiguration)
        conf.isEnabled() >> true

        and:
        def variantsConf = GroovyMock(IOSVariantsConfiguration)
        variantsConf.variants >> [
                GroovyMock(IOSVariant, {
                    getBuildTaskName() >> 'buildV1'
                    getMode() >> new IOSBuildModeProperty(value: DEVICE)
                    getArchiveTaskName() >> 'archiveV1'
                }
                ),
                GroovyMock(IOSVariant, {
                    getBuildTaskName() >> 'buildV2'
                    getMode() >> new IOSBuildModeProperty(value: SIMULATOR)
                    getArchiveTaskName() >> 'archiveV2'
                }),
        ]

        and:
        def plugin = new IOSPlugin()
        plugin.conf = conf
        plugin.variantsConf = variantsConf

        when:
        plugin.apply(project)

        then:
        project.tasks[CopySourcesTask.NAME].group == FLOW_BUILD.name()
        project.tasks[CopyMobileProvisionTask.NAME].group == FLOW_BUILD.name()
        project.tasks[UnlockKeyChainTask.NAME].group == FLOW_BUILD.name()
        project.tasks[BUILD_ALL_DEVICE_TASK_NAME].group == FLOW_BUILD.name()
        project.tasks[BUILD_ALL_SIMULATOR_TASK_NAME].group == FLOW_BUILD.name()
        project.tasks[BUILD_ALL_TASK_NAME].group == FLOW_BUILD.name()
        project.tasks[ARCHIVE_ALL_DEVICE_TASK_NAME].group == FLOW_BUILD.name()
        project.tasks[ARCHIVE_ALL_SIMULATOR_TASK_NAME].group == FLOW_BUILD.name()
        project.tasks[ARCHIVE_ALL_TASK_NAME].group == FLOW_BUILD.name()
        project.tasks['buildV1'].group == FLOW_BUILD.name()
        project.tasks['buildV2'].group == FLOW_BUILD.name()
        project.tasks['archiveV1'].group == FLOW_BUILD.name()
        project.tasks['archiveV2'].group == FLOW_BUILD.name()

        and:
        project.tasks[BUILD_ALL_TASK_NAME].dependsOn.flatten().containsAll(BUILD_ALL_SIMULATOR_TASK_NAME, BUILD_ALL_DEVICE_TASK_NAME)
        project.tasks[ARCHIVE_ALL_TASK_NAME].dependsOn.flatten().containsAll(ARCHIVE_ALL_SIMULATOR_TASK_NAME, ARCHIVE_ALL_DEVICE_TASK_NAME)
        project.tasks['buildV1'].dependsOn.flatten().contains(CopyMobileProvisionTask.NAME)
        project.tasks['buildV2'].dependsOn.flatten().contains(CopyMobileProvisionTask.NAME)
        project.tasks['archiveV1'].dependsOn.flatten().contains(CopyMobileProvisionTask.NAME)
        project.tasks['archiveV2'].dependsOn.flatten().contains(CopyMobileProvisionTask.NAME)
        project.tasks[BUILD_ALL_SIMULATOR_TASK_NAME].dependsOn.flatten().contains('buildV2')
        project.tasks[BUILD_ALL_DEVICE_TASK_NAME].dependsOn.flatten().contains('buildV1')
        project.tasks[ARCHIVE_ALL_SIMULATOR_TASK_NAME].dependsOn.flatten().contains('archiveV2')
        project.tasks[ARCHIVE_ALL_DEVICE_TASK_NAME].dependsOn.flatten().contains('archiveV1')
    }

    def 'no tasks available when configuration is inactive'() {
        given:
        def project = builder().build()

        and:
        def plugin = new IOSPlugin()

        and:
        plugin.conf = GroovyMock(IOSConfiguration) {
            isEnabled() >> false
        }

        when:
        plugin.apply(project)

        then:
        !project.getTasksByName(CopySourcesTask.NAME, false)
        !project.getTasksByName(UnlockKeyChainTask.NAME, false)
        !project.getTasksByName(CopyMobileProvisionTask.NAME, false)
        !project.getTasksByName(BUILD_ALL_TASK_NAME, false)
        !project.getTasksByName(BUILD_ALL_SIMULATOR_TASK_NAME, false)
        !project.getTasksByName(BUILD_ALL_DEVICE_TASK_NAME, false)
        !project.getTasksByName(ARCHIVE_ALL_TASK_NAME, false)
        !project.getTasksByName(ARCHIVE_ALL_SIMULATOR_TASK_NAME, false)
        !project.getTasksByName(ARCHIVE_ALL_DEVICE_TASK_NAME, false)
        !project.getTasksByName('buildV1', false)
        !project.getTasksByName('buildV2', false)
        !project.getTasksByName('buildV3', false)
        !project.getTasksByName('archiveV1', false)
        !project.getTasksByName('archiveV2', false)
        !project.getTasksByName('archiveV3', false)
    }
}
