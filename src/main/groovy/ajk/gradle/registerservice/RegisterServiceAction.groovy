package ajk.gradle.registerservice

import ajk.gradle.ConsulExtension
import org.apache.http.HttpHost
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.impl.conn.DefaultProxyRoutePlanner
import org.gradle.api.Project

import static ajk.gradle.ConsulPlugin.CYAN
import static ajk.gradle.ConsulPlugin.NORMAL
import static org.apache.http.client.fluent.Executor.newInstance
import static org.apache.http.client.fluent.Request.Put
import static org.apache.http.entity.ContentType.APPLICATION_JSON
import static org.apache.http.impl.client.HttpClients.custom

class RegisterServiceAction {
    Project project

    String consulHostname

    int consulPort

    String proxyHost = System.getProperty("http.proxyHost")

    int proxyPort = Integer.parseInt(System.getProperty("http.proxyPort", ""))

    boolean useProxy

    String id

    String name

    String address

    int port

    String[] tags

    RegisterServiceAction(Project project, ConsulExtension consulExtension) {
        this.consulHostname = "localhost"
        this.consulPort = consulExtension.httpPort
        this.project = project
    }

    private def createExecutor() {
        HttpClientBuilder builder = custom()
        if (useProxy) {
            println "${CYAN}* consul:$NORMAL using http proxy definitions: http://$proxyHost:$proxyPort"
            builder.setRoutePlanner(new DefaultProxyRoutePlanner(new HttpHost(proxyHost, proxyPort)))
        }

        newInstance(builder.build())
    }

    void execute() {
        if (id == null || id == "") {
            id = name
        }

        println "${CYAN}* consul:$NORMAL registering service [ $id, $name, $address:$port, $tags ] with consul at $consulHostname:$consulPort"

        createExecutor().execute(Put("http://$consulHostname:$consulPort/v1/agent/service/register")
                .bodyString("""{
    "ID": "$id",
    "Name": "$name",
    "Address": "$address",
    "Port": $port,
    "Tags": [ ${tags?.collect { "\"$it\"" }?.join(",")} ]
}""", APPLICATION_JSON
        )).returnContent().asString()

        // todo allow to go through a gateway using SSH - use JSch to open a tunnel
    }
}
