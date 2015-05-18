package ajk.gradle.stop

import ajk.gradle.ConsulExtension
import ajk.gradle.ConsulPlugin
import org.gradle.api.Project

import static org.apache.tools.ant.taskdefs.condition.Os.FAMILY_WINDOWS
import static org.apache.tools.ant.taskdefs.condition.Os.isFamily

class StopAction {
    Project project

    File consulDir

    AntBuilder ant

    StopAction(Project project, ConsulExtension consulExtension) {
        this(project, consulExtension.consulDir)
    }

    StopAction(Project project, File consulDir) {
        this.project = project
        this.consulDir = consulDir
        this.ant = project.ant
    }

    void execute() {
        if (!isFamily(FAMILY_WINDOWS)) {
            println "${ConsulPlugin.CYAN}* consul:$ConsulPlugin.RED for the time being this plugin is only supported on Windows$ConsulPlugin.NORMAL"
            throw new UnsupportedOperationException();
        }

        File f = File.createTempFile("killThemAll_", ".bat")
        f.deleteOnExit()

        f << """
wmic process where (name like "%%consul%%") delete
"""

        println "${ConsulPlugin.CYAN}* consul:$ConsulPlugin.NORMAL stopping consul"
        [f.getAbsolutePath()].execute().waitForOrKill(10 * 1000)

        f.delete()
    }
}
