package com.apphance.flow.plugins.android.apphance.tasks

import com.apphance.flow.configuration.android.variants.AndroidVariantConfiguration
import com.apphance.flow.configuration.apphance.ApphanceMode
import com.apphance.flow.util.FlowUtils
import com.google.common.io.Files
import groovy.util.slurpersupport.GPathResult
import org.apache.commons.io.FileUtils
import spock.lang.Specification

import static android.Manifest.permission.*
import static org.apache.commons.io.FileUtils.copyFile

@Mixin(FlowUtils)
class AddApphanceToAndroidSpec extends Specification {

    def androidVariantConf = new AndroidVariantConfiguration('test variant')
    def variantDir = Files.createTempDir()
    AddApphanceToAndroid addApphanceToAndroid

    def setup() {
        variantDir.deleteOnExit()
        FileUtils.copyDirectory(new File('testProjects/android/android-basic'), variantDir)

        androidVariantConf.apphanceMode.value = ApphanceMode.QA
        androidVariantConf.apphanceAppKey.value = 'TestKey'
        androidVariantConf.variantDir.value = variantDir

        addApphanceToAndroid = new AddApphanceToAndroid(androidVariantConf)
    }

    def 'test checkIfApphancePresent no apphance'() {
        expect:
        !addApphanceToAndroid.checkIfApphancePresent()
    }

    def 'test checkIfApphancePresent startNewSession'() {
        given:
        copyFile(new File('src/test/resources/com/apphance/flow/android/TestActivity.java.txt'), new File(variantDir,
                'src/com/apphance/flowTest/android/TestActivity.java'))

        expect:
        addApphanceToAndroid.checkIfApphancePresent()
    }

    def 'test checkIfApphancePresent apphance jar'() {
        given:
        new File(variantDir, 'libs/apphance-library.jar').createNewFile()

        expect:
        addApphanceToAndroid.checkIfApphancePresent()
    }

    def 'test checkIfApphancePresent apphance activity'() {
        given:
        copyFile(new File('src/test/resources/com/apphance/flow/android/AndroidManifestWithProblemActivity.xml'), new File(variantDir, 'AndroidManifest.xml'))

        expect:
        addApphanceToAndroid.checkIfApphancePresent()
    }

    def 'test addReportActivityToManifest'() {
        given:
        addApphanceToAndroid.addReportActivityToManifest()
        def manifestFile = new File(variantDir, 'AndroidManifest.xml')
        def manifest = new XmlSlurper().parse(manifestFile).declareNamespace(android: 'http://schemas.android.com/apk/res/android');

        expect:
        manifest.application.activity.find { GPathResult it ->
            ['android:name': 'com.apphance.android.ui.ProblemActivity',
                    'android:configChanges': 'orientation',
                    'android:launchMode': 'singleInstance',
                    'android:process': 'com.utest.apphance.reporteditor'].every { key, val ->
                it."@$key".text() == val
            }
        }
    }

    def 'test addPermisions'() {
        given:
        addApphanceToAndroid.addPermisions()
        def manifestFile = new File(variantDir, 'AndroidManifest.xml')
        def manifest = new XmlSlurper().parse(manifestFile).declareNamespace(android: 'http://schemas.android.com/apk/res/android');

        expect:
        manifest.'uses-permission'.size() == 9
        manifest.'uses-permission'.collect { it.'@android:name'.text() } ==
                [INTERNET, CHANGE_WIFI_STATE, READ_PHONE_STATE, GET_TASKS, ACCESS_WIFI_STATE, ACCESS_NETWORK_STATE, ACCESS_COARSE_LOCATION,
                        ACCESS_FINE_LOCATION, BLUETOOTH]
    }

    def 'test addStartNewSessionToAllMainActivities'() {
        given:
        File mainActivity = new File(variantDir, 'src/com/apphance/flowTest/android/TestActivity.java')
        def appKeyCond = { mainActivity.text.contains('public static final String APP_KEY = "TestKey";') }
        def startNewSessionCond = { mainActivity.text.contains('Apphance.startNewSession(this, APP_KEY, Mode.QA);') }

        expect:
        mainActivity.exists()
        !appKeyCond()
        !startNewSessionCond()

        when:
        addApphanceToAndroid.addStartNewSessionToAllMainActivities()

        then:
        appKeyCond()
        startNewSessionCond()
    }

    def 'test addStartStopInvocations'() {
        given:
        File activity = new File(variantDir, 'src/com/apphance/flowTest/android/TestActivity.java')

        when:
        addApphanceToAndroid.addStartStopInvocations(activity)

        then:
        activity.text.contains('Apphance.onStart(this);')
        activity.text.contains('Apphance.onStop(this);')
        println activity.text

        removeWhitespace(activity.text).contains(removeWhitespace("""
            |protected void onStart() {
            |    super.onStart();
            |    Apphance.onStart(this);
            |}
            |""".stripMargin()))
    }
}
