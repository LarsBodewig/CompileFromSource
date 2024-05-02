# cfs-gradle-test-wrapper

## Maven

This module only invokes the Gradle build and ensures the build order.

The following lifecycle phases are mapped to Gradle tasks:

| Maven phase | Gradle task         |
| ----------- | ------------------- |
| clean       | clean               |
| compile     | assemble            |
| test        | test                |


## Gradle

To invoke Gradle without Maven, the following project properties have to be supplied on the command line:

* groupId=dev.bodewig.compilefromsource
* artifactId=cfs-gradle-test
* version=1.0.0
