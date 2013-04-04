package com.apphance.ameba.plugins.android.analysis.tasks

import org.gradle.api.Project
import org.gradle.api.logging.Logging

class AndroidAnalysisMixin {

    private l = Logging.getLogger(getClass())

    public URL getResourceUrl(Project project, String resourceName) {
        l.info("Reading resource $resourceName")

        URL baseUrl = project.file('config/analysis').toURI().toURL()

        //TODO convention will be switched to AndroidAnalysisConfiguration when anroid configuration is implemented
//        if (convention.baseAnalysisConfigUrl != null) {
//            baseUrl = new URL(convention.baseAnalysisConfigUrl)
//            l.info("Base config url $baseUrl")
//        }

        URL targetURL = new URL(baseUrl, resourceName)
        if (targetURL.getProtocol() != 'file') {
            l.info("Downloading file from $targetURL")
            try {
                targetURL.getContent() // just checking if we can read it
                return targetURL
            } catch (IOException e) {
                l.warn("Exception $e while reading from $targetURL. Falling back")
//                targetURL = new URL(configUrl, resourceName)//TODO what if URL comes from configuration and fails?
            }
        }
        l.info("Reading resource from file $targetURL")
        if (!(new File(targetURL.toURI()).exists())) {
            def url = this.class.getResource(resourceName)
            l.info("Reading resource from internal $url as file $targetURL not found")
            return url
        }
        return targetURL
    }
}
