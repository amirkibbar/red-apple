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

    SshTunnel(SshTunnelDefinition definition) {
        this(definition.gatewayAddress,
                definition.gatewayPort,
                definition.gatewayUsername,
                definition.known_hosts,
                definition.privateKey,
                definition.targetAddress,
                definition.targetPort)
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

    int openTunnel() {
        JSch jsch = new JSch()
        session = jsch.getSession(gatewayUsername, gatewayAddress, gatewayPort)
        jsch.setKnownHosts(known_hosts.absolutePath)
        jsch.addIdentity(privateKey.absolutePath)

        session.connect()
        session.setPortForwardingL(localPort, targetAddress, targetPort)

        return localPort
    }

    void closeTunnel() {
        session.disconnect()
    }

    @SuppressWarnings("GrMethodMayBeStatic")
    private int findLocalPort() {
        ServerSocket ignored = new ServerSocket(0)
        int port = ignored.localPort
        ignored.close()

        return port
    }
}
