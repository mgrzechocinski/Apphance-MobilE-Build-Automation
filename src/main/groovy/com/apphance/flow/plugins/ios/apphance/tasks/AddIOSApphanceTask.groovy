package com.apphance.flow.plugins.ios.apphance.tasks

import com.apphance.flow.configuration.ios.IOSConfiguration
import com.apphance.flow.configuration.ios.variants.AbstractIOSVariant
import com.apphance.flow.executor.IOSExecutor
import com.apphance.flow.executor.command.Command
import com.apphance.flow.executor.command.CommandExecutor
import com.apphance.flow.plugins.apphance.ApphancePluginCommons
import com.google.inject.Inject
import com.google.inject.assistedinject.Assisted
import org.gradle.api.GradleException

import static com.apphance.flow.util.file.FileManager.MAX_RECURSION_LEVEL
import static groovy.io.FileType.DIRECTORIES
import static groovy.io.FileType.FILES
import static java.io.File.separator
import static org.gradle.api.logging.Logging.getLogger

@Mixin(ApphancePluginCommons)
class AddIOSApphanceTask {

    static final FRAMEWORK_PATTERN = ~/.*[aA]pphance.*\.framework/

    private log = getLogger(getClass())

    @Inject CommandExecutor executor
    @Inject IOSExecutor iosExecutor
    @Inject IOSConfiguration iosConfiguration
    @Inject PbxProjectHelper pbxProjectHelper


    private AbstractIOSVariant variant
    private String target
    private String configuration

    @Inject
    AddIOSApphanceTask(@Assisted AbstractIOSVariant variantConf) {
        this.pbxProjectHelper = new PbxProjectHelper(variantConf.apphanceLibVersion.value, variantConf.apphanceMode.value.toString())

        this.variant = variantConf
        this.target = variantConf.target
        this.configuration = variantConf.target
    }

    void addIOSApphance() {
        //TODO way of using single variant builder has changed, to refactor when apphance refactoring is being done
//        def builder = new IOSSingleVariantBuilder(iosExecutor: iosExecutor)
//        if (!isApphancePresent(builder.tmpDir(target, configuration))) {
//            log.lifecycle("Adding Apphance to ${variant} (${target}, ${configuration}): ${builder.tmpDir(target, configuration)}. Project file = ${variant.tmpDir}")
//            pbxProjectHelper.addApphanceToProject(
//                    builder.tmpDir(target, configuration),
//                    iosConfiguration.xcodeDir.value,
//                    target,
//                    configuration,
//                    variant.apphanceAppKey.value)
//            copyApphanceFramework(builder.tmpDir(target, configuration))
//        }
    }

    private boolean isApphancePresent(File projectDir) {
        log.lifecycle("Looking for apphance in: ${projectDir.absolutePath}")

        def apphancePresent = false

        projectDir.traverse([type: DIRECTORIES, maxDepth: MAX_RECURSION_LEVEL]) { file ->
            if (file.name =~ FRAMEWORK_PATTERN) {
                apphancePresent = true
            }
        }

        log.lifecycle("Apphance ${apphancePresent ? 'already' : 'not'} in project")

        apphancePresent
    }

    private copyApphanceFramework(File libsDir) {

        def apphanceLibDependency = prepareApphanceLibDependency(project, 'com.apphance:ios.pre-production.armv7:1.8+')

        libsDir.mkdirs()
        clearLibsDir(libsDir)
        log.lifecycle("Copying apphance framework directory " + libsDir)

        try {
            project.copy {
                from { project.configurations.apphance }
                into libsDir
                rename { String filename ->
                    'apphance.zip'
                }
            }
        } catch (e) {
            def msg = "Error while resolving dependency: '$apphanceLibDependency'"
            log.error("$msg.\nTo solve the problem add correct dependency to gradle.properties file or add -Dapphance.lib=<apphance.lib> to invocation.\n" +
                    "Dependency should be added in gradle style to 'apphance.lib' entry")
            throw new GradleException(msg)
        }

        def projectApphanceZip = new File(libsDir, "apphance.zip")
        log.lifecycle("Unpacking file " + projectApphanceZip)
        log.lifecycle("Exists " + projectApphanceZip.exists())
        executor.executeCommand(new Command(runDir: project.rootDir,
                cmd: ['unzip', projectApphanceZip.canonicalPath, '-d', libsDir.canonicalPath]))

        checkFrameworkFolders(apphanceLibDependency, libsDir)

        project.delete {
            projectApphanceZip
        }
    }

    private clearLibsDir(File libsDir) {
        libsDir.traverse([type: FILES, maxDepth: MAX_RECURSION_LEVEL]) { framework ->
            if (framework.name =~ FRAMEWORK_PATTERN) {
                log.lifecycle("Removing old apphance framework: " + framework.name)
                delClos(new File(framework.canonicalPath))
            }
        }
    }

    private delClos = {
        it.eachDir(delClos);
        it.eachFile {
            it.delete()
        }
    }

    private void checkFrameworkFolders(String apphanceLib, File libsDir) {
        def libVariant = apphanceLib.split(':')[1].split('\\.')[1].replace('p', 'P')
        def frameworkFolder = "Apphance-${libVariant}.framework"
        def frameworkFolderFile = new File(libsDir.canonicalPath + separator + frameworkFolder)
        if (!frameworkFolderFile.exists() || !frameworkFolderFile.isDirectory() || !(frameworkFolderFile.length() > 0l)) {
            throw new GradleException("There is no framework folder (or may be empty): ${frameworkFolderFile.canonicalPath} associated with apphance version: '${apphanceLib}'")
        }
    }
}