package com.apphance.ameba.runBuilds.android

import com.apphance.ameba.ProjectHelper
import org.gradle.tooling.BuildLauncher
import org.gradle.tooling.GradleConnector
import org.gradle.tooling.ProjectConnection
import org.junit.*

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertTrue;

class RunPrepareAndroidSetupTest {
    static File testProject = new File("testProjects/android")
    static File gradleProperties = new File(testProject, "gradle.properties")
    static File gradlePropertiesOrig = new File(testProject, "gradle.properties.orig")
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
        connection = GradleConnector.newConnector().forProjectDirectory(testProject).connect();
    }

    @AfterClass
    static public void afterClass() {
        connection.close()
    }

    String runTests(String input, String... tasks) {
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
        String res = runTests('\n' * 252 + 'y\n', 'prepareSetup')
        String text = gradleProperties.text
        String originalText = gradlePropertiesOrig.text
        assertEquals(originalText, text)
        println text
    }

    @Test
    public void testGenerateDefaults() throws Exception {
        gradleProperties.delete()
        String res = runTests('\n' * 252 + 'y\n', 'prepareSetup')
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
# Android properties
###########################################################
android.mainVariant=market
android.excluded.builds=
android.minSdk.target=android-7
###########################################################
# Release properties
###########################################################
release.project.icon.file=res/drawable-hdpi/icon.png
release.project.url=
release.project.language=en
release.project.country=US
release.mail.from=
release.mail.to=
release.mail.flags=qrCode,imageMontage
###########################################################
# Apphance properties
###########################################################
apphance.appkey=
apphance.mode=QA
apphance.log.events=false
###########################################################
# Android test properties
###########################################################
android.test.emulator.skin=WVGA800
android.test.emulator.cardSize=200M
android.test.emulator.snapshotEnabled=true
android.test.emulator.noWindow=true
android.test.emulator.target=Google Inc.:Google APIs:10
android.test.directory=test/android
android.test.perPackage=false
android.test.mockLocation=false
android.useEmma=true
###########################################################
# Android jar library properties
###########################################################
android.jarLibrary.resPrefix=
""", text)
        println text
    }
}
