package ajk.gradle.registerservice

import ajk.gradle.ConsulExtension
import org.gradle.api.Project
import org.gradle.util.Configurable

import static org.gradle.util.ConfigureUtil.configure

class RegisterServiceExtension implements Configurable<RegisterServiceExtension> {
    private Project project
    private ConsulExtension consulExtension

    RegisterServiceExtension(Project project, ConsulExtension consulExtension) {
        this.consulExtension = consulExtension
        this.project = project
    }

    @Override
    RegisterServiceExtension configure(Closure closure) {
        configure(closure, new RegisterServiceAction(project, consulExtension)).execute()

        return this
    }
}