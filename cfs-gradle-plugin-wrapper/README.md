# cfs-gradle-plugin-wrapper

## Maven

This module only invokes the Gradle build and ensures the build order.

The following lifecycle phases are mapped to Gradle tasks:

| Maven phase | Gradle task         |
| ----------- | ------------------- |
| clean       | clean               |
| compile     | build               |
| install     | publishToMavenLocal | 

Gradle installs the `cfs-gradle-plugin` (with sources and javadocs) and the richer `dev.bodewig.compilefromsource.gradle.plugin` into the local Maven repository.

Since I couldn't get the Gradle `signing` plugin to generate the GPG armored ascii public key files (.asc) required by Sonatype OSSRH, Maven picks up the artifacts from the local repository and signs and deploys them correctly.


## Gradle

To invoke Gradle without Maven, the following project properties have to be supplied on the command line:

* groupId=dev.bodewig.compilefromsource
* artifactId=cfs-gradle-test
* version=1.0.0

See the project `README` for the plugin usage.
