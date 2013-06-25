package com.apphance.flow.configuration.ios.variants

import com.apphance.flow.configuration.apphance.ApphanceArtifactory
import com.apphance.flow.configuration.apphance.ApphanceConfiguration
import com.apphance.flow.configuration.properties.ApphanceModeProperty
import com.apphance.flow.configuration.properties.StringProperty
import com.apphance.flow.executor.IOSExecutor
import spock.lang.Specification
import spock.lang.Unroll

import static com.apphance.flow.configuration.apphance.ApphanceMode.*
import static com.apphance.flow.configuration.ios.IOSBuildMode.DEVICE
import static com.apphance.flow.configuration.ios.IOSBuildMode.SIMULATOR

class AbstractIOSVariantSpec extends Specification {

    @Unroll
    def 'apphance enabled depending for #mode'() {
        given:
        def conf = new IOSSchemeVariant('name')
        conf.mode.value = mode

        and:
        conf.apphanceConf = GroovyMock(ApphanceConfiguration) {
            getEnabled() >> true
        }

        expect:
        conf.apphanceEnabled == expected

        where:
        mode      | expected
        DEVICE    | true
        SIMULATOR | false
    }

    def 'apphance lib dependency is constructed correctly'() {
        given:
        def variant = GroovySpy(AbstractIOSVariant) {
            getApphanceMode() >> new ApphanceModeProperty(value: apphanceMode)
            getApphanceLibVersion() >> new StringProperty(value: '1.8.2')
            getTarget() >> 't'
            getConfiguration() >> 'c'
        }
        variant.executor = GroovyMock(IOSExecutor) {
            buildSettings(_, _) >> ['ARCHS': 'armv6 armv7']
        }
        variant.apphanceArtifactory = GroovyMock(ApphanceArtifactory) {
            iOSArchs(_) >> ['armv6', 'armv7']
        }

        expect:
        variant.apphanceDependencyArch() == expectedDependency

        where:
        apphanceMode | expectedDependency
        QA           | 'armv7'
        SILENT       | 'armv7'
        PROD         | 'armv7'
    }

}
