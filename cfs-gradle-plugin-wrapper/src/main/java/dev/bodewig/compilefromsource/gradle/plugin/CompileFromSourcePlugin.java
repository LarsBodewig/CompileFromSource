package dev.bodewig.compilefromsource.gradle.plugin;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.compile.JavaCompile;

public abstract class CompileFromSourcePlugin implements Plugin<Project> {

	/**
	 * The name of the added configuration
	 */
	public static final String CONFIGURATION_NAME = "compileFromSource";

	/**
	 * The name of the registered task
	 */
	public static final String TASK_NAME = "pullSources";

	/**
	 * Default constructor
	 */
	public CompileFromSourcePlugin() {
	}

	@Override
	public void apply(Project project) {
		project.getPlugins().apply(JavaPlugin.class);

		// add configration
		Configuration config = project.getConfigurations().create(CONFIGURATION_NAME);

		// TODO: add own source set?
		SourceSetContainer sourceSets = project.getExtensions().getByType(SourceSetContainer.class);
		SourceSet main = sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME);

		// add task to copy sources
		TaskProvider<Task> pullSources = project.getTasks().create(TASK_NAME);
		pullSources.configure(t -> {
			// TODO
		});

		// run task before compilation
		TaskProvider<Task> compileJava = project.getTasks().named(JavaPlugin.COMPILE_JAVA_TASK_NAME);
		compileJava.dependsOn(pullSources);
	}
}
