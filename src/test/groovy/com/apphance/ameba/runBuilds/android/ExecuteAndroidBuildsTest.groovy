package com.apphance.ameba.runBuilds.android;

import static org.junit.Assert.*


import org.gradle.tooling.GradleConnector
import org.gradle.tooling.ProjectConnection
import org.junit.Test

import com.apphance.ameba.ProjectConfiguration
import com.apphance.ameba.android.AndroidManifestHelper



class ExecuteAndroidBuildsTest {

    File testProject = new File("testProjects/android")
    File testNovariantsProject = new File("testProjects/android-novariants")
    File templateFile = new File("templates/android")

    protected void runGradle(String ... tasks) {
        ProjectConnection connection = GradleConnector.newConnector().forProjectDirectory(testProject).connect();
        try {
            def buildLauncher = connection.newBuild()
            buildLauncher.getProperties().put(project.rootDir, connection)
            buildLauncher.forTasks(tasks).run();
        } finally {
            connection.close();
        }
    }

    protected void runGradleWithProperties(Properties p, String ... tasks) {
        ProjectConnection connection = GradleConnector.newConnector().forProjectDirectory(testProject).connect();
        try {
            def buildLauncher = connection.newBuild()
            def args = p.collect { property , value -> "-D${property}=${value}"}
            buildLauncher.setJvmArguments(args as String[])
            buildLauncher.forTasks(tasks).run()
        } finally {
            connection.close();
        }
    }

    protected void runGradleNoVariants(String ... tasks) {
        ProjectConnection connection = GradleConnector.newConnector().forProjectDirectory(testNovariantsProject).connect();
        try {
            connection.newBuild().forTasks(tasks).run();
        } finally {
            connection.close();
        }
    }

    @Test
    void testCleanCheckTests() {
        runGradle('updateProject', 'clean','checkTests')
        assertFalse(new File(testProject,"bin").exists())
        assertFalse(new File(testProject,"gen").exists())
        assertFalse(new File(testProject,"build").exists())
        assertFalse(new File(testProject,"tmp").exists())
    }

    @Test
    void testOta() {
        runGradle('updateProject', 'cleanRelease')
        assertTrue(new File(testProject,"ota").exists())
        assertEquals(0,new File(testProject,"ota").listFiles().length)
        assertTrue(new File(testProject,"tmp").exists())
        assertEquals(0,new File(testProject,"tmp").listFiles().length)
    }


    @Test
    void testBuildDebug() {
        runGradle('updateProject', 'cleanRelease', 'buildDebug')
        assertTrue(new File(testProject,
                "ota/AdadalkjsaTest/1.0.1-SNAPSHOT_42/TestAndroidProject-debug-test-1.0.1-SNAPSHOT_42.apk").exists())
        assertFalse(new File(testProject,
                "ota/AdadalkjsaTest/1.0.1-SNAPSHOT_42/TestAndroidProject-debug-test-unsigned-1.0.1-SNAPSHOT_42.apk").exists())
        assertFalse(new File(testProject,
                "ota/AdadalkjsaTest/1.0.1-SNAPSHOT_42/TestAndroidProject-debug-test-unaligned-1.0.1-SNAPSHOT_42.apk").exists())
    }


    @Test
    void testBuildRelease() {
        runGradle('updateProject', 'cleanRelease', 'buildRelease')
        assertTrue(new File(testProject,
                "ota/AdadalkjsaTest/1.0.1-SNAPSHOT_42/TestAndroidProject-release-market-1.0.1-SNAPSHOT_42.apk").exists())
        assertFalse(new File(testProject,
                "ota/AdadalkjsaTest/1.0.1-SNAPSHOT_42/TestAndroidProject-release-market-unsigned-1.0.1-SNAPSHOT_42.apk").exists())
        assertFalse(new File(testProject,
                "ota/AdadalkjsaTest/1.0.1-SNAPSHOT_42/TestAndroidProject-release-market-unaligned-1.0.1-SNAPSHOT_42.apk").exists())
    }

    @Test
    void testBuildDebugNoVariant() {
        runGradleNoVariants('updateProject', 'cleanRelease', 'buildDebug')
        assertTrue(new File(testNovariantsProject,
                "ota/asdlakjljsdTest/1.0.1-SNAPSHOT_42/TestAndroidProject-debug-1.0.1-SNAPSHOT_42.apk").exists())
        assertFalse(new File(testNovariantsProject,
                "ota/asdlakjljsdTest/1.0.1-SNAPSHOT_42/TestAndroidProject-debug-unsigned-1.0.1-SNAPSHOT_42.apk").exists())
        assertFalse(new File(testNovariantsProject,
                "ota/asdlakjljsdTest/1.0.1-SNAPSHOT_42/TestAndroidProject-debug-unaligned-1.0.1-SNAPSHOT_42.apk").exists())
    }
    @Test
    void testBuildReleaseNoVariant() {
        runGradleNoVariants('updateProject', 'cleanRelease', 'buildRelease')
        assertTrue(new File(testNovariantsProject,
                "ota/asdlakjljsdTest/1.0.1-SNAPSHOT_42/TestAndroidProject-release-1.0.1-SNAPSHOT_42.apk").exists())
        assertFalse(new File(testNovariantsProject,
                "ota/asdlakjljsdTest/1.0.1-SNAPSHOT_42/TestAndroidProject-release-unsigned-1.0.1-SNAPSHOT_42.apk").exists())
        assertFalse(new File(testNovariantsProject,
                "ota/asdlakjljsdTest/1.0.1-SNAPSHOT_42/TestAndroidProject-release-unaligned-1.0.1-SNAPSHOT_42.apk").exists())
    }


    @Test
    void testJavadoc() {
        runGradle('updateProject', 'javadoc')
        assertTrue(new File(testProject,"build/docs").isDirectory())
        assertFalse(new File(testProject,"build/docs").listFiles().length == 0)
    }

    @Test
    void testUpdateProject() {
        File localProperties = new File(testProject,"local.properties")
        File localPropertiesSubproject = new File(testProject,"subproject/local.properties")
        File localPropertiesSubsubproject = new File(testProject,"subproject/subsubproject/local.properties")
        localProperties.delete()
        localPropertiesSubproject.delete()
        localPropertiesSubsubproject.delete()
        runGradle('updateProject')
        assertTrue(localProperties.exists())
        assertTrue(localPropertiesSubproject.exists())
        assertTrue(localPropertiesSubsubproject.exists())
    }


    @Test
    void testUpdateVersion() {
        AndroidManifestHelper manifestHelper = new AndroidManifestHelper()
        ProjectConfiguration projectConf = new ProjectConfiguration()
        try {
            Properties p = new Properties()
            p.put('version.string', 'TEST_UPDATE')
            runGradleWithProperties(p, 'updateProject', 'updateVersion')
            manifestHelper.readVersion(new File("testProjects/android"), projectConf)
        } finally {
            manifestHelper.restoreOriginalManifest(new File("testProjects/android"))
        }
        assertEquals(43, projectConf.versionCode)
        assertEquals('TEST_UPDATE', projectConf.versionString)
    }


    @Test
    void testAnalysis() {
        runGradle('updateProject' ,'analysis')
        assertTrue(new File("testProjects/android/build/analysis/checkstyle-report.xml").exists())
        assertTrue(new File("testProjects/android/build/analysis/cpd-result.xml").exists())
        assertTrue(new File("testProjects/android/build/analysis/findbugs-result.xml").exists())
        assertTrue(new File("testProjects/android/build/analysis/pmd-result.xml").exists())
    }

    @Test
    void testAnalysisAfterClean() {
        runGradle('clean', 'updateProject' ,'analysis')
        assertTrue(new File("testProjects/android/build/analysis/checkstyle-report.xml").exists())
        assertTrue(new File("testProjects/android/build/analysis/cpd-result.xml").exists())
        // don't now why but it does not work from within assertTrue(new File("testProjects/android/build/analysis/findbugs-result.xml").exists())
        assertTrue(new File("testProjects/android/build/analysis/pmd-result.xml").exists())
    }

    @Test
    void testBuildAndPrepareVariantedMailMessage() {
        runGradle('cleanRelease', 'updateProject' ,'buildAll')
        runGradle('prepareImageMontage', 'prepareMailMessage')
        assertTrue(new File("testProjects/android/ota/AdadalkjsaTest/1.0.1-SNAPSHOT_42/file_index.html").exists())
        assertTrue(new File("testProjects/android/ota/AdadalkjsaTest/1.0.1-SNAPSHOT_42/icon.png").exists())
        assertTrue(new File("testProjects/android/ota/AdadalkjsaTest/1.0.1-SNAPSHOT_42/index.html").exists())
        assertTrue(new File("testProjects/android/ota/AdadalkjsaTest/1.0.1-SNAPSHOT_42/plain_file_index.html").exists())
        assertTrue(new File("testProjects/android/ota/AdadalkjsaTest/1.0.1-SNAPSHOT_42/qrcode-TestAndroidProject-1.0.1-SNAPSHOT_42.png").exists())
        assertTrue(new File("testProjects/android/ota/AdadalkjsaTest/1.0.1-SNAPSHOT_42/TestAndroidProject-debug-test-1.0.1-SNAPSHOT_42.apk").exists())
        assertTrue(new File("testProjects/android/ota/AdadalkjsaTest/1.0.1-SNAPSHOT_42/TestAndroidProject-release-market-1.0.1-SNAPSHOT_42.apk").exists())
    }

    @Test
    void testBuildAndPrepareNonVariantedMailMessage() {
        runGradleNoVariants('cleanRelease', 'updateProject' ,'buildAll')
        runGradleNoVariants('prepareImageMontage', 'prepareMailMessage')
        assertTrue(new File("testProjects/android-novariants/ota/asdlakjljsdTest/1.0.1-SNAPSHOT_42/file_index.html").exists())
        assertTrue(new File("testProjects/android-novariants/ota/asdlakjljsdTest/1.0.1-SNAPSHOT_42/icon.png").exists())
        assertTrue(new File("testProjects/android-novariants/ota/asdlakjljsdTest/1.0.1-SNAPSHOT_42/index.html").exists())
        assertTrue(new File("testProjects/android-novariants/ota/asdlakjljsdTest/1.0.1-SNAPSHOT_42/plain_file_index.html").exists())
        assertTrue(new File("testProjects/android-novariants/ota/asdlakjljsdTest/1.0.1-SNAPSHOT_42/qrcode-TestAndroidProject-1.0.1-SNAPSHOT_42.png").exists())
        assertTrue(new File("testProjects/android-novariants/ota/asdlakjljsdTest/1.0.1-SNAPSHOT_42/TestAndroidProject-debug-1.0.1-SNAPSHOT_42.apk").exists())
        assertTrue(new File("testProjects/android-novariants/ota/asdlakjljsdTest/1.0.1-SNAPSHOT_42/TestAndroidProject-release-1.0.1-SNAPSHOT_42.apk").exists())
    }

    @Test
    void testBuildDocumentationZip() {
        runGradle('buildDocumentationZip')
        File file = new File('testProjects/android/tmp/TestAndroidProject-1.0.1-SNAPSHOT_42-doc.zip')
        assertTrue(file.exists())
        assertTrue(file.size() > 30000)
    }

    @Test
    void testBuildSourcesZip() {
        runGradle('buildSourcesZip')
        File file = new File('testProjects/android/tmp/TestAndroidProject-1.0.1-SNAPSHOT_42-src.zip')
        assertTrue(file.exists())
        assertTrue(file.size() > 30000)
    }

    @Test
    void testRunCleanAVD() {
        runGradle('cleanAVD')
        File avdsDirectory = new File('testProjects/android/avds')
        assertFalse(avdsDirectory.exists())
    }

    @Test
    void testRunAndroidCreateAVD() {
        runGradle('cleanAVD', 'createAVD')
        def files = [
            'config.ini',
            'sdcard.img',
            'snapshots.img',
            'userdata.img'
        ]
        File avdsDirectory = new File('testProjects/android/avds')
        assertTrue(avdsDirectory.exists())
        files.each {
            assertTrue(it, new File(avdsDirectory,it).exists())
        }
    }
}
