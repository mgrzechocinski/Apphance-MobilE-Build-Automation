package com.apphance.ameba.configuration

import javax.inject.Inject

import static com.apphance.ameba.detection.ProjectType.ANDROID

class AndroidConfiguration extends Configuration {

    @Inject
    ProjectConfiguration conf

    def enabled = false
    int order = 1

    String configurationName = "Android configuration"

    @Override
    boolean isEnabled() {
        enabled && conf.enabled && conf.type != null && conf.type == ANDROID
    }

    @Override
    void setEnabled(boolean enabled) {
        this.enabled = enabled
    }

    def sdkDir = new Prop<File>(name: 'android.sdk.dir', message: 'Android SDK directory')
    def minSdkTargetName = new Prop<String>(name: 'android.min.sdk.target.name', message: 'Android min SDK target name')

}
