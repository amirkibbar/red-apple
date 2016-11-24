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
        println "${ConsulPlugin.CYAN}* consul:$ConsulPlugin.NORMAL stopping consul"
        // Assume default LINUX
        def sout = new StringBuilder(), serr = new StringBuilder()
        def proc
        if (isFamily(FAMILY_WINDOWS)) {
            proc = [
                    "taskkill",
                    "/F",
                    "/T",
                    "/IM",
                    "consul.exe"
            ].execute()
        }
        else {
            proc = [
                    "pkill",
                    "-f",
                    "consul"
            ].execute()
        }
        proc.consumeProcessOutput(sout, serr)
        proc.waitForOrKill(10 * 1000)
        println "${ConsulPlugin.CYAN}* consul: $ConsulPlugin.GREEN$sout$ConsulPlugin.RED$serr$ConsulPlugin.NORMAL "
    }
}
