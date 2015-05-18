package ajk.gradle.service

import ajk.gradle.ConsulApiClient
import ajk.gradle.ConsulExtension
import groovy.json.JsonSlurper
import org.apache.http.client.fluent.Executor

import static ajk.gradle.ConsulPlugin.CYAN
import static ajk.gradle.ConsulPlugin.NORMAL
import static org.apache.http.client.fluent.Request.Get

class DeregisterServiceAction extends ConsulApiClient {
    String id

    String tag

    DeregisterServiceAction(ConsulExtension consulExtension) {
        this.consulHostname = "localhost"
        this.consulPort = consulExtension.httpPort
    }

    void execute() {
        if (id == null && tag == null) {
            throw new IllegalArgumentException("you must provide either an id or a tag")
        }

        if (id != null) {
            // deregister by id
            println "${CYAN}* consul:$NORMAL deregistering service $id"

            consulApi { Executor http, String consulAddress ->
                http.execute(Get("http://$consulAddress/v1/agent/service/deregister/$id")).discardContent()
            }
        } else {
            // deregister by matching tag value
            def services = new JsonSlurper().parseText((String) consulApi { Executor http, String consulAddress ->
                http.execute(Get("http://$consulAddress/v1/agent/services")).returnContent().asString()
            })

            services.findAll { it.value.Tags?.contains(tag) }
                    .each {
                consulApi { Executor http, String consulAddress ->
                    println "${CYAN}* consul:$NORMAL deregistering service $it.key"
                    http.execute(Get("http://$consulAddress/v1/agent/service/deregister/$it.key")).discardContent()
                }
            }
        }
    }
}
