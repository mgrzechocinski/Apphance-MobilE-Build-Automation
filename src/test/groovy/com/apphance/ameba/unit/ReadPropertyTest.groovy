package com.apphance.ameba.unit;

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import static org.junit.Assert.*;

import org.junit.Test;

import com.apphance.ameba.PropertyCategory;
import com.apphance.ameba.plugins.projectconfiguration.ProjectBaseProperty

public class ReadPropertyTest {

    @Test
    public void testReadProperty() {
        use(PropertyCategory) {
            final ProjectBuilder builder = ProjectBuilder.builder()
            final Project project = builder.build()
            project['testProperty'] = '1000'
            assertEquals('1000', project.readProperty('testProperty'))
        }
    }

    @Test
    public void testReadPropertyWithDefault() {
        use(PropertyCategory) {
            final ProjectBuilder builder = ProjectBuilder.builder()
            final Project project = builder.build()
            assertEquals('200', project.readProperty('testProperty','200'))
        }
    }

    @Test
    public void testReadMissingProperty() {
        use(PropertyCategory) {
            final ProjectBuilder builder = ProjectBuilder.builder()
            final Project project = builder.build()
            assertNull(project.readProperty('testProperty'))
        }
    }

    @Test
    public void testReadEnumProperty() {
        use(PropertyCategory) {
            final ProjectBuilder builder = ProjectBuilder.builder()
            final Project project = builder.build()
            project[ProjectBaseProperty.PROJECT_COUNTRY.propertyName] = 'aaaa'
            assertEquals('aaaa',project.readProperty(ProjectBaseProperty.PROJECT_COUNTRY))
        }
    }
}