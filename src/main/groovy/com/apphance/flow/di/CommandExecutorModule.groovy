package com.apphance.flow.di

import com.apphance.flow.executor.command.CommandLogFilesGenerator
import com.google.inject.AbstractModule
import com.google.inject.Provides
import org.gradle.api.Project

import static com.apphance.flow.configuration.ProjectConfiguration.LOG_DIR

class CommandExecutorModule extends AbstractModule {

    private Project project

    CommandExecutorModule(Project project) {
        this.project = project
    }

    @Override
    protected void configure() {}

    @Provides
    @javax.inject.Singleton
    CommandLogFilesGenerator commandLogFileGenerator() {
        return new CommandLogFilesGenerator(createLogDir())
    }

    private File createLogDir() {
        def logDir = project.file(LOG_DIR)
        if (!logDir.exists())
            logDir.mkdirs()
        logDir
    }
}
