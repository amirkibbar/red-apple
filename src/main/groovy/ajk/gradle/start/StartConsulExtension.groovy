package ajk.gradle.start

import ajk.gradle.ConsulExtension
import org.gradle.api.Project
import org.gradle.util.Configurable

import static org.gradle.util.ConfigureUtil.configure

class StartConsulExtension implements Configurable<StartConsulExtension> {
    private Project project
    private ConsulExtension consulExtension

    StartConsulExtension(Project project, ConsulExtension consulExtension) {
        this.consulExtension = consulExtension
        this.project = project
    }

    @Override
    StartConsulExtension configure(Closure closure) {
        configure(closure, new StartAction(project, consulExtension)).execute()

        return this
    }
}
