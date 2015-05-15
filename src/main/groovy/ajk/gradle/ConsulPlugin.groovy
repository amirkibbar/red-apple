package ajk.gradle

import org.gradle.BuildAdapter
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.invocation.Gradle

class ConsulPlugin implements Plugin<Project> {
    private Project project

    @Override
    void apply(Project project) {
        this.project = project

        StartConsulTask startConsul = project.task(type: StartConsulTask, 'startConsul')
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
                }
        ] as BuildAdapter

        project.gradle.addBuildListener(projectAdapter)
    }
}