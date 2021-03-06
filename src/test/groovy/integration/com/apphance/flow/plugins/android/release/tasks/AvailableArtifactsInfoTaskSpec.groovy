package com.apphance.flow.plugins.android.release.tasks

import com.apphance.flow.configuration.android.AndroidConfiguration
import com.apphance.flow.configuration.android.AndroidReleaseConfiguration
import com.apphance.flow.configuration.android.variants.AndroidVariantConfiguration
import com.apphance.flow.configuration.android.variants.AndroidVariantsConfiguration
import com.apphance.flow.configuration.properties.FileProperty
import com.apphance.flow.configuration.properties.StringProperty
import com.apphance.flow.configuration.properties.URLProperty
import com.apphance.flow.configuration.reader.PropertyReader
import com.apphance.flow.plugins.android.builder.AndroidArtifactProvider
import com.apphance.flow.plugins.release.FlowArtifact
import org.gradle.api.Project
import spock.lang.Specification

import static com.apphance.flow.configuration.ProjectConfiguration.TMP_DIR
import static com.apphance.flow.configuration.android.AndroidBuildMode.DEBUG
import static com.apphance.flow.configuration.android.AndroidBuildMode.RELEASE
import static com.apphance.flow.configuration.release.ReleaseConfiguration.OTA_DIR
import static com.google.common.io.Files.createTempDir
import static org.gradle.testfixtures.ProjectBuilder.builder

class AvailableArtifactsInfoTaskSpec extends Specification {

    def rootDir = createTempDir()
    def apkDir = createTempDir()
    def tmpDir = createTempDir()

    def p = builder().withProjectDir(new File('testProjects/android/android-basic')).build()

    def projectName = 'TestAndroidProject'
    def projectUrl = "http://ota.polidea.pl/$projectName".toURL()
    def fullVersionString = '1.0.1_42'
    def mainVariant = 'MainVariant'

    def releaseConf = new AndroidReleaseConfiguration()
    def variantsConf

    def task = p.task(AvailableArtifactsInfoTask.NAME, type: AvailableArtifactsInfoTask) as AvailableArtifactsInfoTask

    def setup() {

        def reader = GroovyStub(PropertyReader) {
            systemProperty('version.code') >> '42'
            systemProperty('version.string') >> '1.0.1'
            envVariable('RELEASE_NOTES') >> 'release\nnotes'
        }

        def conf = GroovySpy(AndroidConfiguration) {
            isLibrary() >> false
            getFullVersionString() >> fullVersionString
            getVersionString() >> '1.0.1'
            getProjectName() >> new StringProperty(value: projectName)
        }
        conf.project = GroovyStub(Project) {
            getRootDir() >> rootDir
            file(TMP_DIR) >> tmpDir
        }
        conf.reader = reader

        releaseConf.conf = conf
        releaseConf.releaseUrl = new URLProperty(value: projectUrl)
        releaseConf.iconFile = new FileProperty(value: 'res/drawable-hdpi/icon.png')
        releaseConf.reader = reader

        variantsConf = GroovyMock(AndroidVariantsConfiguration)
        variantsConf.variants >> [
                GroovyMock(AndroidVariantConfiguration) {
                    getName() >> mainVariant
                    getMode() >> RELEASE
                },
                GroovyMock(AndroidVariantConfiguration) {
                    getName() >> 'Variant1'
                    getMode() >> DEBUG
                },
                GroovyMock(AndroidVariantConfiguration) {
                    getName() >> 'Variant2'
                    getMode() >> RELEASE
                }
        ]
        variantsConf.mainVariant >> mainVariant

        def artifactBuilder = new AndroidArtifactProvider(conf: conf, releaseConf: releaseConf)

        task.conf = conf
        task.releaseConf = releaseConf
        task.variantsConf = variantsConf
        task.artifactBuilder = artifactBuilder
    }

    def cleanup() {
        rootDir.deleteDir()
        apkDir.deleteDir()
        tmpDir.deleteDir()
    }

    def 'task action is executed and all artifacts are prepared'() {
        when:
        task.availableArtifactsInfo()

        then:
        def releaseDir = new File(rootDir.absolutePath, "${OTA_DIR}/$projectName/$fullVersionString")
        releaseDir.exists()
        releaseDir.isDirectory()
        [
                'index.html',
                'icon.png',
                'file_index.html',
                'plain_file_index.html',
                'message_file.html',
                "qrcode-$projectName-${fullVersionString}.png"
        ].every {
            def f = new File(releaseDir, it)
            f.exists() && f.isFile() && f.size() > 0
        }
    }

    def 'index.html is generated and validated'() {
        when:
        task.buildAPKArtifacts()
        task.otaIndexFileArtifact()
        task.prepareOTAIndexFile()

        then:
        !releaseConf.otaIndexFile.location.text.contains('null')
        def slurper = new XmlSlurper().parse(releaseConf.otaIndexFile.location)
        slurper.head.title.text() == "$projectName - Android"
        slurper.body.div[0].div[0].h1.text() == 'OTA installation - Android'
        slurper.body.div[0].div[1].div[0].ul.li.img.@src.text() == 'icon.png'
        slurper.body.div[0].div[1].div.ul.li[0].text() == projectName
        slurper.body.div[0].div[1].div.ul.li[1].text().trim().startsWith('Version: 1.0.1')
        slurper.body.div[0].div[1].div[1].section.header.h3.div.text() == 'Main installation'
        slurper.body.div[0].div[1].div[2].ul.li.div.div[0].text() == 'MainVariant'
        slurper.body.div[0].div[1].div[2].ul.li.div.div[1].text() == 'Install'
        slurper.body.div[0].div[1].div[2].ul.li.div.div[1].a.@href.text() ==
                'http://ota.polidea.pl/TestAndroidProject/1.0.1_42/TestAndroidProject-release-MainVariant-1.0.1_42.apk'
        slurper.body.div[0].div[1].p.text().contains('release')
        slurper.body.div[0].div[1].p.text().contains('notes')
        slurper.body.div[0].div[1].div[3].text() == 'Other installations'
        slurper.body.div[0].div[1].div[4].ul.li.div[0].div[0].text() == 'Variant1'
        slurper.body.div[0].div[1].div[4].ul.li.div[0].div[1].text() == 'Install'
        slurper.body.div[0].div[1].div[4].ul.li.div[0].div[1].a.@href.text() ==
                'http://ota.polidea.pl/TestAndroidProject/1.0.1_42/TestAndroidProject-debug-Variant1-1.0.1_42.apk'
        slurper.body.div[0].div[1].div[4].ul.li.div[1].div[0].text() == 'Variant2'
        slurper.body.div[0].div[1].div[4].ul.li.div[1].div[1].text() == 'Install'
        slurper.body.div[0].div[1].div[4].ul.li.div[1].div[1].a.@href.text() ==
                'http://ota.polidea.pl/TestAndroidProject/1.0.1_42/TestAndroidProject-release-Variant2-1.0.1_42.apk'
    }

    def 'file_index.html is generated and validated'() {
        given:
        releaseConf.QRCodeFile = GroovyMock(FlowArtifact) {
            getRelativeUrl(_) >> 'qr.png'
        }
        releaseConf.mailMessageFile = GroovyMock(FlowArtifact) {
            getRelativeUrl(_) >> 'mail_message'
        }
        releaseConf.imageMontageFile = GroovyMock(FlowArtifact) {
            getRelativeUrl(_) >> 'image_montage.png'
        }
        releaseConf.plainFileIndexFile = GroovyMock(FlowArtifact) {
            getRelativeUrl(_) >> 'plain_file_index.html'
        }

        when:
        task.buildAPKArtifacts()
        task.fileIndexArtifact()
        task.prepareFileIndexFile()

        then:
        !releaseConf.fileIndexFile.location.text.contains('null')
        def slurper = new XmlSlurper().parse(releaseConf.fileIndexFile.location)
        slurper.body.div[0].div[0].h1.text() == 'Files to download'
        slurper.body.div[0].div[1].div[0].section.header.h3.text() == projectName
        slurper.body.div[0].div[1].div[0].section.header.div.text().trim().startsWith('Version: 1.0.1')
        slurper.body.div[0].div[1].div[0].section.ul.li*.text().containsAll(variantsConf.variants*.name).collect {
            "APK file: $it"
        }
        slurper.body.div[0].div[1].div[0].section.ul.li.a.@href*.text().containsAll([
                'TestAndroidProject-release-MainVariant-1.0.1_42.apk',
                'TestAndroidProject-debug-Variant1-1.0.1_42.apk',
                'TestAndroidProject-release-Variant2-1.0.1_42.apk']
        )
        slurper.body.div.div.div.section.ul.li.a*.text().containsAll(
                ['Mail message', 'Image montage file', 'QR Code', 'Plain file index']
        )
        slurper.body.div.div.div.section.ul.li.a.@href*.text().containsAll(
                ['mail_message', 'image_montage.png', 'qr.png', 'plain_file_index.html']
        )
    }

    def 'plain_file_index.html is generated and validated'() {
        given:
        releaseConf.QRCodeFile = GroovyMock(FlowArtifact) {
            getRelativeUrl(_) >> 'http://ota.polidea.pl'
            getUrl() >> 'http://ota.polidea.pl/qr.png'.toURL()
        }
        releaseConf.mailMessageFile = GroovyMock(FlowArtifact) {
            getRelativeUrl(_) >> 'http://ota.polidea.pl'
            getUrl() >> 'http://ota.polidea.pl/mail_message'.toURL()
        }
        releaseConf.imageMontageFile = GroovyMock(FlowArtifact) {
            getRelativeUrl(_) >> 'http://ota.polidea.pl'
            getUrl() >> 'http://ota.polidea.pl/image_montage.png'.toURL()
        }

        when:
        task.buildAPKArtifacts()
        task.plainFileIndexArtifact()
        task.preparePlainFileIndexFile()

        then:
        !releaseConf.plainFileIndexFile.location.text.contains('null')
        def slurper = new XmlSlurper().parse(releaseConf.plainFileIndexFile.location)
        slurper.body.h1.text() == 'TestAndroidProject'
        slurper.body.text().contains('Version: 1.0.1')
        slurper.body.h2[0].text() == 'Application files'
        slurper.body.h2[1].text() == 'Other'
        slurper.body.ul.li*.text().containsAll(
                [
                        'MainVariant : http://ota.polidea.pl/TestAndroidProject/1.0.1_42/TestAndroidProject-release-MainVariant-1.0.1_42.apk',
                        'Variant1 : http://ota.polidea.pl/TestAndroidProject/1.0.1_42/TestAndroidProject-debug-Variant1-1.0.1_42.apk',
                        'Variant2 : http://ota.polidea.pl/TestAndroidProject/1.0.1_42/TestAndroidProject-release-Variant2-1.0.1_42.apk'
                ]
        )
        slurper.body.ul[1].li[0].text() == 'Mail message: http://ota.polidea.pl/mail_message'
        slurper.body.ul[1].li[1].text() == 'Image montage: http://ota.polidea.pl/image_montage.png'
        slurper.body.ul[1].li[2].text() == 'QR code: http://ota.polidea.pl/qr.png'
    }

    def 'message_file.html is generated and validated'() {
        given:
        releaseConf.otaIndexFile = GroovySpy(FlowArtifact) {
            getUrl() >> 'http://ota.polidea.pl/otaIndexFile.html'.toURL()
        }
        releaseConf.fileIndexFile = GroovySpy(FlowArtifact) {
            getUrl() >> 'http://ota.polidea.pl/fileIndexFile.html'.toURL()
        }
        releaseConf.apkFiles[variantsConf.mainVariant] = GroovySpy(FlowArtifact) {
            getLocation() >> GroovyMock(File) {
                size() >> 3145l
            }
        }

        when:
        task.mailMsgArtifact()
        task.prepareMailMsg()

        then:
        !releaseConf.mailMessageFile.location.text.contains('null')
        releaseConf.releaseMailSubject == "Android $projectName $fullVersionString is ready to install"
        def slurper = new XmlSlurper().parse(releaseConf.mailMessageFile.location)
        slurper.head.title.text() == 'TestAndroidProject - Android'
        slurper.body.b[0].text() == 'TestAndroidProject'
        slurper.body.b[1].text() == '1.0.1'
        slurper.body.p[0].ul.li.a.@href.text() == 'http://ota.polidea.pl/otaIndexFile.html'
        slurper.body.p[1].ul.li[0].text() == 'release'
        slurper.body.p[1].ul.li[1].text() == 'notes'
        slurper.body.p[2].ul.li.a.@href.text() == 'http://ota.polidea.pl/fileIndexFile.html'
    }
}
