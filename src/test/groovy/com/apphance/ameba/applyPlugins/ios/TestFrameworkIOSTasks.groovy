package com.apphance.ameba.applyPlugins.ios

import com.apphance.ameba.AmebaCommonBuildTaskGroups
import com.apphance.ameba.ios.plugins.buildplugin.IOSPlugin
import com.apphance.ameba.ios.plugins.framework.IOSFrameworkPlugin
import org.gradle.api.Project
import org.junit.Test

class TestFrameworkIOSTasks extends AbstractBaseIOSTaskTest {

    protected Project getProject() {
        Project project = super.getProject()
        project.project.plugins.apply(IOSPlugin.class)
        project.project.plugins.apply(IOSFrameworkPlugin.class)
        return project
    }

    @Test
    public void testBuildTasksAvailable() {
        verifyTasksInGroup(getProject(), [
                'clean',
                'buildAll',
                'buildAllSimulators',
                'build-GradleXCode-BasicConfiguration',
                'buildSingleVariant',
                'buildFramework',
                'copyMobileProvision',
                'unlockKeyChain',
                'copySources',
                'copyDebugSources',
        ], AmebaCommonBuildTaskGroups.AMEBA_BUILD)
    }
}
