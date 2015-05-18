package ajk.gradle

import org.apache.http.client.fluent.Executor

import static ajk.gradle.ConsulPlugin.CYAN
import static ajk.gradle.ConsulPlugin.NORMAL
import static ajk.gradle.ProxyHttpClientBuilder.buildProxyHttpClient

class ConsulApiClient {
    String gatewayAddress

    Integer gatewayPort

    String gatewayUsername

    File known_hosts

    File privateKey

    String targetAddress

    Integer targetPort

    String consulHostname

    int consulPort

    String proxyHost

    Integer proxyPort

    boolean useProxy

    Object consulApi(Closure closure) {
        SshTunnel tunnel
        if (gatewayAddress != null) {
            tunnel = new SshTunnel(this)

            println "${CYAN}* consul:$NORMAL routing request through an ssh tunnel," +
                    " local port: $tunnel.localPort, gateway: $tunnel.gatewayAddress:$tunnel.gatewayPort," +
                    " remote address: $tunnel.targetAddress:$tunnel.targetPort"
        } else {
            println "${CYAN}* consul:$NORMAL using local Consul on port $consulPort"
        }

        String consulAddress = tunnel == null ? "$consulHostname:$consulPort" : "localhost:$tunnel.localPort"

        useProxy = tunnel != null && useProxy

        try {
            tunnel?.create()
            Executor executor = buildProxyHttpClient(useProxy, proxyHost, proxyPort)

            return closure.call(executor, consulAddress)
        } finally {
            tunnel?.close()
        }
    }
}
