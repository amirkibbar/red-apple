package ajk.gradle

import org.apache.http.HttpHost
import org.apache.http.client.fluent.Executor
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.impl.conn.DefaultProxyRoutePlanner

import static ajk.gradle.ConsulPlugin.CYAN
import static ajk.gradle.ConsulPlugin.NORMAL
import static org.apache.http.client.fluent.Executor.newInstance
import static org.apache.http.impl.client.HttpClients.custom

class ProxyHttpClientBuilder {
    static Executor buildProxyHttpClient(boolean useProxy, String proxyHost, Integer proxyPort) {
        HttpClientBuilder builder = custom()

        if (useProxy) {
            String host = "${proxyHost ?: System.getProperty('http.proxyHost')}"
            int port = Integer.parseInt("${proxyPort ?: System.getProperty('http.proxyPort', '')}")

            println "${CYAN}* consul:$NORMAL using http proxy definitions: http://$host:$port"
            builder.setRoutePlanner(new DefaultProxyRoutePlanner(new HttpHost(host, port)))
        }

        newInstance(builder.build())
    }
}
