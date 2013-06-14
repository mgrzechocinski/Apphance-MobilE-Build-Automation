package com.apphance.flow.di

import com.apphance.flow.detection.ProjectTypeDetector
import com.apphance.flow.plugins.ios.apphance.tasks.IOSApphanceEnhancerFactory
import com.apphance.flow.plugins.ios.apphance.tasks.IOSApphancePbxEnhancerFactory
import com.google.inject.AbstractModule
import com.google.inject.assistedinject.FactoryModuleBuilder
import org.gradle.api.Project

import static com.apphance.flow.detection.ProjectType.IOS

class IOSModule extends AbstractModule {

    private Project project
    private ProjectTypeDetector projectTypeDetector = new ProjectTypeDetector()
    private FactoryModuleBuilder builder = new FactoryModuleBuilder()

    IOSModule(Project project) {
        this.project = project
    }

    @Override
    protected void configure() {
        if (projectTypeDetector.detectProjectType(project.rootDir) == IOS) {
            install(builder.build(IOSApphanceEnhancerFactory))
            install(builder.build(IOSApphancePbxEnhancerFactory))
        }
    }
}
