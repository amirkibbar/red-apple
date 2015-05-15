package ajk.gradle

import de.undercouch.gradle.tasks.download.DownloadAction
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction

import static ajk.gradle.ConsulPlugin.CYAN
import static ajk.gradle.ConsulPlugin.GREEN
import static ajk.gradle.ConsulPlugin.NORMAL
import static ajk.gradle.ConsulPlugin.RED
import static org.apache.tools.ant.taskdefs.condition.Os.FAMILY_WINDOWS
import static org.apache.tools.ant.taskdefs.condition.Os.isFamily

class StartConsulTask extends DefaultTask {
    static final String DEFAULT_VERSION = "0.5.1"
    static final int DEFAULT_HTTP_PORT = 8500
    static final int DEFAULT_DNS_PORT = 8600

    @Input
    @Optional
    int httpPort

    @Input
    @Optional
    int dnsPort

    @Input
    @Optional
    String version

    @Input
    @Optional
    File consulDir

    def installConsul = {
        File marker = new File("$consulDir/consul.$version")
        if (!marker.exists()) {
            println "${CYAN}* consul:$NORMAL installing consul in $consulDir"
            ant.touch(file: marker, mkdirs: true)

            def consulZip = new File("$consulDir/consul.zip")
            DownloadAction binaries = new DownloadAction()
            binaries.dest(consulZip)
            binaries.src("https://dl.bintray.com/mitchellh/consul/${version}_windows_386.zip")
            binaries.onlyIfNewer(true)
            binaries.execute(project)

            def uiZip = new File("$consulDir/ui.zip")
            DownloadAction ui = new DownloadAction()
            ui.dest(uiZip)
            ui.src("https://dl.bintray.com/mitchellh/consul/${version}_web_ui.zip")
            ui.onlyIfNewer(true)
            ui.execute(project)

            ant.unzip(src: consulZip, dest: consulDir)
            ant.unzip(src: uiZip, dest: consulDir)
        } else {
            println "${CYAN}* consul:$NORMAL using consul $version binaries in $consulDir"
        }
    }

    def configureConsul = {
        File configDir = new File("$project.buildDir/consul/consul.d")
        File dataDir = new File("$project.buildDir/consul/data")

        if (!dataDir.exists()) {
            println "${CYAN}* consul:$NORMAL creating data dir: $dataDir"
            dataDir.mkdirs()
        }

        if (!configDir.exists()) {
            println "${CYAN}* consul:$NORMAL creating configuration dir: $configDir"
            configDir.mkdirs()
            println "${CYAN}* consul:$NORMAL creating bootstrap configuration file"
            new File("$configDir/bootstrap.json") << """{
  "datacenter": "local",
  "data_dir": "${dataDir.absolutePath.replaceAll("\\\\", "/")}",
  "log_level": "INFO",
  "node_name": "integrationtests",
  "server": true,
  "ui_dir": "${consulDir.absolutePath.replaceAll("\\\\", "/")}/dist",
  "bind_addr": "127.0.0.1",
  "bootstrap_expect": 1,
  "addresses": {
    "dns": "127.0.0.1",
    "http": "0.0.0.0",
    "https": "0.0.0.0",
    "rpc": "127.0.0.1"
  },
  "ports": {
    "dns": $dnsPort,
    "http": $httpPort
  }
}
"""
        } else {
            println "${CYAN}* consul:$NORMAL using configuration dir: $configDir"
        }

        configDir
    }

    def startInstance = { File configDir ->
        println "${CYAN}* consul:$NORMAL starting consul"

        [
                "$consulDir/consul.exe",
                "agent",
                "-config-dir",
                configDir.absolutePath
        ].execute()

        println "${CYAN}* consul:$NORMAL waiting for consul to start"

        ant.waitfor(maxwait: "1", maxwaitunit: "minute", timeoutproperty: "consulTimeout") {
            and {
                socket(server: "localhost", port: dnsPort)
                ant.http(url: "http://localhost:$httpPort")
            }
        }

        if (ant.properties['consulTimeout']) {
            ant.fail("${CYAN}* consul:$RED unable to start consul$NORMAL")
        } else {
            println "${CYAN}* consul:$GREEN consul is up$NORMAL"
        }
    }

    @TaskAction
    void startConsul() {
        if (!isFamily(FAMILY_WINDOWS)) {
            println "${CYAN}* consul:$RED for the time being this plugin is only supported on Windows$NORMAL"
            throw new UnsupportedOperationException();
        }

        installConsul()
        def configDir = configureConsul()
        startInstance(configDir)
    }
}
