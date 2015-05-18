package ajk.gradle

import com.jcraft.jsch.JSch
import com.jcraft.jsch.Session

class SshTunnel {
    String gatewayAddress
    int gatewayPort
    String gatewayUsername
    File known_hosts
    File privateKey
    String targetAddress
    int targetPort

    int localPort
    private Session session

    SshTunnel(ConsulApiClient client) {
        this(client.gatewayAddress,
                client.gatewayPort,
                client.gatewayUsername,
                client.known_hosts,
                client.privateKey,
                client.targetAddress,
                client.targetPort)
    }

    SshTunnel(String gatewayAddress, Integer gatewayPort, String gatewayUsername, File known_hosts, File privateKey, String targetAddress, Integer targetPort) {
        String userHome = System.properties['user.home']
        this.gatewayAddress = gatewayAddress
        this.gatewayPort = gatewayPort ?: 22
        this.gatewayUsername = gatewayUsername
        this.known_hosts = known_hosts ?: new File("$userHome/.ssh/known_hosts")
        this.privateKey = privateKey ?: new File("$userHome/.ssh/id_rsa")
        this.targetAddress = targetAddress
        this.targetPort = targetPort ?: 8500
        this.localPort = findLocalPort()
    }

    int create() {
        JSch jsch = new JSch()
        session = jsch.getSession(gatewayUsername, gatewayAddress, gatewayPort)
        jsch.setKnownHosts(known_hosts.absolutePath)
        jsch.addIdentity(privateKey.absolutePath)

        session.connect()
        session.setPortForwardingL(localPort, targetAddress, targetPort)

        return localPort
    }

    void close() {
        session?.disconnect()
    }

    @SuppressWarnings("GrMethodMayBeStatic")
    private int findLocalPort() {
        ServerSocket ignored = new ServerSocket(0)
        int port = ignored.localPort
        ignored.close()

        return port
    }
}
