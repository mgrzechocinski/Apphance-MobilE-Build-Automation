package com.apphance.ameba.executor

import com.apphance.ameba.executor.command.CommandExecutor
import com.apphance.ameba.executor.command.CommandLogFilesGenerator
import com.apphance.ameba.executor.linker.FileLinker
import spock.lang.Shared
import spock.lang.Specification

import java.nio.file.Files

class AntExecutorSpec extends Specification {

    @Shared File rootDir = new File('src/test/resources/com/apphance/ameba/executor/antTestProject')

    def antExecutor = new AntExecutor()

    def setup() {
        def fileLinker = Stub(FileLinker)
        def logFilesGenerator = Stub(CommandLogFilesGenerator)
        def executor = new CommandExecutor(fileLinker, logFilesGenerator)
        logFilesGenerator.commandLogFiles() >> [:]

        antExecutor.executor = executor
    }

    def "successfully execute target"() {
        given:
            def md5File = new File(rootDir.absolutePath + '/' + 'build.xml.MD5')
            Files.deleteIfExists(md5File.toPath())

        expect: !md5File.exists()
        when: antExecutor.executeTarget rootDir, "testTarget"
        then: md5File.exists()
        cleanup: Files.deleteIfExists(md5File.toPath())
    }
}
