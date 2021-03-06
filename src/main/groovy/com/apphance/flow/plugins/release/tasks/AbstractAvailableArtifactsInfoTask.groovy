package com.apphance.flow.plugins.release.tasks

import com.apphance.flow.configuration.ProjectConfiguration
import com.apphance.flow.configuration.release.ReleaseConfiguration
import com.apphance.flow.plugins.release.FlowArtifact
import groovy.text.SimpleTemplateEngine
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject

import static com.apphance.flow.plugins.FlowTasksGroups.FLOW_RELEASE
import static com.apphance.flow.util.file.FileDownloader.downloadFile
import static java.io.File.separator
import static java.net.URLEncoder.encode
import static java.util.ResourceBundle.getBundle

abstract class AbstractAvailableArtifactsInfoTask extends DefaultTask {

    static final NAME = 'prepareAvailableArtifactsInfo'
    String group = FLOW_RELEASE
    String description = 'Prepares information about available artifacts for mail message to include'

    @Inject ProjectConfiguration conf
    @Inject ReleaseConfiguration releaseConf

    private SimpleTemplateEngine templateEngine = new SimpleTemplateEngine()

    @Lazy Map basicBinding = [
            title: conf.projectName.value,
            version: conf.versionString,
            currentDate: releaseConf.buildDate
    ]

    @TaskAction
    void availableArtifactsInfo() {

        mailMsgArtifact()
        fileIndexArtifact()
        plainFileIndexArtifact()
        otaIndexFileArtifact()
        qrCodeArtifact()

        prepareOtherArtifacts()

        prepareQRCode()
        prepareIconFile()
        prepareMailMsg()
        preparePlainFileIndexFile()
        prepareOTAIndexFile()
    }

    void mailMsgArtifact() {
        releaseConf.mailMessageFile = new FlowArtifact(
                name: 'Mail message file',
                url: new URL("$releaseConf.releaseUrlVersioned${separator}message_file.html"),
                location: new File(releaseConf.releaseDir, 'message_file.html'))
        releaseConf.mailMessageFile.location.parentFile.mkdirs()
        releaseConf.mailMessageFile.location.delete()
    }

    void fileIndexArtifact() {
        releaseConf.fileIndexFile = new FlowArtifact(
                name: "The file index file: ${conf.projectName.value}",
                url: new URL("$releaseConf.releaseUrlVersioned${separator}file_index.html"),
                location: new File(releaseConf.releaseDir, 'file_index.html')
        )
        releaseConf.fileIndexFile.location.parentFile.mkdirs()
        releaseConf.fileIndexFile.location.delete()
    }

    void plainFileIndexArtifact() {
        releaseConf.plainFileIndexFile = new FlowArtifact(
                name: "The plain file index file: ${conf.projectName.value}",
                url: new URL("$releaseConf.releaseUrlVersioned${separator}plain_file_index.html"),
                location: new File(releaseConf.releaseDir, 'plain_file_index.html'))
        releaseConf.plainFileIndexFile.location.parentFile.mkdirs()
        releaseConf.plainFileIndexFile.location.delete()
    }

    void otaIndexFileArtifact() {
        releaseConf.otaIndexFile = new FlowArtifact(
                name: "The ota index file: ${conf.projectName.value}",
                url: new URL("$releaseConf.releaseUrlVersioned${separator}index.html"),
                location: new File(releaseConf.releaseDir, 'index.html'))
        releaseConf.otaIndexFile.location.parentFile.mkdirs()
        releaseConf.otaIndexFile.location.delete()
    }

    void qrCodeArtifact() {
        def qrCodeFileName = "qrcode-${conf.projectName.value}-${conf.fullVersionString}.png"
        def qrCodeFile = new File(releaseConf.releaseDir, qrCodeFileName)
        releaseConf.QRCodeFile = new FlowArtifact(
                name: 'QR Code',
                url: new URL("$releaseConf.releaseUrlVersioned$separator$qrCodeFileName"),
                location: qrCodeFile)
        releaseConf.QRCodeFile.location.parentFile.mkdirs()
        releaseConf.QRCodeFile.location.delete()
    }

    abstract void prepareOtherArtifacts()

    void prepareQRCode() {
        def urlEncoded = encode(releaseConf.otaIndexFile.url.toString(), 'utf-8')
        downloadFile(new URL("https://chart.googleapis.com/chart?cht=qr&chs=256x256&chl=${urlEncoded}"), releaseConf.QRCodeFile.location)
        logger.lifecycle("QRCode created: ${releaseConf.QRCodeFile.location}")
    }

    void prepareIconFile() {
        ant.copy(
                file: new File(project.rootDir, releaseConf.iconFile.value.path),
                tofile: new File(releaseConf.releaseDir, releaseConf.iconFile.value.name)
        )
    }

    void prepareMailMsg() {
        releaseConf.releaseMailSubject = fillMailSubject(bundle('mail_message'))
        def result = fillTemplate(loadTemplate('mail_message.html'), mailMsgBinding())
        releaseConf.mailMessageFile.location.write(result.toString(), 'UTF-8')
        logger.lifecycle("Mail message file created: ${releaseConf.mailMessageFile.location}")
    }

    private String fillMailSubject(ResourceBundle rb) {
        String subject = rb.getString('Subject')
        Eval.me('conf', conf, /"$subject"/)
    }

    abstract Map mailMsgBinding()

    void preparePlainFileIndexFile() {
        def result = fillTemplate(loadTemplate('plain_file_index.html'), plainFileIndexFileBinding())
        templateToFile(releaseConf.plainFileIndexFile.location, result)
        logger.lifecycle("Plain file index created: ${releaseConf.plainFileIndexFile.location}")
    }

    abstract Map plainFileIndexFileBinding()

    void prepareOTAIndexFile() {
        def result = fillTemplate(loadTemplate('index.html'), otaIndexFileBinding())
        templateToFile(releaseConf.otaIndexFile.location, result)
        logger.lifecycle("OTA index created: ${releaseConf.otaIndexFile.location}")
    }

    abstract Map otaIndexFileBinding()

    ResourceBundle bundle(String id) {
        def c = getClass()
        getBundle("${c.package.name}.$id", releaseConf.locale, c.classLoader)
    }

    URL loadTemplate(String template) {
        getClass().getResource(template)
    }

    Writable fillTemplate(URL tmpl, Map binding) {
        templateEngine.createTemplate(tmpl).make(binding)
    }

    void templateToFile(File f, Writable tmpl) {
        f.write(tmpl.toString(), 'UTF-8')
    }
}
