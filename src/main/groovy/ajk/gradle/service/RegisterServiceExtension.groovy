package ajk.gradle.service

import ajk.gradle.ConsulExtension
import org.gradle.util.Configurable

import static org.gradle.util.ConfigureUtil.configure

class RegisterServiceExtension implements Configurable<RegisterServiceExtension> {
    private ConsulExtension consulExtension

    RegisterServiceExtension(ConsulExtension consulExtension) {
        this.consulExtension = consulExtension
    }

    @Override
    RegisterServiceExtension configure(Closure closure) {
        configure(closure, new RegisterServiceAction(consulExtension)).execute()

        return this
    }
}