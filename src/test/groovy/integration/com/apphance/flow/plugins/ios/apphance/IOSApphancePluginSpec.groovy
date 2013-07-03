package com.apphance.flow.plugins.ios.apphance

import com.apphance.flow.configuration.apphance.ApphanceConfiguration
import com.apphance.flow.configuration.ios.variants.AbstractIOSVariant
import com.apphance.flow.configuration.ios.variants.IOSSchemeVariant
import com.apphance.flow.configuration.ios.variants.IOSVariantsConfiguration
import com.apphance.flow.configuration.properties.ApphanceModeProperty
import com.apphance.flow.configuration.properties.IOSBuildModeProperty
import com.apphance.flow.plugins.ios.buildplugin.IOSSingleVariantBuilder
import com.apphance.flow.plugins.ios.release.IOSReleaseListener
import spock.lang.Specification

import static com.apphance.flow.configuration.apphance.ApphanceMode.QA
import static com.apphance.flow.configuration.ios.IOSBuildMode.DEVICE
import static org.gradle.testfixtures.ProjectBuilder.builder

class IOSApphancePluginSpec extends Specification {

    def "no tasks added when no buildable variants exist"() {
        given:
        def project = builder().build()

        when:
        def plugin = new IOSApphancePlugin()
        plugin.apphanceConf = GroovyStub(ApphanceConfiguration) { isEnabled() >> true }
        plugin.variantsConf = GroovyStub(IOSVariantsConfiguration) { getVariants() >> [] }
        plugin.builder = new IOSSingleVariantBuilder()
        plugin.listener = new IOSReleaseListener()
        plugin.apply(project)

        then: 'no build & upload tasks added'
        !project.tasks.any { it.name ==~ '(upload|build)-' }

        then:
        plugin.builder.buildListeners.size() > 0
    }

    def 'no tasks added when configuration disabled'() {
        given:
        def project = builder().build()

        when:
        def plugin = new IOSApphancePlugin()
        plugin.apphanceConf = GroovyMock(ApphanceConfiguration)
        plugin.apphanceConf.enabled >> false
        plugin.builder = new IOSSingleVariantBuilder()
        plugin.listener = new IOSReleaseListener()

        def v1 = new IOSSchemeVariant('id1')
        v1.apphanceMode.value = QA.toString()
        plugin.variantsConf = GroovyStub(IOSVariantsConfiguration) { getVariants() >> [v1] }
        plugin.apply(project)

        then: 'apphance configuration is added'
        !project.configurations.contains('apphance')

        then: 'no build & upload tasks added'
        !project.tasks.any { it.name ==~ '(upload|build)-' }

        then:
        plugin.builder.buildListeners.size() == 0
    }

    def "plugin tasks' graph configured correctly when buildable variants exists"() {
        given:
        def project = builder().build()

        and: 'add fake tasks'
        project.task('buildid1')
        project.task('buildid2')

        when:
        def plugin = new IOSApphancePlugin()
        plugin.apphanceConf = GroovyStub(ApphanceConfiguration) { isEnabled() >> true }
        plugin.builder = new IOSSingleVariantBuilder()
        plugin.listener = new IOSReleaseListener()
        plugin.variantsConf = GroovyStub(IOSVariantsConfiguration) {
            getVariants() >> [
                    GroovyMock(AbstractIOSVariant) {
                        getName() >> 'id1'
                        getTarget() >> 't1'
                        getConfiguration() >> 'c1'
                        getApphanceMode() >> new ApphanceModeProperty(value: QA)
                        getBuildTaskName() >> 'buildid1'
                        getUploadTaskName() >> 'uploadid1'
                        getMode() >> new IOSBuildModeProperty(value: DEVICE)
                    },
                    GroovyMock(AbstractIOSVariant) {
                        getName() >> 'id2'
                        getTarget() >> 't2'
                        getConfiguration() >> 'c2'
                        getApphanceMode() >> new ApphanceModeProperty(value: QA)
                        getBuildTaskName() >> 'buildid2'
                        getUploadTaskName() >> 'uploadid2'
                        getMode() >> new IOSBuildModeProperty(value: DEVICE)
                    }]
        }
        plugin.apply(project)

        then: 'tasks for buildable variants added'
        project.tasks['uploadid1']
        project.tasks['uploadid2']

        then: 'tasks also have actions declared'
        project.tasks['buildid1'].actions
        project.tasks['buildid2'].actions
        project.tasks['uploadid1'].actions
        project.tasks['uploadid2'].actions

        then:
        plugin.builder.buildListeners.size() > 0
    }
}
