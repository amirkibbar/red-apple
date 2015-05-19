package ajk.gradle.service

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction

class ListServicesTask extends DefaultTask {
    @Input
    @Optional
    Integer httpPort

    @TaskAction
    void list() {
        new ListServicesAction(httpPort ?: 8500).execute()
    }
}
