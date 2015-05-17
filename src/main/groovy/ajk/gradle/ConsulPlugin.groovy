package ajk.gradle

import ajk.gradle.registerservice.RegisterServiceExtension
import ajk.gradle.start.StartConsulExtension
import ajk.gradle.start.StartConsulTask
import ajk.gradle.stop.StopConsulExtension
import ajk.gradle.stop.StopConsulTask
import org.gradle.BuildAdapter
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.invocation.Gradle

class ConsulPlugin implements Plugin<Project> {
    static final String DEFAULT_VERSION = "0.5.1"
    static final int DEFAULT_HTTP_PORT = 8500
    static final int DEFAULT_DNS_PORT = 8600

    static final String ESC = "${(char) 27}"
    static final String CYAN = "${ESC}[36m"
    static final String GREEN = "${ESC}[32m"
    static final String YELLOW = "${ESC}[33m"
    static final String RED = "${ESC}[31m"
    static final String NORMAL = "${ESC}[0m"

    private Project project

    @Override
    void apply(Project project) {
        this.project = project

        StartConsulTask startConsul = project.task(type: StartConsulTask, 'startConsul')
        StopConsulTask stopConsul = project.task(type: StopConsulTask, 'stopConsul')

        def consulExtension = project.extensions.create('consul', ConsulExtension)
        consulExtension.with {
            version = DEFAULT_VERSION
            httpPort = DEFAULT_HTTP_PORT
            dnsPort = DEFAULT_DNS_PORT
            consulDir = new File("$project.rootProject.projectDir/gradle/tools/consul")
        }

        def projectAdapter = [
                startConsul          : startConsul,
                stopConsul           : stopConsul,
                projectsEvaluated    : { Gradle gradle ->
                    startConsul.with {
                        version = consulExtension.version
                        httpPort = consulExtension.httpPort
                        dnsPort = consulExtension.dnsPort
                        consulDir = consulExtension.consulDir
                    }

                    stopConsul.with {
                        consulDir = consulExtension.consulDir
                    }
                }
        ] as BuildAdapter

        project.gradle.addBuildListener(projectAdapter)

        project.extensions.create('startConsul', StartConsulExtension, project, consulExtension)
        project.extensions.create('stopConsul', StopConsulExtension, project, consulExtension)
        project.extensions.create('registerConsulService', RegisterServiceExtension, project, consulExtension)
    }
}