package ajk.gradle.service

import ajk.gradle.ConsulExtension
import org.gradle.util.Configurable

import static org.gradle.util.ConfigureUtil.configure

class DeregisterServiceExtension implements Configurable<DeregisterServiceExtension> {
    private ConsulExtension consulExtension

    DeregisterServiceExtension(ConsulExtension consulExtension) {
        this.consulExtension = consulExtension
    }

    @Override
    DeregisterServiceExtension configure(Closure closure) {
        configure(closure, new DeregisterServiceAction(consulExtension)).execute()

        return this
    }
}
