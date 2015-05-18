package ajk.gradle

class SshTunnelDefinition {
    String gatewayAddress

    Integer gatewayPort

    String gatewayUsername

    File known_hosts

    File privateKey

    String targetAddress

    Integer targetPort
}
