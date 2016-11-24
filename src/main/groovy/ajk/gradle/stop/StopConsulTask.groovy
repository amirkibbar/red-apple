package ajk.gradle.stop

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction

class StopConsulTask extends DefaultTask {
    @Input
    @Optional
    File consulDir

    @TaskAction
    void stopConsul() {
        new StopAction(project, consulDir).execute()
    }
}
