package ajk.gradle

import de.undercouch.gradle.tasks.download.DownloadAction
import org.gradle.api.Project

import static ajk.gradle.ConsulPlugin.CYAN
import static ajk.gradle.ConsulPlugin.GREEN
import static ajk.gradle.ConsulPlugin.NORMAL
import static ajk.gradle.ConsulPlugin.RED
import static org.apache.tools.ant.taskdefs.condition.Os.FAMILY_WINDOWS
import static org.apache.tools.ant.taskdefs.condition.Os.isFamily

class StartAction {
    Project project

    int httpPort

    int dnsPort

    String version

    File consulDir

    AntBuilder ant

    StartAction(Project project, ConsulExtension extension) {
        this(project, extension.httpPort, extension.dnsPort, extension.version, extension.consulDir)
    }

    StartAction(Project project, int httpPort, int dnsPort, String version, File consulDir) {
        this.project = project
        this.ant = project.ant
        this.httpPort = httpPort
        this.dnsPort = dnsPort
        this.version = version
        this.consulDir = consulDir
    }

    private def installConsul = {
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

    private def configureConsul = {
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

    private def startInstance = { File configDir ->
        println "${CYAN}* consul:$NORMAL starting consul"

        [
                "$consulDir/consul.exe",
                "agent",
                "-config-dir",
                configDir.absolutePath
        ].execute()

        println "${CYAN}* consul:$NORMAL waiting for consul to start, testing ports $httpPort, $dnsPort"

        ant.waitfor(maxwait: "1", maxwaitunit: "minute", timeoutproperty: "consulTimeout") {
            and {
                socket(server: "localhost", port: dnsPort)
                ant.http(url: "http://localhost:$httpPort")
            }
        }

        if (ant.properties['consulTimeout']) {
            ant.fail("${CYAN}* consul:$RED unable to start consul$NORMAL")
        } else {
            println "${CYAN}* consul:$GREEN consul is up, browse to http://localhost:$httpPort to see the UI$NORMAL"
        }
    }

    void execute() {
        if (!isFamily(FAMILY_WINDOWS)) {
            println "${CYAN}* consul:$RED for the time being this plugin is only supported on Windows$NORMAL"
            throw new UnsupportedOperationException();
        }

        installConsul()
        def configDir = configureConsul()
        startInstance(configDir)
    }
}
