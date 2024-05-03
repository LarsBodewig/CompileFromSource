package dev.bodewig.compilefromsource.gradle.plugin;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.LenientConfiguration;
import org.gradle.api.artifacts.ResolvedConfiguration;
import org.gradle.api.artifacts.type.ArtifactTypeDefinition;
import org.gradle.api.file.SourceDirectorySet;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.TaskCollection;
import org.gradle.api.tasks.compile.JavaCompile;

/**
 * Plugin that defines and handles the {@code compileFromSource} dependency
 * configuration. Dependencies in this configuration are transitively downloaded
 * as source JAR and compiled as standard project sources.
 */
public abstract class CompileFromSourcePlugin implements Plugin<Project> {

	private static final Logger logger = Logging.getLogger(CompileFromSourcePlugin.class);

	/**
	 * The name of the added configuration
	 */
	public static final String CONFIGURATION_NAME = "compileFromSource";

	/**
	 * The classifier for source jars
	 */
	public static final String SOURCES_CLASSIFIER = "sources";

	/**
	 * The name of the added source set
	 */
	public static final String SOURCE_SET_NAME = "pulled";

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

		// add configuration
		Configuration config = project.getConfigurations().create(CONFIGURATION_NAME, c -> {
			c.resolutionStrategy(rs -> {
				rs.eachDependency(drs -> {
					drs.artifactSelection(asd -> {
						if (asd.getRequestedSelectors().isEmpty()) {
							asd.selectArtifact(ArtifactTypeDefinition.JAR_TYPE, null, SOURCES_CLASSIFIER);
						} else {
							asd.getRequestedSelectors().forEach(das -> {
								asd.selectArtifact(das.getType(), das.getExtension(), SOURCES_CLASSIFIER);
							});
						}
					});
				});
			});
			c.setVisible(false); // dependencies should not be inherited by other projects
		});

		// add additional source set
		SourceSetContainer sourceSets = project.getExtensions().getByType(SourceSetContainer.class);
		SourceSet pulled = sourceSets.create(SOURCE_SET_NAME);
		SourceDirectorySet pulledDirs = pulled.getAllSource();

		// add task to copy sources
		Task pullSources = project.getTasks().create(TASK_NAME);
		pullSources.doLast(t -> {
			// resolve dependencies
			ResolvedConfiguration resolved = config.getResolvedConfiguration();
			LenientConfiguration lenient = resolved.getLenientConfiguration();
			logger.info("Resolved " + lenient.getArtifacts().size() + " artifacts:");
			lenient.getArtifacts().forEach(ra -> {
				logger.info("\t" + ra.getName() + " -> " + ra.getFile().getName());
				// add to source set
				pulledDirs.srcDir(project.file(ra.getFile()));
			});
			if (!lenient.getUnresolvedModuleDependencies().isEmpty()) {
				logger.error("Could not resolve " + lenient.getUnresolvedModuleDependencies().size() + " dependencies");
				lenient.getUnresolvedModuleDependencies().forEach(ud -> {
					logger.error("Unresolved dependency: ", ud.getProblem());
				});
			}
			resolved.rethrowFailure(); // outside of if in case of other failures
		});

		// run task before compilation
		TaskCollection<JavaCompile> compileJava = project.getTasks().withType(JavaCompile.class);
		compileJava.configureEach(t -> {
			t.dependsOn(pullSources);
		});

		// TODO: one output for each dependency?
	}
}
