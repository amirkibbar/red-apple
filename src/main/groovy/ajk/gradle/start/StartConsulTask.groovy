package ajk.gradle.start

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

    @Input
    @Optional
    File dataDir

    @Input
    @Optional
    File configDir

    @TaskAction
    void startConsul() {
        new StartAction(project, httpPort, dnsPort, version, consulDir, configDir, dataDir).execute()
    }
}
