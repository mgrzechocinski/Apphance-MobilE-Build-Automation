package com.apphance.ameba.plugins.android.analysis

import org.gradle.api.plugins.JavaPlugin
import spock.lang.Specification

import static com.apphance.ameba.plugins.AmebaCommonBuildTaskGroups.AMEBA_ANALYSIS
import static com.apphance.ameba.plugins.android.analysis.AndroidAnalysisPlugin.*
import static org.gradle.api.plugins.JavaPlugin.CLASSES_TASK_NAME
import static org.gradle.testfixtures.ProjectBuilder.builder

class AndroidAnalysisPluginSpec extends Specification {

    def "plugin tasks' graph configured correctly"() {
        given:
        def project = builder().build()

        when:
        project.plugins.apply(JavaPlugin)
        project.plugins.apply(AndroidAnalysisPlugin)

        then: 'analysis convention is added'
        project.convention.plugins['androidAnalysis']

        then: 'every single task is in correct group'
        project.tasks[PMD_TASK_NAME].group == AMEBA_ANALYSIS
        project.tasks[CPD_TASK_NAME].group == AMEBA_ANALYSIS
        project.tasks[FINDBUGS_TASK_NAME].group == AMEBA_ANALYSIS
        project.tasks[CHECKSTYLE_TASK_NAME].group == AMEBA_ANALYSIS
        project.tasks[ANALYSIS_TASK_NAME].group == AMEBA_ANALYSIS

        then: 'configurations for tasks were added properly'
        project.configurations.pmdConf
        project.configurations.findbugsConf
        project.configurations.checkstyleConf

        then: 'external dependencies configured correctly'
        project.dependencies.configurationContainer.pmdConf.dependencies
        project.dependencies.configurationContainer.pmdConf.dependencies.find {
            it.group == 'pmd' && it.name == 'pmd' && it.version == '4.3'
        }

        project.dependencies.configurationContainer.findbugsConf.dependencies
        project.dependencies.configurationContainer.findbugsConf.dependencies.find {
            it.group == 'com.google.code.findbugs' && it.name == 'findbugs' && it.version == '2.0.1'
        }
        project.dependencies.configurationContainer.findbugsConf.dependencies.find {
            it.group == 'com.google.code.findbugs' && it.name == 'findbugs-ant' && it.version == '2.0.1'
        }

        project.dependencies.configurationContainer.checkstyleConf.dependencies
        project.dependencies.configurationContainer.checkstyleConf.dependencies.find {
            it.group == 'com.puppycrawl.tools' && it.name == 'checkstyle' && it.version == '5.6'
        }

        and: 'task dependencies configured correctly'
        project.tasks[ANALYSIS_TASK_NAME].dependsOn.containsAll(FINDBUGS_TASK_NAME,
                CPD_TASK_NAME,
                PMD_TASK_NAME,
                CHECKSTYLE_TASK_NAME)
        project.tasks[FINDBUGS_TASK_NAME].dependsOn.contains(CLASSES_TASK_NAME)
        project.tasks[CHECKSTYLE_TASK_NAME].dependsOn.contains(CLASSES_TASK_NAME)
    }
}