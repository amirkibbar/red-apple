package ajk.gradle

import org.gradle.api.Project

import static ajk.gradle.ConsulPlugin.CYAN
import static ajk.gradle.ConsulPlugin.NORMAL
import static ajk.gradle.ConsulPlugin.RED
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
            println "${CYAN}* consul:$RED for the time being this plugin is only supported on Windows$NORMAL"
            throw new UnsupportedOperationException();
        }

        File f = File.createTempFile("killThemAll_", ".bat")
        f.deleteOnExit()

        f << """
wmic process where (name like "%%consul%%") delete
"""

        println "${CYAN}* consul:$NORMAL stopping consul"
        [f.getAbsolutePath()].execute().waitForOrKill(10 * 1000)

        f.delete()
    }
}
