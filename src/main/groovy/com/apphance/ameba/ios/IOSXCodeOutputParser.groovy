package com.apphance.ameba.ios

import groovy.lang.Closure

import java.io.File
import java.util.Collection
import java.util.List

import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

class IOSXCodeOutputParser {
    static Logger logger = Logging.getLogger(IOSXCodeOutputParser.class)

    IOSProjectConfiguration getIosProjectConfiguration(Project project){
        if (!project.hasProperty('ios.project.configuration')) {
            project['ios.project.configuration'] = new IOSProjectConfiguration()
        }
        return project['ios.project.configuration']
    }

    Collection readBuildableConfigurations(List trimmedOutput) {
        return readBaseConfigurations(trimmedOutput, {it != "Debug" && it != "Release"})
    }

    Collection readBuildableTargets(List trimmedOutput) {
        return readBaseTargets(trimmedOutput, {!it.endsWith('Tests') && !it.endsWith('Specs')})
    }

    Collection readBaseConfigurations(List trimmed, Closure filter) {
        def startConfigurations = trimmed.indexOf('Build Configurations:')
        def configurations = trimmed[startConfigurations + 1 ..-1]
        def onlyConfigurations = configurations[0.. configurations.indexOf('') - 1]
        return onlyConfigurations.findAll (filter)
    }

    Collection readBaseTargets(List trimmed, Closure filter) {
        def startTargets = trimmed.indexOf('Targets:')
        def targets = trimmed[startTargets + 1 ..-1]
        def onlyTargets = targets[0.. targets.indexOf('') - 1 ]
        return onlyTargets.findAll(filter)
    }

    Collection readIphoneSdks(List trimmed) {
        def startConfigurations = trimmed.indexOf('iOS SDKs:')
        def configurations = trimmed[startConfigurations + 1 ..-1]
        def onlyConfigurations = configurations[0.. configurations.indexOf('') - 1]
        def output = ['iphoneos']
        onlyConfigurations.each {
            output << it[it.indexOf('-sdk ') + '-sdk '.length() .. -1 ]
        }
        return output
    }

    Collection readIphoneSimulatorSdks(List trimmed) {
        def startConfigurations = trimmed.indexOf('iOS Simulator SDKs:')
        def configurations = trimmed[startConfigurations + 1 ..-1]
        def lastIndex = configurations.indexOf('')
        if (lastIndex == -1) {
            lastIndex = 0
        }
        def onlyConfigurations = configurations[0.. lastIndex - 1]
        def output = ['iphonesimulator']
        onlyConfigurations.each {
            output << it[it.indexOf('-sdk ') + '-sdk '.length() .. -1 ]
        }
        return output
    }

    String readProjectName(List trimmed) {
        String firstLine = trimmed[0]
        def matcher = firstLine =~ /.*"(.*)"/
        return matcher[0][1]
    }

    File findMobileProvisionFile(Project project, String target, String configuration) {
        IOSProjectConfiguration iosConf = getIosProjectConfiguration(project)
        File f = new File(iosConf.distributionDirectory,"${target}-${configuration}.mobileprovision")
        if (f.exists()) {
            logger.lifecycle("Mobile provision file found in ${iosConf.distributionDirectory}: ${f}" )
            return f
        }
        f = new File(iosConf.distributionDirectory,"${target}.mobileprovision")
        if (f.exists()) {
            logger.lifecycle("Mobile provision file found in ${iosConf.distributionDirectory}: ${f}" )
            return f
        }
        f = new File(iosConf.distributionDirectory,"${iosConf.mainTarget}.mobileprovision")
        if (f.exists()) {
            logger.lifecycle("Mobile provision file found in ${iosConf.distributionDirectory}: ${f}" )
            return f
        }
        iosConf.distributionDirectory.eachFile {
            if (it.name.endsWith('.mobileprovision')) {
                f = it
            }
        }
        if (f == null) {
            throw new GradleException("The mobileprovision file cannot be found in ${iosConf.distributionDirectory}.\
 Please add one and name it ${iosConf.mainTarget}.mobileprovision")
        }
        logger.lifecycle("Mobile provision file found in ${iosConf.distributionDirectory}: ${f}" )
        return f
    }
}