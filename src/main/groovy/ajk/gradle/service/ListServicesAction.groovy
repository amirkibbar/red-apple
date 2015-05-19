package ajk.gradle.service

import ajk.gradle.ConsulApiClient
import ajk.gradle.ConsulExtension
import groovy.json.JsonSlurper
import org.apache.http.client.fluent.Executor

import static org.apache.http.client.fluent.Request.Get

import static ajk.gradle.ConsulPlugin.MAGENTA
import static ajk.gradle.ConsulPlugin.NORMAL

class ListServicesAction extends ConsulApiClient {
    ListServicesAction(int port) {
        this.consulHostname = "localhost"
        this.consulPort = port
    }

    ListServicesAction(ConsulExtension consulExtension) {
        this.consulHostname = "localhost"
        this.consulPort = consulExtension.httpPort
    }

    void execute() {
        println "in here"
        JsonSlurper js = new JsonSlurper()
        def services = []
        consulApi { Executor http, String consulAddress ->
            services += [
                    js.parseText(http
                            .execute(Get("http://$consulAddress/v1/agent/services"))
                            .returnContent().asString())
            ]
        }

        println "\n+${'-'.padRight(38, '-')}+${'-'.padRight(22, '-')}+${'-'.padRight(17, '-')}+${'-'.padRight(37, '-')}+"
        println "| $MAGENTA${'Service ID'.center(36)}$NORMAL | $MAGENTA${'Address'.center(20)}$NORMAL |" +
                " $MAGENTA${'Service Name'.center(15)}$NORMAL | $MAGENTA${'Tags'.center(35)}$NORMAL |"
        println "+${'='.padRight(38, '=')}+${'='.padRight(22, '=')}+${'='.padRight(17, '=')}+${'='.padRight(37, '=')}+"
        services.each {
            it.values().each { service ->
                String hostPort = "$service.Address:$service.Port"
                println "| ${service.ID.padRight(36).substring(0,36)} | ${hostPort.padRight(20).substring(0,20)} |" +
                        " ${service.Service.padRight(15).substring(0,15)} | ${service.Tags.join(',').padRight(35).substring(0,35)} |"
            }
        }
        println "+${'='.padRight(38, '=')}+${'='.padRight(22, '=')}+${'='.padRight(17, '=')}+${'='.padRight(37, '=')}+\n"
    }
}
