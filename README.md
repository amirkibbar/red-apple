# gradle-consul-plugin
A gradle plugin that can start a [Consul](https://consul.io) instance during the build. The purpose of this plugin is to
be used in integration tests that require a Consul instance

[ ![Build Status](https://travis-ci.org/amirkibbar/red-apple.svg?branch=master) ](https://travis-ci.org/amirkibbar/red-apple)
[ ![Download](https://api.bintray.com/packages/amirk/maven/gradle-consul-plugin/images/download.svg) ](https://bintray.com/amirk/maven/gradle-consul-plugin/_latestVersion)

This plugin provides the following functionality:

- Install a local Consul with a web UI, start it and stop it
- Register and deregister services in any Consul (local, behind an HTTP proxy, using an SSH tunnel through a gateway server)
- Deregister checks in any Consul (local, behind an HTTP proxy, using an SSH tunnel through a gateway server)

# Using

Define the plugin in your build.gradle

Gradle 2.1 and above:

```gradle

    plugins {
        id "ajk.gradle.consul" version "0.1.1"
    }
```

Older gradle versions:

```gradle

    buildscript {
        repositories {
            jcenter()
            maven { url "http://dl.bintray.com/amirk/maven" }
        }
        dependencies {
            classpath("ajk.gradle.consul:gradle-consul-plugin:0.1.1")
        }
    }

    apply plugin: 'ajk.gradle.consul'
```

## Starting consul

```bash

    $ gradlew startConsul

```

You don't need to install Consul - the plugin will install it for you, configure it, add the web UI And then start it.

## Stopping consul

```bash

    $ gradlew stopConsul
```

# Configuring the plugin

You can change the consul ports and version by defining the following in your build script:

```gradle

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

```gradle

    task foo << {
        startConsul {}
        ...
        stopConsul {}
    }

```

Consul will start using the configuration in the `Consul {}` section (or the defaults)

# Registering a service with Consul

You can register any service with any Consul, not just the local one started by this plugin. To register a service:

```gradle

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

## Registering a service with a Consul using an SSH tunnel

Your Consul server might only be accessible using a gateway server if, for example, your Consul server is in some cloud
provider, and you can only access its HTTP port through a tunnel. In this case you can configure the registerConsulService
extension to open an SSH tunnel and run the command against a local port routed through that tunnel.

To clarify let's examine this scenario:

- The consul is installed on a server in a cloud provider with a local IP address (not exposed to the internet): 
  10.0.0.123
- The Consul server is accessible through a gateway machine with a public IP address (exposed to the internet: 52.1.2.3
- The gateway at 52.1.2.3 is running sshd
- You'd like to register the service name with address 1.2.3.4 port 1234 in the Consul running on 10.0.0.123:8500 

The following settings will tell the registerConsulService extension to open a tunnel and use the Consul server at 
10.0.0.123 instead of taking the `consul {}` configuration:

```gradle

    task foo << {
        registerConsulService {
            // consul connection properties
            gatewayAddress = '52.1.2.3'
            gatewayPort = 22
            gatewayUsername = 'ubuntu'
            privateKey = file("${System.properties['user.home']}/.ssh/id_rsa")
            known_hosts = file("${System.properties['user.home']}/.ssh/known_hosts")
            targetAddress = '10.0.0.123'
            targetPort = 8500

            // service properties
            id = 'my-service-id'
            name = 'my-service'
            address = '1.2.3.4'
            port = 5678
            tags = [ "tag-1", "tag-2" ]
        }
    }
    
```

The *gatewayPort*, *privateKey*, *known_hosts* and *targetPort* are optional - the value here above is the default value.

You might run into a problem with the known_hosts - this is because [JSch](http://www.jcraft.com/jsch/) requires rsa
keys in the known_hosts and not any other type of key. Some SSH clients, like the one in Cygwin use, by default, other
key types. You can add your gateway rsa SSH key to your known_hosts as follows:

```bash

    ssh-keyscan -t rsa 52.1.2.3 >> ~/.ssh/known_hosts
```

# Deregistering Consul services

The deregisterConsulService extension supports the same connection options as the registerConsulService extension, 
which means that you can deregister a service in a Consul behind an HTTP proxy or using an SSH tunnel through a gateway.

You can deregister a service by its ID or all services that match a certain tag.

## Deregister a service by its ID

If you know the service ID you can deregister it as follows:

```gradle

    task foo << {
        deregisterConsulService {
            id = 'my-service-id
        }
    }
```

## Deregister services by matching a tag value

You can deregister all services that have a specific tag value:

```gradle

    task foo << {
        deregisterConsulService {
            tag = 'tag-1'
        }
    }
```

The above task will deregister all Consul services that have the tag 'tag-1'.

# Deregistering Consul checks

Much like the deregisterConsulService extension, the deregisterConsulCheck extension can remove checks. This extension
too supports the same connection options, which means that you can deregister checks in a Consul behind an HTTP proxy
or using an SSH tunnel through a gateway.

To deregister all checks that contain a some id:

```gradle

    task foo {
        deregisterConsulCheck {
            id = 'service'
        }
    }
```

The above task will deregister all Consul checks that have 'service' in their ID.

# Listing services registered in Consul

You can list the services in a Consul using the simple listConsulServices task or by using the more advanced
listConsulServices extension. The task can only list the services registered in a local consul - as configured in the
`consul {}` section, whereas the listConsulServices extension can list services in a Consul behind a firewall or using
an SSH tunnel through a gateway.

## Listing services with the task

```bash

    $ gradlew listConsulServices
```

The above will list the services in the local Consul in port 8500 (unless some other port has been configured in the
`consul {}` section.

## Listing services with the extension

### Listing services in the localhost using an arbitrary port

```gradle

    task foo << {
        listConsulServices {
            consulPort = 8521
        }
    }
```

The task foo above will list the services registered on the Consul running at localhost:8521

### Listing services in any host running behind an HTTP proxy

```gradle

    task foo << {
        listConsulServices {
            consulHostname = '1.2.3.4'
            consulPort = 8500
            useProxy = false
            proxyHost = 'web-proxy.evil-corp.com'
            proxyPort = 8888
        }
    }
```

The task foo above will list the services registered on the Consul running at 1.2.3.4:8500 behind the HTTP proxy at
web-proxy.evil-corp.com:8888.

### Listing services in any host that requires an SSH tunnel throguh a gateway

```gradle

    task foo << {
        listConsulServices {
            gatewayAddress = '52.1.2.3'
            gatewayPort = 22
            gatewayUsername = 'ubuntu'
            privateKey = file("${System.properties['user.home']}/.ssh/id_rsa")
            known_hosts = file("${System.properties['user.home']}/.ssh/known_hosts")
            targetAddress = '10.0.0.123'
            targetPort = 8500
        }
    }
```

The task foo above will list the services registered on the Consul running at 10.0.0.123:8500 using an SSH tunnel
through the gateway at ubuntu@52.1.2.3:22.

# Limitations

For the time being the start and stop Consul commands only work on windows 

# References

If you're interested in Consul, perhaps you'd also be interested in [consul4spring](https://github.com/amirkibbar/plum)