package com.apphance.ameba.configuration.properties

class FileProperty extends AbstractProperty<File> {

    @Override
    void setValue(String value) {
        if (value)
            this.@value = new File(value)
    }

    @Override
    Closure<Boolean> getValidator() {
        if (!super.validator) {
            return super.validator
        }
        return { true }
    }
}
