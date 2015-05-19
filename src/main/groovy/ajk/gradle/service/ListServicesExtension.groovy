package ajk.gradle.service

import ajk.gradle.ConsulExtension
import org.gradle.util.Configurable

import static org.gradle.util.ConfigureUtil.configure

class ListServicesExtension implements Configurable<ListServicesExtension> {
    private ConsulExtension consulExtension

    ListServicesExtension(ConsulExtension consulExtension) {
        this.consulExtension = consulExtension
    }

    @Override
    ListServicesExtension configure(Closure closure) {
        configure(closure, new ListServicesAction(consulExtension)).execute()

        return this
    }
}
