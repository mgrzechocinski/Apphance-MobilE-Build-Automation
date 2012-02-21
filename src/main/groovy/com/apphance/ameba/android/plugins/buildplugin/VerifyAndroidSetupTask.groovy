package com.apphance.ameba.android.plugins.buildplugin

import org.gradle.api.GradleException
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.tasks.TaskAction

import com.apphance.ameba.AbstractVerifySetupTask
import com.apphance.ameba.PropertyCategory
import com.apphance.ameba.android.AndroidCommandParser;
import com.apphance.ameba.android.AndroidProjectConfiguration;
import com.apphance.ameba.android.AndroidProjectConfigurationRetriever;


class VerifyAndroidSetupTask extends AbstractVerifySetupTask {
    Logger logger = Logging.getLogger(VerifyAndroidSetupTask.class)

    AndroidProjectConfigurationRetriever retriever = new AndroidProjectConfigurationRetriever()
    AndroidProjectConfiguration androidConf
    VerifyAndroidSetupTask() {
        super(AndroidProjectProperty.class)
        androidConf = retriever.getAndroidProjectConfiguration(project)
        this.dependsOn(project.readAndroidProjectConfiguration, project.verifyBaseSetup)
    }

    @TaskAction
    void verifySetup() {
        use (PropertyCategory) {
            Properties projectProperties = readProperties()
            AndroidProjectProperty.each{ checkProperty(projectProperties, it) }
            checkVariant(projectProperties)
            checkMinSdkTarget(projectProperties)
            allPropertiesOK()
        }
    }

    void checkVariant(Properties properties) {
        use (PropertyCategory) {
            String mainVariant = project.readProperty(AndroidProjectProperty.MAIN_VARIANT)
            if (mainVariant != null && !mainVariant.empty && !androidConf.variants.contains(mainVariant)) {
                throw new GradleException("""The main variant in ${AndroidProjectProperty.MAIN_VARIANT.propertyName}: ${mainVariant} can only be one of ${androidConf.variants}""")
            }
        }
    }

    void checkMinSdkTarget(Properties properties) {
        use (PropertyCategory) {
            String target = project.readProperty(AndroidProjectProperty.MIN_SDK_TARGET)
            List targets = AndroidCommandParser.getTargets()
            if (target != null && !target.empty && !targets.contains(target)) {
                throw new GradleException("""The min sdk target ${AndroidProjectProperty.MIN_SDK_TARGET.propertyName}: ${target} can only be one of ${targets}""")
            }
        }
    }
}
