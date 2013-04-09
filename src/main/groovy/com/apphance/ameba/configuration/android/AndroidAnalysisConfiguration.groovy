package com.apphance.ameba.configuration.android

import com.apphance.ameba.configuration.Configuration
import com.apphance.ameba.configuration.properties.URLProperty

import javax.inject.Inject

@com.google.inject.Singleton
class AndroidAnalysisConfiguration extends Configuration {

    String configurationName = 'Android Analysis Configuration'
    private boolean enabled = false

    private AndroidConfiguration androidConfiguration

    @Inject AndroidAnalysisConfiguration(AndroidConfiguration androidConfiguration) {
        this.androidConfiguration = androidConfiguration
    }

    @Override
    boolean isEnabled() {
        this.@enabled && androidConfiguration.enabled
    }

    @Override
    void setEnabled(boolean enabled) {
        this.@enabled = enabled
    }

    def analysisConfigUrl = new URLProperty(
            name: 'android.analysis.config.url',
            message: 'Android analysis config URL',
    )
}
