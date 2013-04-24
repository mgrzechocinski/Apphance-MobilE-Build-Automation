package com.apphance.ameba.executor

import com.apphance.ameba.executor.command.CommandExecutor
import spock.lang.Specification

class AndroidExecutorSpec extends Specification {

    CommandExecutor commandExecutor = Mock()
    File file = Mock()
    AndroidExecutor androidExecutor = new AndroidExecutor(commandExecutor)

    def 'test updateProject method'() {
        when: androidExecutor.updateProject(file, 'android-8', 'sample-name')
        then: 1 * commandExecutor.executeCommand({ it.commandForExecution.join(' ') == 'android update project -p . -t android-8 -n sample-name -s' })
    }

    def 'test listAvd'() {
        when: androidExecutor.listAvd(file)
        then: 1 * commandExecutor.executeCommand({ it.commandForExecution.join(' ') == 'android list avd -c' })
    }

    def 'test run method'() {
        when: androidExecutor.run(file, "command")
        then: 1 * commandExecutor.executeCommand({ it.commandForExecution.join(' ') == 'android command' })
    }

    def 'test list targets'() {
        given:
        def ce = Mock(CommandExecutor)
        ce.executeCommand(_) >> targets.split('\n')

        and:
        def ae = new AndroidExecutor(ce)

        when:
        def output = ae.listTarget(Mock(File))

        then:
        ['Google Inc.:Google APIs:3', 'Google Inc.:Google APIs:4', 'android-17', 'android-3', 'android-4'] == output
    }

    def 'test list skins'() {
        given:
        def ce = Mock(CommandExecutor)
        ce.executeCommand(_) >> targets.split('\n')

        and:
        def ae = new AndroidExecutor(ce)

        when:
        def output = ae.listSkinsForTarget(Mock(File), 'android-3')

        then:
        ['HVGA', 'HVGA-L', 'HVGA-P', 'QVGA-L', 'QVGA-P'] == output
    }

    def 'test default skin for target'() {
        given:
        def ce = Mock(CommandExecutor)
        ce.executeCommand(_) >> targets.split('\n')

        and:
        def ae = new AndroidExecutor(ce)

        expect:
        skin == ae.defaultSkinForTarget(Mock(File), target)

        where:
        skin      | target
        'HVGA'    | 'android-3'
        'WVGA800' | 'android-4'
        'WVGA800' | 'Google Inc.:Google APIs:4'
    }

    def targets = "Available Android targets:\n" +
            "----------\n" +
            "id: 1 or \"android-3\"\n" +
            "     Name: Android 1.5\n" +
            "     Type: Platform\n" +
            "     API level: 3\n" +
            "     Revision: 4\n" +
            "     Skins: HVGA (default), HVGA-L, HVGA-P, QVGA-L, QVGA-P\n" +
            "     ABIs : armeabi\n" +
            "----------\n" +
            "id: 2 or \"Google Inc.:Google APIs:3\"\n" +
            "     Name: Google APIs\n" +
            "     Type: Add-On\n" +
            "     Vendor: Google Inc.\n" +
            "     Revision: 3\n" +
            "     Description: Android + Google APIs\n" +
            "     Based on Android 1.5 (API level 3)\n" +
            "     Libraries:\n" +
            "      * com.google.android.maps (maps.jar)\n" +
            "          API for Google Maps\n" +
            "     Skins: QVGA-P, HVGA-L, HVGA (default), QVGA-L, HVGA-P\n" +
            "     ABIs : armeabi\n" +
            "----------\n" +
            "id: 3 or \"android-4\"\n" +
            "     Name: Android 1.6\n" +
            "     Type: Platform\n" +
            "     API level: 4\n" +
            "     Revision: 3\n" +
            "     Skins: HVGA, QVGA, WVGA800 (default), WVGA854\n" +
            "     ABIs : armeabi\n" +
            "----------\n" +
            "id: 4 or \"Google Inc.:Google APIs:4\"\n" +
            "     Name: Google APIs\n" +
            "     Type: Add-On\n" +
            "     Vendor: Google Inc.\n" +
            "     Revision: 2\n" +
            "     Description: Android + Google APIs\n" +
            "     Based on Android 1.6 (API level 4)\n" +
            "     Libraries:\n" +
            "      * com.google.android.maps (maps.jar)\n" +
            "          API for Google Maps\n" +
            "     Skins: WVGA854, HVGA, WVGA800 (default), QVGA\n" +
            "     ABIs : armeabi\n" +
            "----------\n" +
            "id: 24 or \"android-17\"\n" +
            "     Name: Android 4.2\n" +
            "     Type: Platform\n" +
            "     API level: 17\n" +
            "     Revision: 1\n" +
            "     Skins: HVGA, QVGA, WQVGA400, WQVGA432, WSVGA, WVGA800 (default), WVGA854, WXGA720, WXGA800, WXGA800-7in\n" +
            "     ABIs : armeabi-v7a, mips"
}
