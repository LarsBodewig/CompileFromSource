[![Gradle plugin available on Maven Central](https://img.shields.io/maven-central/v/dev.bodewig.compilefromsource/dev.bodewig.compilefromsource.gradle.plugin?label=Gradle%20plugin%20available%20on%20Maven%20Central)](https://central.sonatype.com/artifact/dev.bodewig.compilefromsource/dev.bodewig.compilefromsource.gradle.plugin)

# CompileFromSource

CompileFromSource is a gradle plugin intended to provide one functionality of the [Apache Maven Dependency Plugin](https://maven.apache.org/plugins/maven-dependency-plugin/index.html) by resolving the specified and transitive dependencies, downloading the source artifacts and adding them to the Java source set in order to compile them as part of your own project.

This is useful to compile a custom variant using a special configuration (e.g. for older or newer Java versions) or apply custom post-processing (e.g. your own annotation processor or compiler plugin). The `compileFromSource` configuration can be used with Gradles exclusion mechanism to limit unwanted dependency chains - in that case the excluded dependencies need to be added to the `implementation` or `api` configuration to compile successfully.

## Gradle plugin usage (groovy)

```groovy
plugins {
	id 'dev.bodewig.compilefromsource' version '1.0.0'
}
dependencies {
	compileFromSource 'my.dependency:1.0.0'
}
```

If you use the `dev.bodewig.compilefromsource` plugin, the Gradle Java plugin is applied automatically.

---

Run `git config --add include.path ../.gitconfig` to include the template config in your project config.
