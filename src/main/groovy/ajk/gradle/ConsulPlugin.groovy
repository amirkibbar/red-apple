package ajk.gradle

import org.gradle.BuildAdapter
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.invocation.Gradle

class ConsulPlugin implements Plugin<Project> {
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

        def extension = project.extensions.create('consul', ConsulExtension)
        extension.with {
            version = StartConsulTask.DEFAULT_VERSION
            httpPort = StartConsulTask.DEFAULT_HTTP_PORT
            dnsPort = StartConsulTask.DEFAULT_DNS_PORT
            consulDir = new File("$project.rootProject.projectDir/gradle/tools/consul")
        }

        def projectAdapter = [
                startConsul      : startConsul,
                projectsEvaluated: { Gradle gradle ->
                    startConsul.with {
                        version = extension.version
                        httpPort = extension.httpPort
                        dnsPort = extension.dnsPort
                        consulDir = extension.consulDir
                    }

                    stopConsul.with {
                        consulDir = extension.consulDir
                    }
                }
        ] as BuildAdapter

        project.gradle.addBuildListener(projectAdapter)
    }
}