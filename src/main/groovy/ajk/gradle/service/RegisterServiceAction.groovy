package ajk.gradle.service

import ajk.gradle.ConsulApiClient
import ajk.gradle.ConsulExtension
import org.apache.http.client.fluent.Executor

import static ajk.gradle.ConsulPlugin.CYAN
import static ajk.gradle.ConsulPlugin.NORMAL
import static org.apache.http.client.fluent.Request.Put
import static org.apache.http.entity.ContentType.APPLICATION_JSON

class RegisterServiceAction extends ConsulApiClient {
    String id

    String name

    String address

    int port

    String[] tags

    RegisterServiceAction(ConsulExtension consulExtension) {
        this.consulHostname = "localhost"
        this.consulPort = consulExtension.httpPort
    }

    void execute() {
        if (id == null || id == "") {
            id = name
        }

        println "${CYAN}* consul:$NORMAL registering service [ $id, $name, $address:$port, $tags ]"

        consulApi { Executor http, String consulAddress ->
            http.execute(Put("http://$consulAddress/v1/agent/service/register")
                    .bodyString("""{
    "ID": "$id",
    "Name": "$name",
    "Address": "$address",
    "Port": $port,
    "Tags": [ ${tags?.collect { "\"$it\"" }?.join(",")} ]
}""", APPLICATION_JSON
            )).discardContent()
        }
    }
}
