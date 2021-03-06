package com.apphance.flow.configuration.ios

import com.apphance.flow.configuration.AbstractConfiguration
import com.apphance.flow.configuration.ios.variants.IOSVariantsConfiguration
import com.apphance.flow.configuration.properties.ListStringProperty
import com.apphance.flow.configuration.properties.StringProperty
import com.google.inject.Singleton

import javax.inject.Inject

@Singleton
class IOSFrameworkConfiguration extends AbstractConfiguration {

    String configurationName = 'iOS Framework Configuration'
    private boolean enabledInternal = false

    @Inject IOSConfiguration conf
    @Inject IOSVariantsConfiguration variantsConf
    @Inject IOSReleaseConfiguration releaseConf

    @Inject
    @Override
    void init() {
        super.init()
    }

    @Override
    boolean isEnabled() {
        conf.enabled && enabledInternal
    }

    @Override
    void setEnabled(boolean enabled) {
        enabledInternal = enabled
    }

    def variantName = new StringProperty(
            name: 'ios.framework.variant',
            message: 'Variant to build framework project with',
            possibleValues: { variantsConf.variantsNames.value },
            validator: { it in variantsConf.variantsNames.value },
            required: { true }
    )

    def version = new StringProperty(
            name: 'ios.framework.version',
            message: 'Version of framework (usually single alphabet letter A)',
            defaultValue: { 'A' }
    )

    def headers = new ListStringProperty(
            name: 'ios.framework.headers',
            message: 'List of headers (coma separated) that should be copied to the framework'
    )

    def resources = new ListStringProperty(
            name: 'ios.framework.resources',
            message: 'List of resources (coma separated) that should be copied to the framework'
    )

    @Override
    boolean canBeEnabled() {
        !releaseConf.enabled
    }

    @Override
    String explainDisabled() {
        "'$configurationName' cannot be enabled because '${releaseConf.configurationName}' is enabled and those plugins are mutually exclusive.\n"
    }
}
