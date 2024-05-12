package dev.bodewig.compilefromsource.gradle.plugin;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.type.ArtifactTypeDefinition;
import org.gradle.api.file.Directory;
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
	 * The name of the registered task
	 */
	public static final String TASK_NAME = "pullSources";

	/**
	 * The name of the source dir
	 */
	public static final String SOURCE_DIR_NAME = "pulled";

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
			// use source artifacts
			c.resolutionStrategy(rs -> {
				rs.dependencySubstitution(ds -> {
					ds.all(des -> {
						des.artifactSelection(asd -> {
							if (asd.getRequestedSelectors().isEmpty()) {
								asd.selectArtifact(ArtifactTypeDefinition.JAR_TYPE, null, SOURCES_CLASSIFIER);
							} else {
								asd.getRequestedSelectors().forEach(das -> {
									asd.selectArtifact(das.getType(), das.getExtension(), SOURCES_CLASSIFIER);
								});
							}
						});
						logger.warn("Resolved dependency " + des.getRequested().getDisplayName());
					});
				});
			});
			// source dependencies should not be inherited by other projects
			c.setVisible(false);
		});

		// add additional source set
		SourceSetContainer sourceSets = project.getExtensions().getByType(SourceSetContainer.class);
		SourceSet pulled = sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME);
		Directory sourcesDir = project.getLayout().getBuildDirectory().dir(SOURCE_DIR_NAME).get();

		// add task to unpack sources
		Task pullSources = project.getTasks().create(TASK_NAME);
		pullSources.doLast(t -> {
			// resolve dependencies
			config.resolve().forEach(f -> {
				logger.warn("Unpacked " + f.getName());
				project.copy(cs -> {
					cs.from(project.zipTree(f));
					cs.into(sourcesDir.dir(f.getName()));
				});
			});
			pulled.getJava().srcDir(sourcesDir);
		});

		// run task before compilation
		TaskCollection<JavaCompile> compileJava = project.getTasks().withType(JavaCompile.class);
		compileJava.configureEach(t -> {
			t.dependsOn(pullSources);
		});
	}
}
