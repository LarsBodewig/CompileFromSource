package dev.bodewig.compilefromsource.gradle.plugin;

import java.io.File;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.type.ArtifactTypeDefinition;
import org.gradle.api.file.Directory;
import org.gradle.api.file.DuplicatesStrategy;
import org.gradle.api.file.FileTree;
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
	 * The group of the added tasks
	 */
	public static final String TASK_GROUP = "CompileFromSource";

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
	public static final String TASK_NAME_PULL = "pullSources";

	/**
	 * The name of the registered task
	 */
	public static final String TASK_NAME_FILTER = "filterSources";

	/**
	 * The name of the pulled dir
	 */
	public static final String PULLED_DIR_NAME = "pulled";

	/**
	 * The name of the source dir
	 */
	public static final String SOURCE_DIR_NAME = "pulledSources";

	/**
	 * The name of the resource dir
	 */
	public static final String RESOURCE_DIR_NAME = "pulledResources";

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
								asd.selectArtifact(ArtifactTypeDefinition.JAR_TYPE, null, null);
								asd.selectArtifact(ArtifactTypeDefinition.JAR_TYPE, null, SOURCES_CLASSIFIER);
							} else {
								asd.getRequestedSelectors().forEach(das -> {
									asd.selectArtifact(das.getType(), das.getExtension(), das.getClassifier());
									asd.selectArtifact(das.getType(), das.getExtension(), SOURCES_CLASSIFIER); // best guess
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
		Directory pulledDir = project.getLayout().getBuildDirectory().dir(PULLED_DIR_NAME).get();
		Directory sourcesDir = project.getLayout().getBuildDirectory().dir(SOURCE_DIR_NAME).get();
		Directory resourcesDir = project.getLayout().getBuildDirectory().dir(RESOURCE_DIR_NAME).get();

		// add task to unpack sources
		Task pullSources = project.getTasks().create(TASK_NAME_PULL, t -> {
			t.setGroup(TASK_GROUP);
			t.setDescription("Downloads the configured dependencies and adds them as source directory for compilation");
			t.doLast(tt -> {
				// resolve dependencies
				config.resolve().forEach(f -> {
					logger.warn("Unpacked " + f.getName());
					project.copy(cs -> {
						cs.from(project.zipTree(f));
						cs.into(pulledDir.dir(f.getName()));
					});
				});
				// split sources and resources
				for (File f : pulledDir.getAsFile().listFiles()) {
					project.copy(cs -> {
						cs.from(f);
						cs.include("**/*.java");
						cs.into(sourcesDir);
						if (!sourcesDir.getAsFileTree().isEmpty()) {
							cs.filesMatching(sourcesDir.getAsFileTree().getFiles().stream().map(ff -> "**/" + ff.getPath()).toList(), cd -> {
								logger.warn("Overwriting duplicate file: " + cd.getPath()); // TODO: does not find duplicates!
							});
						}
					});
					project.copy(cs -> {
						cs.from(f);
						cs.exclude("**/*.java");
						cs.exclude("**/*.class");
						cs.into(resourcesDir);
						if (!resourcesDir.getAsFileTree().isEmpty()) {
							cs.filesMatching(resourcesDir.getAsFileTree().getFiles().stream().map(ff -> "**/" + ff.getPath()).toList(), cd -> {
								logger.warn("Overwriting duplicate file: " + cd.getPath()); // TODO: does not find duplicates!
							});
						}
					});
				}
				// add to source set
				pulled.getJava().srcDir(sourcesDir);
				pulled.getResources().srcDir(resourcesDir);
			});
		});

		// add task to filter sources
		Task filterSources = project.getTasks().create(TASK_NAME_FILTER, t -> {
			t.setGroup(TASK_GROUP);
			t.setDescription("Delete unwanted files from the pulled dependencies before compiling");
			t.doLast(tt -> {
				project.delete(project.fileTree(sourcesDir, ds -> {
					ds.include("**/META-INF/versions/**/*.java");
					ds.visit(d -> logger.warn("Deleting: " + d.getFile().getAbsolutePath())); // TODO: prints random folders???
				}));
			});
			t.dependsOn(pullSources); // run after unpacking
		});

		// run task before compilation
		TaskCollection<JavaCompile> compileJava = project.getTasks().withType(JavaCompile.class);
		compileJava.configureEach(t -> {
			t.dependsOn(pullSources);
			t.dependsOn(filterSources);
		});
	}
}
