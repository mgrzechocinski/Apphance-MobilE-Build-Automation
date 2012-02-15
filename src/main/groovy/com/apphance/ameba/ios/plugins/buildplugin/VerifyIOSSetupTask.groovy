package com.apphance.ameba.ios.plugins.buildplugin

import org.gradle.api.GradleException
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.tasks.TaskAction

import com.apphance.ameba.AbstractVerifySetupTask
import com.apphance.ameba.PropertyCategory
import com.apphance.ameba.ios.IOSProjectConfiguration;
import com.apphance.ameba.ios.IOSXCodeOutputParser


class VerifyIOSSetupTask extends AbstractVerifySetupTask {
    Logger logger = Logging.getLogger(VerifyIOSSetupTask.class)

    IOSXCodeOutputParser iosXCodeOutputParser
    IOSProjectConfiguration iosConf

    VerifyIOSSetupTask() {
        super(IOSProjectProperty.class)
        this.dependsOn(project.verifyBaseSetup)
    }


    @TaskAction
    void verifySetup() {
        use (PropertyCategory) {
            def projectProperties = readProperties()
            IOSProjectProperty.each{ checkProperty(projectProperties, it) }
            iosXCodeOutputParser = new IOSXCodeOutputParser()
            iosConf = iosXCodeOutputParser.getIosProjectConfiguration(project)
            checkPlistFile()
            checkFamilies()
            checkDistributionDir()
            checkTargetsAndConfigurations()
            checkSDKs()
            checkMainTarget()
            checkMainConfiguration()
            allPropertiesOK()
        }
    }

    void checkPlistFile() {
        use (PropertyCategory) {
            File plistFile = new File(project.rootDir,project.readExpectedProperty(IOSProjectProperty.PLIST_FILE))
            if (!plistFile.exists() || !plistFile.isFile()) {
                throw new GradleException("""The plist file ${IOSProjectProperty.PLIST_FILE.propertyName}:${plistFile}) does not exist or is not a file. Please run 'gradle prepareSetup' to correct it.""")
            }
        }
    }
    void checkSDKs() {
        use (PropertyCategory){
            String sdk = project.readProperty(IOSProjectProperty.IOS_SDK)
            if (!iosConf.allIphoneSDKs.contains(sdk)) {
                throw new GradleException("iPhone sdk ${IOSProjectProperty.IOS_SDK.propertyName}:${sdk} is not on the list of sdks ${iosConf.allIphoneSDKs}")
            }
            String simulatorsdk = project.readProperty(IOSProjectProperty.IOS_SIMULATOR_SDK)
            if (!iosConf.allIphoneSimulatorSDKs.contains(simulatorsdk)) {
                throw new GradleException("iPhone simulator sdk ${IOSProjectProperty.IOS_SIMULATOR_SDK.propertyName}: ${simulatorsdk} is not on the list of sdks ${iosConf.allIphoneSimulatorSDKs}")
            }
        }
    }

    void checkDistributionDir() {
        use (PropertyCategory) {
            if (!iosConf.distributionDirectory.exists() || !iosConf.distributionDirectory.isDirectory()) {
                throw new GradleException("""The distribution resources directory (${iosConf.distributionDirectory}) does not exist or is not a directory. Please run 'gradle prepareSetup' to correct it.""")
            }
            boolean hasMobileProvision = false
            iosConf.distributionDirectory.list().each {
                if (it.endsWith('.mobileprovision')) {
                    hasMobileProvision = true
                }
            }
            if (!hasMobileProvision) {
                throw new GradleException("""The distribution resources directory (${iosConf.distributionDirectory}) should contain at least one .mobileprovision file. """)
            }
        }
    }

    void checkFamilies() {
        use (PropertyCategory) {
            String[] families = project.readProperty(IOSProjectProperty.IOS_FAMILIES).split(',')
            families.each { family ->
                if (!IOSPlugin.FAMILIES.contains(family)) {
                    throw new GradleException("""The family in ${IOSProjectProperty.IOS_FAMILIES.propertyName}: ${families} can only be one of ${IOSPlugin.FAMILIES}""")
                }
            }
        }
    }

    void checkMainTarget() {
        use (PropertyCategory) {
            String mainTarget = project.readProperty(IOSProjectProperty.MAIN_TARGET)
            if (!iosConf.alltargets.contains(mainTarget)) {
                throw new GradleException("""The main target in ${IOSProjectProperty.MAIN_TARGET.propertyName}: ${mainTarget} can only be one of ${iosConf.alltargets}""")
            }
        }
    }

    void checkMainConfiguration() {
        use (PropertyCategory) {
            String mainConfiguration = project.readProperty(IOSProjectProperty.MAIN_CONFIGURATION)
            if (!iosConf.allconfigurations.contains(mainConfiguration)) {
                throw new GradleException("""The main configuration in ${IOSProjectProperty.MAIN_CONFIGURATION.propertyName}: ${mainConfiguration} can only be one of ${iosConf.allconfigurations}""")
            }
        }
    }

    void checkTargetsAndConfigurations() {
        use (PropertyCategory) {
            if (iosConf.targets == ['']) {
                throw new GradleException("You must specify at least one target")
            }
            if (iosConf.configurations == ['']) {
                throw new GradleException("You must specify at least one configuration")
            }
            if (iosConf.excludedBuilds != ['.*']&& iosConf.excludedBuilds.size != iosConf.targets.size * iosConf.configurations.size) {
                if (!iosConf.targets.contains(iosConf.mainTarget)) {
                    throw new GradleException("Main target ${iosConf.mainTarget} is not on the list of targets ${iosConf.targets}")
                }
                if (!iosConf.configurations.contains(iosConf.mainConfiguration)) {
                    throw new GradleException("Main configuration ${iosConf.mainConfiguration} is not on the list of targets ${iosConf.configurations}")
                }
                def id = "${iosConf.mainTarget}-${iosConf.mainConfiguration}".toString()
                if (iosConf.isBuildExcluded(id)) {
                    throw new GradleException("Main target-configuration pair (${id}) is excluded from build by ${iosConf.excludedBuilds}")
                }
            }
        }
    }
}
