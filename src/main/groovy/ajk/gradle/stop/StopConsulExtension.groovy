package ajk.gradle.stop

import ajk.gradle.ConsulExtension
import org.gradle.api.Project
import org.gradle.util.Configurable

import static org.gradle.util.ConfigureUtil.configure

class StopConsulExtension implements Configurable<StopConsulExtension> {
    private Project project
    private ConsulExtension consulExtension

    StopConsulExtension(Project project, ConsulExtension consulExtension) {
        this.project = project
        this.consulExtension = consulExtension
    }

    @Override
    StopConsulExtension configure(Closure closure) {
        configure(closure, new StopAction(project, consulExtension)).execute()

        return this
    }
}
