package com.apphance.ameba.runBuilds.ios;

import static org.junit.Assert.*

import org.gradle.tooling.BuildLauncher
import org.gradle.tooling.GradleConnector
import org.gradle.tooling.ProjectConnection
import org.junit.After
import org.junit.AfterClass
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test

import com.apphance.ameba.ProjectHelper

class RunPrepareIOSSetupTest {
    static File testIosProject = new File("testProjects/ios/GradleXCode")
    static File gradleProperties = new File(testIosProject,"gradle.properties")
    static File gradlePropertiesOrig = new File(testIosProject,"gradle.properties.orig")
    static ProjectConnection connection

    @Before
    void before() {
        gradlePropertiesOrig.delete()
        gradlePropertiesOrig << gradleProperties.text
    }


    @After
    void after() {
        gradleProperties.delete()
        gradleProperties << gradlePropertiesOrig.text
    }

    @BeforeClass
    static void beforeClass() {
        connection = GradleConnector.newConnector().forProjectDirectory(testIosProject).connect();
    }

    @AfterClass
    static void afterClass() {
        connection.close()
    }

    String runTests(String input, String ... tasks) {
        ByteArrayOutputStream os = new ByteArrayOutputStream()
        BuildLauncher bl = connection.newBuild().forTasks(tasks);
        bl.setStandardInput(new ByteArrayInputStream(input.bytes))
        bl.setStandardOutput(os)
        bl.setJvmArguments(ProjectHelper.GRADLE_DAEMON_ARGS)
        bl.run();
        def res = os.toString("UTF-8")
        println res
        assertTrue(res.contains('BUILD SUCCESSFUL'))
        return res
    }

    @Test
    public void testGenerateNoChange() throws Exception {
        String res = runTests('\n'*50 + 'y\n', 'prepareSetup')
        String text = gradleProperties.text
        String originalText = gradlePropertiesOrig.text
        assertEquals(originalText, text)
        println text
    }

    @Test
    public void testGenerateDefaults() throws Exception {
        gradleProperties.delete()
        String res = runTests('\n'*50 + 'y\n', 'prepareSetup')
        assertTrue(gradleProperties.exists())
        String text = gradleProperties.text
        String originalText = gradlePropertiesOrig.text
        assertEquals("""###########################################################
# Generated by Ameba system by running
#    gradle prepareSetup
# You can modify the file manually.
# Or you can re-run the prepareSetup command
# for guided re-configuration
###########################################################
# Mercurial properties
###########################################################
hg.commit.user=
###########################################################
# iOS properties
###########################################################
ios.project.directory=GradleXCode.xcodeproj
ios.plist.file=GradleXCode/GradleXCode-Info.plist
ios.excluded.builds=
ios.families=iPhone,iPad
ios.distribution.resources.dir=
ios.mainTarget=GradleXCode
ios.mainConfiguration=BasicConfiguration
ios.sdk=iphoneos
ios.simulator.sdk=iphonesimulator
###########################################################
# Release properties
###########################################################
release.project.icon.file=icon.png
release.project.url=
release.project.language=en
release.project.country=US
release.mail.from=
release.mail.to=
release.mail.flags=qrCode,imageMontage
###########################################################
# iOS KIF properties
###########################################################
ios.kif.configuration=Debug
###########################################################
# iOS FoneMonkey properties
###########################################################
ios.fonemonkey.configuration=Debug
###########################################################
# iOS Framework properties
###########################################################
ios.framework.target=GradleXCode
ios.framework.configuration=Debug
ios.framework.version=A
ios.framework.headers=GradleXCode/gradleXCodeAppDelegate.h
ios.framework.resources=icon.png
###########################################################
# Apphance properties
###########################################################
apphance.appkey=
apphance.mode=QA
apphance.log.events=false
""", text)
        println text
    }
}
