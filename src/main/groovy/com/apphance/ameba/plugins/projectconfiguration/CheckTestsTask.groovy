package com.apphance.ameba.plugins.projectconfiguration

import com.apphance.ameba.AmebaCommonBuildTaskGroups
import com.apphance.ameba.ProjectConfiguration
import com.apphance.ameba.ProjectHelper
import com.apphance.ameba.PropertyCategory
import com.apphance.ameba.ios.IOSProjectConfiguration
import groovy.io.FileType
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.logging.Logging
import org.gradle.api.tasks.TaskAction
import org.slf4j.Logger

/**
 * Checks if all tests are ok.
 *
 */
class CheckTestsTask extends DefaultTask {
    Logger logger = Logging.getLogger(CheckTestsTask.class)
    ProjectHelper projectHelper
    ProjectConfiguration conf
    IOSProjectConfiguration iosConf

    CheckTestsTask() {
        use(PropertyCategory) {
            this.group = AmebaCommonBuildTaskGroups.AMEBA_TEST
            this.description = 'Checks if there are any failed junit test results in the project and fails if therea are'
            this.projectHelper = new ProjectHelper()
            this.conf = project.getProjectConfiguration()
            this.dependsOn(project.readProjectConfiguration)
        }
    }

    @TaskAction
    void checkTests() {
        List<String> failingTests = new ArrayList<String>();
        File testDir = project.rootDir
        testDir.traverse([type: FileType.FILES, maxDepth: ProjectHelper.MAX_RECURSION_LEVEL]) {
            String fileName = it.name
            if (fileName.matches("TEST.*\\.xml")) {
                Node testsuites = new XmlParser().parse(it)
                testsuites.testsuite.each() { testsuite ->
                    if (testsuite.attributes()["failures"] != "0") {
                        failingTests.add(testsuite.attributes()["name"])
                    }
                }
            }
        }
        if (!failingTests.isEmpty()) {
            throw new GradleException("build failed, since following tests failed: " + failingTests.toListString())
        }
    }
}
