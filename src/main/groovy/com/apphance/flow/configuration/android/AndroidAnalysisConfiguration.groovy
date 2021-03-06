package com.apphance.flow.configuration.android

import com.apphance.flow.configuration.AbstractConfiguration
import com.apphance.flow.configuration.properties.URLProperty

import javax.inject.Inject

@com.google.inject.Singleton
class AndroidAnalysisConfiguration extends AbstractConfiguration {

    String configurationName = 'Android Analysis Configuration'
    private boolean enabledInternal = false

    @Inject AndroidConfiguration conf

    @Override
    boolean isEnabled() {
        enabledInternal && conf.isEnabled()
    }

    @Override
    void setEnabled(boolean enabled) {
        enabledInternal = enabled
    }

    def analysisConfigUrl = new URLProperty(
            name: 'android.analysis.config.url',
            message: 'Android analysis config URL',
    )

    @Override
    void checkProperties() {
        if (analysisConfigUrl.isSet()) {
            check !checkException { analysisConfigUrl.value }, "Property '${analysisConfigUrl.name}' is not valid! Should be valid URL address!"
        }
    }
}

