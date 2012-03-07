package com.apphance.ameba.wp7.plugins.buildplugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

import com.apphance.ameba.AmebaCommonBuildTaskGroups
import com.apphance.ameba.ProjectConfiguration
import com.apphance.ameba.ProjectHelper
import com.apphance.ameba.PropertyCategory
import com.apphance.ameba.wp7.Wp7ProjectConfiguration


/**
 * Plugin for various Windows Phone 7 related tasks.
 *
 */
class Wp7Plugin implements Plugin<Project> {

	static Logger logger = Logging.getLogger(Wp7Plugin.class)

	ProjectHelper projectHelper
	ProjectConfiguration conf
	Wp7ProjectConfiguration wp7Conf;

	public void apply(Project project) {

		use (PropertyCategory) {
			this.projectHelper = new ProjectHelper()
			this.conf = project.getProjectConfiguration()
			prepareBuildAllTask(project)
			prepareCleanTask(project)
			prepareCopyProject(project)

			project.prepareSetup.prepareSetupOperations << new PrepareWp7SetupOperation()
			project.verifySetup.verifySetupOperations << new VerifyWp7SetupOperation()
			project.showSetup.showSetupOperations << new ShowWp7PropertiesOperation()

		}
	}


	private prepareReadWp7ProjectConfigurationTask(Project project) {
		def task = project.task('readWp7ProjectConfiguration')
		task.group = AmebaCommonBuildTaskGroups.AMEBA_CONFIGURATION
		task.description = 'Reads Wp7 project configuration'

		task << {
			use (PropertyCategory) {
			}
		}

		project.readProjectConfiguration.dependsOn(task)
	}

	void prepareBuildAllTask(Project project) {
		def task = project.task('buildAll')
		task.group = AmebaCommonBuildTaskGroups.AMEBA_BUILD
		task.description = 'Builds all target/configuration combinations and produces all artifacts'

		logger.lifecycle("Building all builds")

		task << {
			projectHelper.executeCommand(project, ['MSBuild'])
		}
	}

	private void prepareCleanTask(Project project) {
		def task = project.task('clean')
		task.description = "Cleans the project"
		task.group = AmebaCommonBuildTaskGroups.AMEBA_BUILD
		task << {
			projectHelper.executeCommand(project, ['MSBuild', "/target:Clean"])
		}
		task.dependsOn(project.cleanConfiguration)
	}


	void prepareCopyProject(Project project) {
		def task = project.task('copySources')
		task.description = "Copies all sources to selected directory"
		task.group = AmebaCommonBuildTaskGroups.AMEBA_APPHANCE_SERVICE
		task << {

			File srcDir = getProjectDir(project)
			File destDir = new File(getProjectDir(project), "tmpApphance/")
			destDir.deleteDir()

			new AntBuilder().delete(dir: destDir)
			new AntBuilder().copy(toDir : destDir, verbose:true) {
				fileset(dir : srcDir) {
					exclude(name: "${destDir.absolutePath}/**/*")
				}
			}

		}
	}

	static public final String DESCRIPTION =
	"""This is the main windows phone build plugin.

	The plugin provides all the tasks needed to build windows phone application.
	Besides tasks explained below, the plugin prepares build-* and install-*
	tasks which are dynamically created, based on variants available. In
	case the build has no variants, the only available builds are Debug and Release.
	In case of variants, there is one build and one task created for every variant.
	"""
}
