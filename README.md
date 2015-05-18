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
            classpath("ajk.gradle.consul:gradle-consul-plugin:0.0.8")
        }
    }

    apply plugin: 'ajk.gradle.consul'
```

## Starting consul

```

    $ gradlew startConsul

```

You don't need to install Consul - the plugin will install it for you, configure it, add the web UI And then start it.

## Stopping consul

```

    $ gradlew stopConsul
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

The values shown here above are the default values.

# Starting and stopping Consul within a gradle task

You can start and stop Consul as part of a gradle task. This is very useful during integration tests:

```

    task foo << {
        startConsul {}
        ...
        stopConsul {}
    }

```

Consul will start using the configuration in the `Consul {}` section (or the defaults)

# Registering a service with Consul

You can register any service with any Consul, not just the local one started by this plugin. To register a service:

```

    task foo << {
        registerConsulService {
            // consul connection properties
            consulHostname = 'localhost'
            consulPort = 8500
            useProxy = false
            proxyHost = 'web-proxy.evil-corp.com'
            proxyPort = 8888

            // service properties
            id = 'my-service-id'
            name = 'my-service'
            address = '1.2.3.4'
            port = 5678
            tags = [ "tag-1", "tag-2" ]
        }
    }

```

When the *consulHostname* and *consulPort* are omitted the value from the `consul {}` configuration section are used (or
its default).

The default proxy settings are disabled ( *useProxy = false* ). The default *proxyHost* is the system property
*http.proxyHost* and the default *proxyPort* uses the value from the system property *http.proxyPort*. If you set other
values here they will override the system properties.

When the *id* is omitted the service is registered with the *name* as its *id*.

The *tags* are optional too.
 
The name, address and port are required.

# Limitations

For the time being the start and stop Consul commands only work on windows 

# References

If you're interested in Consul, perhaps you'd also be interested in [consul4spring](https://github.com/amirkibbar/plum)