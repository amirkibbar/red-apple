package ajk.gradle.check

import ajk.gradle.ConsulExtension
import org.gradle.util.Configurable

import static org.gradle.util.ConfigureUtil.configure

class DeregisterCheckExtension implements Configurable<DeregisterCheckExtension>{
    private ConsulExtension consulExtension

    DeregisterCheckExtension(ConsulExtension consulExtension) {
        this.consulExtension = consulExtension
    }

    @Override
    DeregisterCheckExtension configure(Closure closure) {
        configure(closure, new DeregisterCheckAction(consulExtension)).execute()

        return this
    }
}
