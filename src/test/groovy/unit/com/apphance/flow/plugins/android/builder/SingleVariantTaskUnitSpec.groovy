package com.apphance.flow.plugins.android.builder

import com.apphance.flow.TestUtils
import com.apphance.flow.configuration.android.AndroidBuildMode
import com.apphance.flow.configuration.android.AndroidConfiguration
import com.apphance.flow.configuration.android.AndroidReleaseConfiguration
import com.apphance.flow.configuration.android.variants.AndroidVariantConfiguration
import com.apphance.flow.configuration.properties.BooleanProperty
import com.apphance.flow.configuration.properties.StringProperty
import com.apphance.flow.executor.AndroidExecutor
import com.apphance.flow.executor.AntExecutor
import com.apphance.flow.plugins.android.buildplugin.tasks.AndroidProjectUpdater
import com.apphance.flow.plugins.android.buildplugin.tasks.SingleVariantTask
import com.apphance.flow.plugins.release.FlowArtifact
import com.apphance.flow.util.FlowUtils
import org.gradle.api.AntBuilder as AntBuilder
import org.gradle.api.GradleException
import spock.lang.Specification

import static com.apphance.flow.executor.AntExecutor.CLEAN
import static com.google.common.io.Files.createTempDir

@Mixin([TestUtils, FlowUtils])
class SingleVariantTaskUnitSpec extends Specification {

    def task = create SingleVariantTask
    def tmpDir = createTempDir()
    def variantDir = temporaryDir
    AndroidBuilderInfo builderInfo

    def setup() {
        builderInfo = GroovyStub(AndroidBuilderInfo) {
            getTmpDir() >> tmpDir
            getMode() >> AndroidBuildMode.DEBUG
            getOriginalFile() >> getTempFile()
        }
        with(task) {
            artifactProvider = GroovyStub(AndroidArtifactProvider) {
                builderInfo(_) >> builderInfo
                artifact(_) >> GroovyStub(FlowArtifact) {
                    getLocation() >> getTempFile()
                }
            }

            projectUpdater = GroovySpy(AndroidProjectUpdater)
            conf = GroovyStub(AndroidConfiguration) {
                getTarget() >> new StringProperty(value: 'android-8')
                getProjectName() >> new StringProperty(value: 'TestAndroidProject')
            }
            projectUpdater.executor = GroovyMock(AndroidExecutor)
            releaseConf = GroovyStub(AndroidReleaseConfiguration)
            variant = GroovyStub(AndroidVariantConfiguration) {
                getTmpDir() >> variantDir
                getOldPackage() >> new StringProperty()
                getNewPackage() >> new StringProperty()
                getMergeManifest() >> new BooleanProperty(value: 'true')
            }

            ant = GroovyMock(AntBuilder)
            antExecutor = GroovyMock(AntExecutor)
        }
    }

    def 'test ant executor tasks'() {
        given:
        task.releaseConf.enabled >> false

        when:
        task.singleVariant()

        then:
        with(task) {
            1 * antExecutor.executeTarget(tmpDir, CLEAN)
            1 * antExecutor.executeTarget(tmpDir, 'debug')
            1 * projectUpdater.updateRecursively(variantDir, 'android-8', 'TestAndroidProject')
            0 * antExecutor.executeTarget(_, _)
            0 * ant.copy(_)
        }
        1 * task.projectUpdater.executor.updateProject(variantDir, 'android-8', 'TestAndroidProject')
    }

    def 'test override files from variant dir'() {
        given: 'variant has its directory'
        builderInfo.variantDir >> createTempDir()
        task.releaseConf.enabled >> false

        when:
        task.singleVariant()

        then:
        1 * task.ant.copy(* _)
    }

    def 'test copy to ota'() {
        given: 'variant has its directory'
        task.releaseConf.enabled >> true

        when:
        task.singleVariant()

        then:
        1 * task.ant.copy(* _)
    }

    def 'test manifest merge'() {
        given:
        def main = tempFile << new File('src/test/resources/com/apphance/flow/android/AndroidManifestToMergeMain.xml').text
        def variantA = new File('src/test/resources/com/apphance/flow/android/AndroidManifestToMergeVariantA.xml')
        def variantB = new File('src/test/resources/com/apphance/flow/android/AndroidManifestToMergeVariantB.xml')

        expect:
        main.exists() && variantA.exists() && variantB.exists()
        permissions(main) == []
        permissions(variantA) == ['android.permission.INTERNET']
        permissions(variantB) == ['android.permission.READ_CALENDAR']

        when:
        task.mergeManifest(main, main, variantA, variantB)

        then:
        permissions(main) == ['android.permission.INTERNET', 'android.permission.READ_CALENDAR']
    }

    def 'test manifest merge throws exception'() {
        given:
        def main = tempFile << new File('src/test/resources/com/apphance/flow/android/AndroidManifestToMergeMain.xml').text
        def incorrectManifest = tempFile << new File('src/test/resources/com/apphance/flow/android/AndroidManifestToMergeVariantA.xml').
                text.replace('android:minSdkVersion="7"', 'android:minSdkVersion="1000"')

        expect:
        main.exists() && incorrectManifest.exists()
        permissions(main) == []
        permissions(incorrectManifest) == ['android.permission.INTERNET']

        when:
        task.mergeManifest(main, main, incorrectManifest)

        then:
        GradleException ex = thrown()
        ex.message == 'Error during merging manifests.'
    }

    List<String> permissions(File manifest) {
        new XmlSlurper().parse(manifest).'uses-permission'.@'android:name'*.text()
    }
}
