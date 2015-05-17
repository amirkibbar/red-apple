package ajk.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction

class StartConsulTask extends DefaultTask {
    @Input
    @Optional
    int httpPort

    @Input
    @Optional
    int dnsPort

    @Input
    @Optional
    String version

    @Input
    @Optional
    File consulDir

    @TaskAction
    void startConsul() {
        new StartConsulAction(project, httpPort, dnsPort, version, consulDir).execute()
    }
}
