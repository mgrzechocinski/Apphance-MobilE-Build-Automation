package com.apphance.flow.plugins.ios.release

import com.apphance.flow.configuration.ios.variants.IOSVariantsConfiguration
import com.apphance.flow.executor.command.Command
import com.apphance.flow.plugins.ios.builder.IOSBuilderInfo
import com.apphance.flow.plugins.ios.parsers.MobileProvisionParser
import com.apphance.flow.plugins.release.FlowArtifact

import javax.inject.Inject

import static com.apphance.flow.configuration.ios.IOSConfiguration.FAMILIES
import static java.io.File.separator
import static org.gradle.api.logging.Logging.getLogger

class IOSSimulatorArtifactsBuilder extends AbstractIOSArtifactsBuilder {

    def l = getLogger(getClass())

    @Inject IOSVariantsConfiguration variantsConf
    @Inject MobileProvisionParser mpParser

    void buildArtifacts(IOSBuilderInfo bi) {
        FAMILIES.each { family ->
            prepareSimulatorBundleFile(bi, family)
        }
    }

    private void prepareSimulatorBundleFile(IOSBuilderInfo bi, String family) {

        FlowArtifact file = new FlowArtifact()
        file.name = "Simulator build for ${family}"
        file.url = new URL("$releaseConf.releaseUrlVersioned$separator$bi.id$separator${bi.filePrefix}-${family}-simulator-image.dmg")
        file.location = new File(releaseConf.releaseDir, "$bi.id$separator${bi.filePrefix}-${family}-simulator-image.dmg")
        file.location.parentFile.mkdirs()
        file.location.delete()
        def File tmpDir = File.createTempFile("${conf.projectName.value}-${bi.target}-${family}-simulator", ".tmp")
        tmpDir.delete()
        tmpDir.mkdir()
        def destDir = new File(tmpDir, "${bi.target} (${family}_Simulator) ${conf.versionString}_${conf.versionCode}.app")
        destDir.mkdir()
        rsyncTemplatePreservingExecutableFlag(destDir)
        File embedDir = new File(destDir, "Contents/Resources/EmbeddedApp")
        embedDir.mkdirs()
        File sourceApp = new File("$bi.archiveDir${separator}Products${separator}Applications", bi.appName)
        rsyncEmbeddedAppPreservingExecutableFlag(sourceApp, embedDir)
        updateBundleId(bi, destDir)
        resampleIcon(destDir)
        updateDeviceFamily(family, embedDir, bi)
        updateVersions(embedDir, bi)
        String[] cmd = [
                'hdiutil',
                'create',
                file.location.canonicalPath,
                '-srcfolder',
                destDir,
                '-volname',
                "${conf.projectName.value}-${bi.target}-${family}"
        ]
        executor.executeCommand(new Command(runDir: conf.rootDir, cmd: cmd))
        releaseConf.dmgImageFiles.put("${family}-${variantsConf.mainVariant.target}" as String, file)
        l.lifecycle("Simulator zip file created: ${file.location} for ${family}-${variantsConf.mainVariant.target}")
    }

    private rsyncTemplatePreservingExecutableFlag(File destDir) {
        def cmd = [
                'rsync',
                '-aE',
                '--exclude',
                'Contents/Resources/EmbeddedApp',
                '/Applications/Simulator Bundler.app/Contents/Resources/Launcher.app/',
                destDir
        ]
        executor.executeCommand(new Command(runDir: conf.rootDir, cmd: cmd))
    }

    private rsyncEmbeddedAppPreservingExecutableFlag(File sourceAppDir, File embedDir) {
        def cmd = [
                'rsync',
                '-aE',
                sourceAppDir,
                embedDir
        ]
        executor.executeCommand(new Command(runDir: conf.rootDir, cmd: cmd))
    }

    private updateBundleId(IOSBuilderInfo bi, File tmpDir) {
        def bundleId = mpParser.bundleId(bi.mobileprovision)
        File contentsPlist = new File(tmpDir, "Contents/Info.plist")
        runPlistBuddy("Set :CFBundleIdentifier ${bundleId}.launchsim", contentsPlist)
    }

    private resampleIcon(File tmpDir) {
        String[] cmd = [
                '/opt/local/bin/convert',
                new File(conf.rootDir, releaseConf.iconFile.value.path).canonicalPath,
                '-resample',
                '128x128',
                new File(tmpDir, "Contents/Resources/Launcher.icns").canonicalPath
        ]
        executor.executeCommand(new Command(runDir: conf.rootDir, cmd: cmd))
    }

    private updateDeviceFamily(String device, File embedDir, IOSBuilderInfo bi) {
        File targetPlistFile = new File(embedDir, "${bi.target}.app/Info.plist")
        runPlistBuddy('Delete UIDeviceFamily', targetPlistFile, false)
        runPlistBuddy('Add UIDeviceFamily array', targetPlistFile)
        String family = (device == "iPhone" ? "1" : "2")
        runPlistBuddy("Add UIDeviceFamily:0 integer ${family}", targetPlistFile)
    }

    private updateVersions(File embedDir, IOSBuilderInfo bi) {
        File targetPlistFile = new File(embedDir, "${bi.target}.app/Info.plist")
        runPlistBuddy('Delete CFBundleVersion', targetPlistFile, false)
        runPlistBuddy("Add CFBundleVersion string ${conf.versionCode}", targetPlistFile)
        runPlistBuddy('Delete CFBundleShortVersionString', targetPlistFile, false)
        runPlistBuddy("Add CFBundleShortVersionString string ${conf.versionString}", targetPlistFile)
    }

    private runPlistBuddy(String command, File targetPlistFile, boolean failOnError = true) {
        String[] cmd = [
                '/usr/libexec/PlistBuddy',
                '-c',
                command,
                targetPlistFile
        ]
        executor.executeCommand(new Command(runDir: conf.rootDir, cmd: cmd, failOnError: failOnError))
    }
}
