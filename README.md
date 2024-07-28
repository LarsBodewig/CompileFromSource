[![Gradle plugin available on Maven Central](https://img.shields.io/maven-central/v/dev.bodewig.compilefromsource/dev.bodewig.compilefromsource.gradle.plugin?label=Gradle%20plugin%20available%20on%20Maven%20Central)](https://central.sonatype.com/artifact/dev.bodewig.compilefromsource/dev.bodewig.compilefromsource.gradle.plugin)

# CompileFromSource

> [!WARNING]
> This project is no longer maintained. After the initial release issues emerged that cannot be efficiently solved with the current approach. Feel free to fork the repository if you want to address these problems. Currently known issues are:
>
> * Resources in the original artifact may not be present in the sources (this was already solved on the main branch but never released).
> * Classifiers can be chosen arbitrarily; there is no easy way to guess or configure them for transitive dependencies.
> * Artifacts do not need to publish accurate sources or might require a custom build step.
>
> If you found this project in search of a way to apply a custom post-processor to dependencies, you might want to take a look at the [instrumentation API](https://docs.oracle.com/en/java/javase/22/docs/api/java.instrument/java/lang/instrument/package-summary.html) or tools for bytecode manipulation at build time, such as [byte-buddy](https://bytebuddy.net) or [ASM](https://asm.ow2.io/).

<br>

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
