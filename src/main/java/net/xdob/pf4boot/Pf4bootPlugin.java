package net.xdob.pf4boot;

import org.gradle.api.Action;
import org.gradle.api.GradleException;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationPublications;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.ProjectDependency;
import org.gradle.api.attributes.Category;
import org.gradle.api.attributes.LibraryElements;
import org.gradle.api.attributes.Usage;
import org.gradle.api.file.CopySpec;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.plugins.BasePlugin;
import org.gradle.api.plugins.JavaLibraryPlugin;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.provider.Property;
import org.gradle.api.Task;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.bundling.Jar;
import org.gradle.api.tasks.bundling.Zip;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * pf4boot plugin.
 */
public class Pf4bootPlugin implements Plugin<Project> {

	static final Logger LOG = Logging.getLogger(Pf4bootPlugin.class);

	private static final String PLUGIN_TASK_NAME = "pf4boot";

	public static final String PF4BOOT_PLUGIN = "pf4bootPlugin";

	public static final String BUNDLE_CONFIG_NAME = "bundle";
	public static final String BUNDLE_ONLY_CONFIG_NAME = "bundleOnly";
	public static final String EMBED_CONFIG_NAME = "embed";
	public static final String PLUGIN_LOCAL_RUNTIME_CLASSPATH_CONFIG_NAME = "pluginLocalRuntimeClasspath";
	public static final String PF4BOOT_DEPENDENCIES_TASK_NAME = "pf4bootDependencies";
	public static final String CHECK_PLUGIN_RUNTIME_CLASSPATH_TASK_NAME = "checkPluginRuntimeClasspath";
	public static final String VERIFY_RELEASE_READINESS_TASK_NAME = "verifyReleaseReadiness";
	public static final String VERIFY_RELEASE_TAG_TASK_NAME = "verifyReleaseTag";
	public static final String PF4BOOT_INFO_TASK_NAME = "pf4bootInfo";

	/**
	 * 独立发布/消费 pf4boot zip 的配置。
	 *
	 * 依赖方可以这样用：
	 *
	 * dependencies {
	 *   pf4bootPlugin project(path: ":xxx", configuration: "pf4bootElements")
	 * }
	 */
	public static final String PF4BOOT_ELEMENTS_CONFIG_NAME = "pf4bootElements";

	private final ObjectFactory objectFactory;

	@Inject
	public Pf4bootPlugin(
			ObjectFactory objectFactory
	) {
		this.objectFactory = objectFactory;
	}

	@Override
	public void apply(Project project) {
		project.getPluginManager().apply(Pf4boot.class);
		project.getPluginManager().apply(JavaLibraryPlugin.class);

		Configuration compileClasspath =
				project.getConfigurations().getByName(JavaPlugin.COMPILE_CLASSPATH_CONFIGURATION_NAME);

		Configuration runtimeClasspath =
				project.getConfigurations().getByName(JavaPlugin.RUNTIME_CLASSPATH_CONFIGURATION_NAME);

		Configuration bundle = project.getConfigurations().create(BUNDLE_CONFIG_NAME, conf -> {
			conf.setCanBeConsumed(false);
			conf.setCanBeResolved(true);
			conf.setTransitive(true);
			conf.setVisible(false);
			conf.setDescription("Dependencies packaged into pf4boot plugin zip, including transitive dependencies.");
		});

		Configuration bundleOnly = project.getConfigurations().create(BUNDLE_ONLY_CONFIG_NAME, conf -> {
			conf.setCanBeConsumed(false);
			conf.setCanBeResolved(true);
			conf.setTransitive(false);
			conf.setVisible(false);
			conf.setDescription("Direct dependencies packaged into pf4boot plugin zip, without transitive dependencies.");
		});

		Configuration embed = project.getConfigurations().create(EMBED_CONFIG_NAME, conf -> {
			conf.setCanBeConsumed(false);
			conf.setCanBeResolved(true);
			conf.setTransitive(true);
			conf.setVisible(false);
			conf.setDescription("Embedded dependencies packaged into pf4boot plugin zip.");
		});

		/*
		 * 只放进编译/运行 classpath。
		 * 不要默认 extendsFrom(api)，否则会把插件内部依赖暴露给消费者。
		 */
		compileClasspath.extendsFrom(bundle, bundleOnly, embed);
		runtimeClasspath.extendsFrom(bundle, bundleOnly, embed);

		Pf4bootPluginExtension extension =
				project.getExtensions().create(PF4BOOT_PLUGIN, Pf4bootPluginExtension.class);
		extension.getCheckRuntimeClasspathOnCheck().convention(false);
		extension.getDuplicateDependencyPolicy().convention("warn");

		Configuration platformClasspath =
				project.getConfigurations().getByName(Pf4boot.PLATFORM_CLASSPATH_CONFIG_NAME);

		Configuration pluginLocalRuntimeClasspath =
				project.getConfigurations().create(PLUGIN_LOCAL_RUNTIME_CLASSPATH_CONFIG_NAME, conf -> {
					conf.setCanBeConsumed(false);
					conf.setCanBeResolved(true);
					conf.setTransitive(true);
					conf.setVisible(false);
					conf.setDescription("Local runtime classpath for pf4boot plugin development and diagnostics.");
				});
		pluginLocalRuntimeClasspath.extendsFrom(platformClasspath);
		configureBundledProjectPlatformApiDependencies(project, bundle, bundleOnly, embed, pluginLocalRuntimeClasspath);

		TaskProvider<Zip> pf4bootTask =
				configurePf4bootTask(project, extension, bundle, bundleOnly, embed);

		configurePf4bootElements(project, pf4bootTask);
		configureDiagnosticTasks(project, extension, pf4bootTask, bundle, bundleOnly, embed, platformClasspath, pluginLocalRuntimeClasspath);
	}

	private void configureBundledProjectPlatformApiDependencies(
			Project project,
			Configuration bundle,
			Configuration bundleOnly,
			Configuration embed,
			Configuration pluginLocalRuntimeClasspath
	) {
		project.afterEvaluate(evaluated -> {
			Set<Project> bundledProjects = collectBundledProjects(bundle, bundleOnly, embed);
			for (Project bundledProject : bundledProjects) {
				if (bundledProject.getConfigurations().findByName(Pf4boot.PLATFORM_API_CONFIG_NAME) == null) {
					continue;
				}
				Configuration bundledPlatformApi =
						bundledProject.getConfigurations().getByName(Pf4boot.PLATFORM_API_CONFIG_NAME);
				for (Dependency dependency : bundledPlatformApi.getDependencies()) {
					pluginLocalRuntimeClasspath.getDependencies().add(dependency.copy());
				}
			}
		});
	}

	private Set<Project> collectBundledProjects(Configuration... configurations) {
		Set<Project> projects = new LinkedHashSet<>();
		for (Configuration configuration : configurations) {
			collectBundledProjects(configuration, projects);
		}
		return projects;
	}

	private void collectBundledProjects(Configuration configuration, Set<Project> projects) {
		for (Dependency dependency : configuration.getAllDependencies()) {
			if (dependency instanceof ProjectDependency) {
				Project dependencyProject = ((ProjectDependency) dependency).getDependencyProject();
				if (projects.add(dependencyProject)) {
					Configuration runtimeClasspath =
							dependencyProject.getConfigurations().findByName(JavaPlugin.RUNTIME_CLASSPATH_CONFIGURATION_NAME);
					if (runtimeClasspath != null) {
						collectBundledProjects(runtimeClasspath, projects);
					}
				}
			}
		}
	}

	private Properties loadPluginProperties(Project project) {
		Properties properties = new Properties();

		File file = project.file("plugin.properties");
		if (!file.exists()) {
			return properties;
		}

		try (Reader reader = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {
			properties.load(reader);
		} catch (IOException e) {
			throw new GradleException("Failed to read plugin.properties: " + file.getAbsolutePath(), e);
		}

		return properties;
	}

	private TaskProvider<Zip> configurePf4bootTask(
			Project project,
			Pf4bootPluginExtension extension,
			Configuration bundle,
			Configuration bundleOnly,
			Configuration embed
	) {
		TaskProvider<Jar> jarTask = project.getTasks().named(JavaPlugin.JAR_TASK_NAME, Jar.class);
		File pluginPropertiesFile = project.file("plugin.properties");

		return project.getTasks().register(PLUGIN_TASK_NAME, Zip.class, zip -> {
			zip.getInputs().property("project.version", String.valueOf(project.getVersion()));
			zip.getInputs().property(
					"pf4bootPlugin.propertiesFile",
					project.getProviders().provider(() -> readOptionalFileContent(pluginPropertiesFile))
			);
			zip.getInputs().property("pf4bootPlugin.id", project.getProviders().provider(() -> extension.getId().getOrElse("")));
			zip.getInputs().property("pf4bootPlugin.pluginClass", project.getProviders().provider(() -> extension.getPluginClass().getOrElse("")));
			zip.getInputs().property("pf4bootPlugin.version", project.getProviders().provider(() -> extension.getVersion().getOrElse("")));
			zip.getInputs().property("pf4bootPlugin.provider", project.getProviders().provider(() -> extension.getProvider().getOrElse("")));
			zip.getInputs().property("pf4bootPlugin.description", project.getProviders().provider(() -> extension.getDescription().getOrElse("")));
			zip.getInputs().property("pf4bootPlugin.dependencies", project.getProviders().provider(() -> extension.getDependencies().getOrElse("")));
			zip.getInputs().property("pf4bootPlugin.requires", project.getProviders().provider(() -> extension.getRequires().getOrElse("")));
			zip.getInputs().property("pf4bootPlugin.license", project.getProviders().provider(() -> extension.getLicense().getOrElse("")));

			zip.setGroup(BasePlugin.BUILD_GROUP);
			zip.setDescription("Build pf4boot plugin package.");

			zip.dependsOn(jarTask);

			zip.getDestinationDirectory().set(project.getLayout().getBuildDirectory().dir("libs"));
			//zip.getArchiveClassifier().set("pf4boot");
			zip.getArchiveExtension().set("zip");
			zip.getOutputs().file(zip.getArchiveFile());

			Path generatedPluginPropertiesPath =
					project.getLayout()
							.getBuildDirectory()
							.file("generated/pf4boot/plugin.properties")
							.get()
							.getAsFile()
							.toPath();

			zip.doFirst(new Action<Task>() {
				@Override
				public void execute(Task task) {
					try {
						Properties effectiveProperties = resolveEffectiveProperties(project, extension);
						Files.createDirectories(generatedPluginPropertiesPath.getParent());
						try (Writer out = Files.newBufferedWriter(generatedPluginPropertiesPath, StandardCharsets.UTF_8)) {
							effectiveProperties.store(out, "Auto create for Pf4boot Plugin");
						}
					} catch (IOException e) {
						throw new GradleException("Failed to write plugin.properties", e);
					}
				}
			});

			zip.from(generatedPluginPropertiesPath.toFile());

			zip.into("lib", new Action<CopySpec>() {
				@Override
				public void execute(CopySpec copySpec) {
					copySpec.from(jarTask.flatMap(Jar::getArchiveFile));
					copySpec.from(bundle);
					copySpec.from(bundleOnly);
					copySpec.from(embed);
				}
			});

			zip.doLast(new Action<Task>() {
				@Override
				public void execute(Task task) {
					Properties effectiveProperties = resolveEffectiveProperties(project, extension);
					LOG.lifecycle("Built pf4boot plugin for {}.", effectiveProperties.get(PropKeys.PLUGIN_ID));
					LOG.lifecycle("pf4boot zip: {}", zip.getArchiveFile().get().getAsFile().getAbsolutePath());
					LOG.lifecycle("effective plugin properties:\n{}",
							effectiveProperties.stringPropertyNames().stream()
									.sorted()
									.map(name -> "  " + name + "=" + effectiveProperties.getProperty(name))
									.collect(Collectors.joining(System.lineSeparator())));
					LOG.lifecycle("packaged libs:\n  jar={}\n  bundle={}\n  bundleOnly={}\n  embed={}",
							jarTask.get().getArchiveFile().get().getAsFile().getName(),
							formatFiles(bundle),
							formatFiles(bundleOnly),
							formatFiles(embed));
				}
			});
		});
	}

	private Properties resolveEffectiveProperties(Project project, Pf4bootPluginExtension extension) {
		Properties effectiveProperties = new Properties();
		effectiveProperties.putAll(loadPluginProperties(project));
		handlePluginConfig(extension, effectiveProperties);

		if (isNullOrEmpty(effectiveProperties.getProperty(PropKeys.PLUGIN_VERSION))) {
			String projectVersion = String.valueOf(project.getVersion());
			if (!isUnspecified(projectVersion)) {
				effectiveProperties.put(PropKeys.PLUGIN_VERSION, projectVersion);
			}
		}

		validateRequiredPluginProperties(effectiveProperties);
		return effectiveProperties;
	}

	private String readOptionalFileContent(File file) {
		if (!file.exists()) {
			return "";
		}

		try {
			return new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
		} catch (IOException e) {
			throw new GradleException("Failed to read plugin.properties: " + file.getAbsolutePath(), e);
		}
	}

	private String formatFiles(Configuration configuration) {
		return configuration.getFiles().stream()
				.map(File::getName)
				.sorted()
				.collect(Collectors.joining(", "));
	}

	private void configurePf4bootElements(Project project, TaskProvider<Zip> pf4bootTask) {
		Configuration pf4bootElements =
				project.getConfigurations().create(PF4BOOT_ELEMENTS_CONFIG_NAME, conf -> {
					conf.setCanBeConsumed(true);
					conf.setCanBeResolved(false);
					conf.setVisible(false);
					conf.setDescription("Consumable pf4boot plugin zip artifact.");

					conf.getAttributes().attribute(
							Usage.USAGE_ATTRIBUTE,
							objectFactory.named(Usage.class, Usage.JAVA_RUNTIME)
					);

					conf.getAttributes().attribute(
							Category.CATEGORY_ATTRIBUTE,
							objectFactory.named(Category.class, Category.LIBRARY)
					);

					conf.getAttributes().attribute(
							LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE,
							objectFactory.named(LibraryElements.class, "zip")
					);
				});

		pf4bootElements.getAttributes().attribute(
				Pf4boot.PF4BOOT_ARTIFACT_ATTRIBUTE,
				"zip"
		);
		ConfigurationPublications publications = pf4bootElements.getOutgoing();
		publications.artifact(pf4bootTask);
	}

	private void configureDiagnosticTasks(
			Project project,
			Pf4bootPluginExtension extension,
			TaskProvider<Zip> pf4bootTask,
			Configuration bundle,
			Configuration bundleOnly,
			Configuration embed,
			Configuration platformClasspath,
			Configuration pluginLocalRuntimeClasspath
	) {
		project.getTasks().register(PF4BOOT_DEPENDENCIES_TASK_NAME, task -> {
			task.setGroup("help");
			task.setDescription("Print pf4boot packaged/platform/local runtime dependency report.");
			task.doLast(t -> {
				DependencyReport report = buildDependencyReport(bundle, bundleOnly, embed, platformClasspath, pluginLocalRuntimeClasspath);
				LOG.lifecycle(DependencyReporter.formatReport(report));
				DependencyReporter.logDuplicateWarnings(LOG, report);
			});
		});

		TaskProvider<Task> checkRuntimeTask = project.getTasks().register(CHECK_PLUGIN_RUNTIME_CLASSPATH_TASK_NAME, task -> {
			task.setGroup("verification");
			task.setDescription("Check pf4boot plugin local runtime dependency boundaries.");
			task.doLast(t -> {
				DependencyReport report = buildDependencyReport(bundle, bundleOnly, embed, platformClasspath, pluginLocalRuntimeClasspath);
				validateDuplicatePolicy(extension, report);
				validateMissingPlatformInLocalRuntime(report);
				validateKnownBytecodeReferences(report);
				LOG.lifecycle("pf4boot runtime classpath check passed.");
			});
		});

		project.afterEvaluate(evaluated -> {
			if (extension.getCheckRuntimeClasspathOnCheck().getOrElse(false)) {
				project.getTasks().named("check").configure(task -> task.dependsOn(checkRuntimeTask));
			}
		});

		project.getTasks().register(PF4BOOT_INFO_TASK_NAME, task -> {
			task.setGroup("help");
			task.setDescription("Print effective pf4boot plugin metadata and dependency counts.");
			task.doLast(t -> {
				Properties properties = resolveEffectiveProperties(project, extension);
				DependencyReport report = buildDependencyReport(bundle, bundleOnly, embed, platformClasspath, pluginLocalRuntimeClasspath);
				LOG.lifecycle("pf4boot info:\n  id={}\n  class={}\n  version={}\n  zip={}\n  packagedDependencies={}\n  platformDependencies={}\n  localRuntimeDependencies={}",
						properties.getProperty(PropKeys.PLUGIN_ID),
						properties.getProperty(PropKeys.PLUGIN_CLASS),
						properties.getProperty(PropKeys.PLUGIN_VERSION),
						pf4bootTask.get().getArchiveFile().get().getAsFile().getAbsolutePath(),
						report.packagedArtifacts().size(),
						report.getPlatformArtifacts().size(),
						report.getLocalRuntimeArtifacts().size());
			});
		});

		project.getTasks().register(VERIFY_RELEASE_READINESS_TASK_NAME, task -> {
			task.setGroup("verification");
			task.setDescription("Verify release version, docs and pf4boot zip content before publishing.");
			task.dependsOn(pf4bootTask);
			task.doLast(t -> verifyReleaseReadiness(project, pf4bootTask.get().getArchiveFile().get().getAsFile()));
		});

		project.getTasks().register(VERIFY_RELEASE_TAG_TASK_NAME, task -> {
			task.setGroup("verification");
			task.setDescription("Verify release tag exists and points to current HEAD.");
			task.doLast(t -> verifyReleaseTag(project));
		});
	}

	private DependencyReport buildDependencyReport(
			Configuration bundle,
			Configuration bundleOnly,
			Configuration embed,
			Configuration platformClasspath,
			Configuration pluginLocalRuntimeClasspath
	) {
		return DependencyReporter.buildReport(bundle, bundleOnly, embed, platformClasspath, pluginLocalRuntimeClasspath);
	}

	private void validateDuplicatePolicy(Pf4bootPluginExtension extension, DependencyReport report) {
		String policy = extension.getDuplicateDependencyPolicy().getOrElse("warn").trim().toLowerCase();
		if ("ignore".equals(policy)) {
			return;
		}
		if ("fail".equals(policy) && !report.getDuplicateModuleKeys().isEmpty()) {
			throw new GradleException("Duplicate pf4boot dependencies found in packaged and platform classpaths: "
					+ String.join(", ", report.getDuplicateModuleKeys()));
		}
		if (!"warn".equals(policy) && !"fail".equals(policy)) {
			throw new GradleException("Invalid duplicateDependencyPolicy: " + policy + ". Expected warn, fail, or ignore.");
		}
		DependencyReporter.logDuplicateWarnings(LOG, report);
	}

	private void validateMissingPlatformInLocalRuntime(DependencyReport report) {
		if (!report.getMissingPlatformArtifactsInLocalRuntime().isEmpty()) {
			throw new GradleException("Missing platform runtime dependency in pluginLocalRuntimeClasspath:\n"
					+ report.getMissingPlatformArtifactsInLocalRuntime().stream()
					.map(artifact -> "- " + artifact.coordinate())
					.collect(Collectors.joining(System.lineSeparator()))
					+ System.lineSeparator()
					+ "Suggested fixes:\n"
					+ "- keep platformApi(\"<group>:<name>:<version>\") in the plugin project\n"
					+ "- use sourceSets.main.runtimeClasspath + configurations.pluginLocalRuntimeClasspath for JavaExec/IDE local runs\n"
					+ "- or declare runtimeOnly(\"<group>:<name>:<version>\") in the standalone runnable library");
		}
	}

	private void validateKnownBytecodeReferences(DependencyReport report) {
		Set<String> availableModuleKeys = new TreeSet<>();
		report.packagedArtifacts().stream().filter(ResolvedArtifactInfo::hasModuleIdentity).map(ResolvedArtifactInfo::moduleKey).forEach(availableModuleKeys::add);
		report.getPlatformArtifacts().stream().filter(ResolvedArtifactInfo::hasModuleIdentity).map(ResolvedArtifactInfo::moduleKey).forEach(availableModuleKeys::add);
		report.getLocalRuntimeArtifacts().stream().filter(ResolvedArtifactInfo::hasModuleIdentity).map(ResolvedArtifactInfo::moduleKey).forEach(availableModuleKeys::add);
		Set<String> missing = BytecodeDependencyScanner.findMissingKnownModules(report.packagedArtifacts(), availableModuleKeys);
		if (!missing.isEmpty()) {
			throw new GradleException("Missing known platform API referenced by packaged bytecode:\n- "
					+ String.join(System.lineSeparator() + "- ", missing));
		}
	}

	private void verifyReleaseReadiness(Project project, File zipFile) {
		String version = String.valueOf(project.getVersion());
		if (isNullOrEmpty(version) || version.endsWith("SNAPSHOT")) {
			throw new GradleException("Release version must be explicit and must not be SNAPSHOT. current=" + version);
		}
		assertFileContains(project.file("CHANGELOG.md"), "## [" + version + "]");
		assertFileContains(project.file("CHANGELOG_EN.md"), "## [" + version + "]");
		assertFileContains(project.file("README.md"), "pf4boot-plugin:" + version);
		assertFileContains(project.file("README_EN.md"), "pf4boot-plugin:" + version);
		assertFileContains(project.file("docs/usage-zh.md"), "pf4boot-plugin:" + version);
		assertFileContains(project.file("docs/usage-en.md"), "pf4boot-plugin:" + version);
		assertZipContains(zipFile, "plugin.properties");
		assertZipHasLib(zipFile);
	}

	private void verifyReleaseTag(Project project) {
		String version = String.valueOf(project.getVersion());
		String tag = "v" + version;
		String head = runGit(project, "rev-parse", "HEAD");
		String tagHead = runGit(project, "rev-list", "-n", "1", tag);
		if (!head.equals(tagHead)) {
			throw new GradleException("Release tag " + tag + " does not point to HEAD. tag=" + tagHead + ", head=" + head);
		}
	}

	private String runGit(Project project, String... args) {
		try {
			java.util.List<String> command = new java.util.ArrayList<>();
			command.add("git");
			for (String arg : args) {
				command.add(arg);
			}
			Process process = new ProcessBuilder(command)
					.directory(project.getRootDir())
					.redirectErrorStream(true)
					.start();
			byte[] output = readProcessOutput(process.getInputStream());
			int exit = process.waitFor();
			String text = new String(output, StandardCharsets.UTF_8).trim();
			if (exit != 0) {
				throw new GradleException("Failed to run git " + String.join(" ", args) + ": " + text);
			}
			return text;
		} catch (IOException | InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new GradleException("Failed to run git command.", e);
		}
	}

	private byte[] readProcessOutput(java.io.InputStream input) throws IOException {
		try {
			java.io.ByteArrayOutputStream output = new java.io.ByteArrayOutputStream();
			byte[] buffer = new byte[1024];
			int read;
			while ((read = input.read(buffer)) >= 0) {
				output.write(buffer, 0, read);
			}
			return output.toByteArray();
		} finally {
			input.close();
		}
	}

	private void assertFileContains(File file, String expected) {
		if (!file.exists()) {
			throw new GradleException("Required release document does not exist: " + file.getAbsolutePath());
		}
		try {
			String content = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
			if (!content.contains(expected)) {
				throw new GradleException("Required release document " + file.getPath() + " does not contain: " + expected);
			}
		} catch (IOException e) {
			throw new GradleException("Failed to read release document: " + file.getAbsolutePath(), e);
		}
	}

	private void assertZipContains(File zipFile, String entryName) {
		try (java.util.zip.ZipFile zip = new java.util.zip.ZipFile(zipFile)) {
			if (zip.getEntry(entryName) == null) {
				throw new GradleException("Release zip does not contain " + entryName + ": " + zipFile.getAbsolutePath());
			}
		} catch (IOException e) {
			throw new GradleException("Failed to inspect release zip: " + zipFile.getAbsolutePath(), e);
		}
	}

	private void assertZipHasLib(File zipFile) {
		try (java.util.zip.ZipFile zip = new java.util.zip.ZipFile(zipFile)) {
			boolean hasLib = zip.stream().anyMatch(entry -> entry.getName().startsWith("lib/"));
			if (!hasLib) {
				throw new GradleException("Release zip does not contain lib/: " + zipFile.getAbsolutePath());
			}
		} catch (IOException e) {
			throw new GradleException("Failed to inspect release zip: " + zipFile.getAbsolutePath(), e);
		}
	}

	private boolean isNullOrEmpty(String s) {
		return s == null || s.trim().isEmpty();
	}

	private void setProperty(Properties pluginProperties, String name, Property<String> property) {
		if (property != null && property.isPresent()) {
			String value = property.get();
			if (!isNullOrEmpty(value)) {
				pluginProperties.put(name, value);
			}
		}
	}

	private void handlePluginConfig(
			Pf4bootPluginExtension extension,
			Properties pluginProperties
	) {
		setProperty(pluginProperties, PropKeys.PLUGIN_ID, extension.getId());
		setProperty(pluginProperties, PropKeys.PLUGIN_CLASS, extension.getPluginClass());

		setProperty(pluginProperties, PropKeys.PLUGIN_PROVIDER, extension.getProvider());
		setProperty(pluginProperties, PropKeys.PLUGIN_DESCRIPTION, extension.getDescription());
		setProperty(pluginProperties, PropKeys.PLUGIN_DEPENDENCIES, extension.getDependencies());
		setProperty(pluginProperties, PropKeys.PLUGIN_REQUIRES, extension.getRequires());
		setProperty(pluginProperties, PropKeys.PLUGIN_LICENSE, extension.getLicense());

		if (isNullOrEmpty(pluginProperties.getProperty(PropKeys.PLUGIN_VERSION))) {
			setProperty(pluginProperties, PropKeys.PLUGIN_VERSION, extension.getVersion());
		}
	}

	private void validateRequiredPluginProperties(Properties pluginProperties) {
		String pluginId = pluginProperties.getProperty(PropKeys.PLUGIN_ID);
		String pluginClass = pluginProperties.getProperty(PropKeys.PLUGIN_CLASS);
		String pluginVersion = pluginProperties.getProperty(PropKeys.PLUGIN_VERSION);

		if (isNullOrEmpty(pluginId)) {
			throwInvalidProperty(PropKeys.PLUGIN_ID, pluginId, "Configure plugin.id in plugin.properties or pf4bootPlugin.id.");
		}

		if (isNullOrEmpty(pluginClass)) {
			throwInvalidProperty(PropKeys.PLUGIN_CLASS, pluginClass, "Configure plugin.class in plugin.properties or pf4bootPlugin.pluginClass.");
		}

		if (isNullOrEmpty(pluginVersion)) {
			throwInvalidProperty(
					PropKeys.PLUGIN_VERSION,
					pluginVersion,
					"Set plugin.version in plugin.properties or pf4bootPlugin.version, or keep project.version when it is explicit."
			);
		}

		if (isUnspecified(pluginVersion)) {
			throwInvalidProperty(
					PropKeys.PLUGIN_VERSION,
					pluginVersion,
					"Avoid using 'unspecified'. Set an explicit plugin.version."
			);
		}
	}

	private boolean isUnspecified(String value) {
		return "unspecified".equalsIgnoreCase(Optional.ofNullable(value).orElse("").trim());
	}

	private void throwInvalidProperty(String key, String value, String suggestion) {
		throw new GradleException(
				String.format(
						"Invalid pf4boot plugin property '%s'. current=%s. %s",
						key,
						Optional.ofNullable(value).filter(v -> !v.trim().isEmpty()).orElse("<empty>"),
						suggestion
				)
		);
	}
}
