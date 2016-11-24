package ajk.gradle.start

import ajk.gradle.ConsulExtension
import de.undercouch.gradle.tasks.download.DownloadAction
import org.gradle.api.Project

import static ajk.gradle.ConsulPlugin.*
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
        this.consulDir = new File("$consulDir/$version")

    }

    private def installConsul = {
        File marker = getExec()
        if (!marker.exists()) {
            println "${CYAN}* consul:$NORMAL installing consul in $consulDir"

            // Assume default LINUX
            def consulZip = "consul_${version}_linux_386.zip"
            DownloadAction binaries = new DownloadAction(project)
            if (isFamily(FAMILY_WINDOWS)) {
                consulZip = "consul_${version}_windows_386.zip"
            }
            binaries.src("https://releases.hashicorp.com/consul/${version}/${consulZip}")
            def consulDest = new File("$consulDir/$consulZip")
            binaries.dest(consulDest)
            binaries.onlyIfNewer(true) //Linux and Windows version can be loaded both
            binaries.execute()

            def uiZip = new File("$consulDir/ui.zip")
            DownloadAction ui = new DownloadAction(project)
            ui.dest(uiZip)
            ui.src("https://releases.hashicorp.com/consul/${version}/consul_${version}_web_ui.zip")
            ui.onlyIfNewer(true)
            ui.execute()

            def uiDir = consulDir
            if (!version.startsWith("0.5")) {
                uiDir = new File("$consulDir/dist")
                uiDir.mkdirs()
            }

            ant.unzip(src: consulDest, dest: consulDir)
            ant.chmod(dir:consulDir, perm:'+rx', includes:"consul")
            ant.unzip(src: uiZip, dest: uiDir)
        } else {
            println "${CYAN}* consul:$NORMAL using consul $version binaries in $consulDir"
        }
    }

    private def configureConsul = {
        File configDir = getConfigDir()
        File dataDir = getDataDir()

        if (!dataDir.exists()) {
            println "${CYAN}* consul:$NORMAL creating data dir: $dataDir"
            if (!dataDir.mkdirs()){
                println "${CYAN}* consul:$RED Unable to create data directory$NORMAL"
                throw new RuntimeException();
            }
        }

        if (!configDir.exists()) {
            println "${CYAN}* consul:$NORMAL creating configuration dir: $configDir"
            if (!configDir.mkdirs()){
                println "${CYAN}* consul:$RED Unable to create configuration directory$NORMAL"
                throw new RuntimeException();
            }
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
                getExec().absolutePath,
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
            ant.fail("${CYAN}* consul:$RED unable to start consul$NORMAL ("+getExec().absolutePath+")")
        } else {
            println "${CYAN}* consul:$GREEN consul is up, browse to http://localhost:$httpPort to see the UI$NORMAL"
        }
    }

    void execute() {
        installConsul()
        def configDir = configureConsul()
        startInstance(configDir)
    }

    File getExec() {
        File consulExe = new File("$consulDir/consul")
        if (isFamily(FAMILY_WINDOWS)) {
            consulExe = new File("$consulDir/consul.exe")
        }
        consulExe
    }

    File getConfigDir() {
        File configDir = new File("$project.buildDir/consul/consul.d")
        if (isFamily(FAMILY_WINDOWS)) {
            configDir = new File("$project.buildDir/consul/consul.d")
        }
        configDir
    }

    File getDataDir() {
        File dataDir = new File("/tmp")
        if (isFamily(FAMILY_WINDOWS)) {
            dataDir = new File("$project.buildDir/consul/data")
        }
        dataDir
    }

}
