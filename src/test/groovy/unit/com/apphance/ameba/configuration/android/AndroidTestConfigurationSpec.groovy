package com.apphance.ameba.configuration.android

import com.apphance.ameba.detection.ProjectTypeDetector
import com.apphance.ameba.plugins.android.AndroidBuildXmlHelper
import com.apphance.ameba.plugins.android.AndroidManifestHelper
import com.google.common.io.Files
import org.gradle.api.Project
import spock.lang.Specification

import static com.apphance.ameba.detection.ProjectType.ANDROID
import static com.apphance.ameba.detection.ProjectType.IOS

class AndroidTestConfigurationSpec extends Specification {

    def 'android test configuration is enabled based on project type and internal field'() {
        given:
        def p = Mock(Project)

        and:
        def ptd = Mock(ProjectTypeDetector)

        when:
        ptd.detectProjectType(_) >> type
        def ac = new AndroidConfiguration(p, * [null] * 3, ptd)
        def atc = new AndroidTestConfiguration(p, ac, * [null] * 2)
        atc.enabled = internalField

        then:
        atc.isEnabled() == enabled

        where:
        enabled | type    | internalField
        false   | IOS     | true
        false   | IOS     | false
        true    | ANDROID | true
        false   | ANDROID | false
    }

    def 'emulator port is found well'() {
        given:
        def atc = new AndroidTestConfiguration(* [null] * 4)

        expect:
        atc.emulatorPort
        atc.emulatorPort > 0
    }

    def 'testProjectPackage & testProjectName are set well'() {
        given:
        def p = Mock(Project)

        and:
        def ptd = Mock(ProjectTypeDetector) {
            detectProjectType(_) >> ANDROID
        }

        and:
        def ac = new AndroidConfiguration(p, * [null] * 3, ptd)

        and:
        def amh = Mock(AndroidManifestHelper)
        amh.androidPackage(_) >> 'androidPackage'

        and:
        def abxh = Mock(AndroidBuildXmlHelper)
        abxh.projectName(_) >> 'androidName'

        and:
        def atc = new AndroidTestConfiguration(p, ac, amh, abxh)
        atc.enabled = true

        when:
        atc.testDir.value = dir

        then:
        atc.testProjectName == projectName
        atc.testProjectPackage == packageName

        where:
        dir                                | projectName   | packageName
        'bolo'                             | null          | null
        Files.createTempDir().absolutePath | 'androidName' | 'androidPackage'
    }
}