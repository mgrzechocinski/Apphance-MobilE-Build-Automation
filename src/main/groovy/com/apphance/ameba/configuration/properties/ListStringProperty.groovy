package com.apphance.ameba.configuration.properties

class ListStringProperty extends AbstractProperty<List<String>> {

    private static String SEPARATOR = ','

    @Override
    void setValue(String value) {
        if (value && value.trim())
            this.@value = value.trim().split(SEPARATOR)*.trim()
    }

    Closure<String> persistentForm = { value?.join(SEPARATOR) ?: '' }

    static String getSEPARATOR() {
        SEPARATOR
    }

}