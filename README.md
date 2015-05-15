# gradle-consul-plugin
A gradle plugin that can start a [Consul](https://consul.io) instance during the build. The purpose of this plugin is to
be used in integration tests that require a Consul instance

[ ![Download](https://api.bintray.com/packages/amirk/maven/gradle-consul-plugin/images/download.svg) ](https://bintray.com/amirk/maven/gradle-consul-plugin/_latestVersion)

# Using

Define the plugin in your build.gradle:

```

    buildscript {
        repositories {
            maven { url "http://dl.bintray.com/amirk/maven" }
        }
        dependencies {
            classpath("ajk.gradle.consul:gradle-consul-plugin:0.0.1")
        }
    }

    apply plugin: 'ajk.gradle.consul'
```

Starting consul:

```

    $ gradlew startConsul

```

# Configuring the plugin

You can change the consul ports and version by defining the following in your build script:

```

    consul {
        version = "0.5.1"
        httpPort = 8500
        dnsPort = 8600
        consulDir = file("$rootProject.projectDir/gradle/tools/consul")
    }

```

The values show here above are the default values.

# References

If you're interested in Consul, perhaps you'd also be interested in [consul4spring](https://github.com/amirkibbar/plum)