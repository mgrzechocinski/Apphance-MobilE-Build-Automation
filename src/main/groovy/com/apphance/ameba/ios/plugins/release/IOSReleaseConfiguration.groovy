package com.apphance.ameba.ios.plugins.release

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.apphance.ameba.plugins.release.AmebaArtifact;

class IOSReleaseConfiguration {
    Map<String,AmebaArtifact> distributionZipFiles = [:]
    Map<String,AmebaArtifact> dSYMZipFiles = [:]
    Map<String,AmebaArtifact> ipaFiles = [:]
    Map<String,AmebaArtifact> manifestFiles = [:]
    Map<String,AmebaArtifact> mobileProvisionFiles = [:]
    AmebaArtifact otaIndexFile
    AmebaArtifact fileIndexFile
    AmebaArtifact plainFileIndexFile
    Map<String, AmebaArtifact> foneMonkeyTestResultFiles = [:]
    Map<String,AmebaArtifact> dmgImageFiles = [:]
    Map<String, HashMap<String, Collection<AmebaArtifact>>> monkeyTestImages = [:]

    @Override
    public String toString() {
        return this.getProperties()
    }
}