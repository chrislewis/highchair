# Highchair

Highchair is a set of modules for developing Google App Engine services and applications in scala.

Please use the [discussion group](http://groups.google.com/group/highchair-user) for questions, suggestions, and requests.

## Modules

### Datastore
A module providing an idiomatic API for persiting objects to the Google Datastore, and for executing type-safe queries:

    Person where (_.name is "Chris")
      and (_.middleName is Some("Aaron")) fetch (limit = 100)
      
[wiki](https://github.com/chrislewis/highchair/wiki/Datastore)

### Remote
Remote wraps the [Remote API](http://code.google.com/appengine/docs/java/tools/remoteapi.html),
which allows any java application to transparently access the App Engine services of a given application:

    remote {
      Person.put(Person(None, "Chris", "Aaron", "Lewis", 30))
    }

[wiki](https://github.com/chrislewis/highchair/wiki/Remote)

### Util
Util allows you to programatically launch and shutdown a local GAE application:

    val server = DevServer()
    server.start(guestbookApp)
    // ... integration tests ...
    server.stop()


## Install

Highchair requires at least Scala 2.8 and is cross-built for versions 2.8.0 - 2.9.0-1. Highchair artifacts are published using
the excellent [sbt](http://code.google.com/p/simple-build-tool/).

### sbt

    val h_datastore = "net.thegodcode" %% "highchair-datastore" % "0.0.4-SNAPSHOT"

### maven, ivy

Because sbt uses the maven module format, artifacts published via sbt are usable by maven and ivy. If you are
unfamiliar with sbt, note the following:

  - You must be explicit about the artifact versions. Because Highchair is cross-built its artifacts are suffixed
    with _scala_version, where scala_version is the version of scala against which the artifact was compiled.
    Therefore, in tools such as maven and ivy, the artifact id would be `highchair-datastore_2.8.0`.
  - You must add `http://scala-tools.org/repo-releases` as a repository.
