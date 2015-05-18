package ajk.gradle.check

import ajk.gradle.ConsulApiClient
import ajk.gradle.ConsulExtension
import groovy.json.JsonSlurper
import org.apache.http.client.fluent.Executor

import static ajk.gradle.ConsulPlugin.CYAN
import static ajk.gradle.ConsulPlugin.NORMAL
import static org.apache.http.client.fluent.Request.Get


class DeregisterCheckAction extends ConsulApiClient {
    String id

    DeregisterCheckAction(ConsulExtension consulExtension) {
        this.consulHostname = "localhost"
        this.consulPort = consulExtension.httpPort
    }

    void execute() {
        if (id == null) {
            throw new IllegalArgumentException("you must provide an id")
        }

        println "${CYAN}* consul:$NORMAL deregistering checks that match $id"

        def checks = new JsonSlurper().parseText((String) consulApi { Executor http, String consulAddress ->
            http.execute(Get("http://$consulAddress/v1/agent/checks")).returnContent().asString()
        })

        checks.keySet().findAll { it.contains(id) }.each { checkId ->
            println "${CYAN}* consul:$NORMAL deregistering checks $checkId"
            consulApi { Executor http, String consulAddress ->
                http.execute(Get("http://$consulAddress/v1/agent/check/deregister/$checkId")).returnContent().asString()
            }
        }
    }
}
