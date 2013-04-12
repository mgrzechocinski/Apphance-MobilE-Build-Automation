package com.apphance.ameba.plugins.android.apphance.tasks

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

import static java.lang.System.getProperties

class ApphanceLogsConversionTaskSpec extends Specification {

    def 'converts android logs to apphance'() {

        given:
        def ant = new AntBuilder()
//        def logConverter = new ApphanceLogsConversionTask(ant)
        def tmpDir = new File(properties['java.io.tmpdir'].toString(), 'src')
        tmpDir.mkdirs()

        Project proj = ProjectBuilder.builder().withProjectDir(tmpDir.parentFile).build()
        ApphanceLogsConversionTask logConverter = proj.task('logConverter', type: ApphanceLogsConversionTask)

        and:
        def filenameWithLogs = 'AndroidToApphanceWithLogs.java'
        def filenameWithoutLogs = 'AndroidToApphanceWithoutLogs.java'

        and:
        def classWithLogs = new File(getClass().getResource(
                "${filenameWithLogs}src").file)
        def classWithoutLogs = new File(getClass().getResource(
                "${filenameWithoutLogs}src").file)

        and:
        ant.copy(
                file: classWithLogs.canonicalPath,
                toFile: new File(tmpDir.canonicalPath, filenameWithLogs))
        ant.copy(
                file: classWithoutLogs.canonicalPath,
                toFile: new File(tmpDir.canonicalPath, filenameWithoutLogs))

        when:
        logConverter.convertLogsToApphance()

        then:
        !(new File(tmpDir, filenameWithLogs).text).contains('android.util.Log')
        (new File(tmpDir, filenameWithLogs).text).contains('com.apphance.android.Log')
        !(new File(tmpDir, filenameWithoutLogs).text).contains('android.util.Log')
        tmpDir.deleteDir()
        !tmpDir.exists()
    }
}
