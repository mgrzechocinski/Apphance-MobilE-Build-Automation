package com.apphance.ameba.unit

import com.apphance.ameba.util.file.FileManager
import org.junit.Test

import static org.junit.Assert.assertArrayEquals

class ReadingPackageTest {
    @Test
    public void testReadPackages() {
        File f = new File('src/test/groovy')
        def currentPackage = []
        FileManager.findAllPackages('', f, currentPackage)
        currentPackage.sort()
        String[] packagesList = (String[]) [
                'com.apphance.ameba',
                'com.apphance.ameba.apphance.android',
                'com.apphance.ameba.applyPlugins.android',
                'com.apphance.ameba.applyPlugins.ios',
                'com.apphance.ameba.conventions',
                'com.apphance.ameba.detection',
                'com.apphance.ameba.plugins',
                'com.apphance.ameba.runBuilds.android',
                'com.apphance.ameba.runBuilds.ios',
                'com.apphance.ameba.unit',
                'com.apphance.ameba.unit.android',
                'com.apphance.ameba.unit.apphance.android',
                'com.apphance.ameba.unit.ios',
                'com.apphance.ameba.util'
        ].sort()
        assertArrayEquals(currentPackage.toString(), packagesList, (String[]) currentPackage)
    }
}
