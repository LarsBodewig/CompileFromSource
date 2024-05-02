[![Gradle plugin available on Maven Central](https://img.shields.io/maven-central/v/dev.bodewig.compilefromsource/dev.bodewig.compilefromsource.gradle.plugin?label=Gradle%20plugin%20available%20on%20Maven%20Central)](https://central.sonatype.com/artifact/dev.bodewig.compilefromsource/dev.bodewig.compilefromsource.gradle.plugin)

# CompileFromSource

TODO

## Gradle plugin usage (groovy)

```groovy
plugins {
	id 'dev.bodewig.compileFromSource' version '1.0.0'
}
dependencies {
	compileFromSource 'my.dependency:1.0.0'
}
```

If you use the `dev.bodewig.compileFromSource` plugin, the Gradle Java plugin is applied automatically.

---

Run `git config --add include.path ../.gitconfig` to include the template config in your project config.
