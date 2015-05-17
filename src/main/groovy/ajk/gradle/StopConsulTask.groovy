package ajk.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction

import static ajk.gradle.ConsulPlugin.CYAN
import static ajk.gradle.ConsulPlugin.NORMAL
import static ajk.gradle.ConsulPlugin.RED
import static org.apache.tools.ant.taskdefs.condition.Os.FAMILY_WINDOWS
import static org.apache.tools.ant.taskdefs.condition.Os.isFamily

class StopConsulTask extends DefaultTask {
    @Input
    @Optional
    File consulDir

    @TaskAction
    void stopConsul() {
        new StopAction(project, consulDir)
    }
}
