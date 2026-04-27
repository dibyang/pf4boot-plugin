package net.xdob.pf4boot;

import org.gradle.api.Action;
import org.gradle.api.GradleException;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationPublications;
import org.gradle.api.artifacts.PublishArtifact;
import org.gradle.api.attributes.Category;
import org.gradle.api.attributes.LibraryElements;
import org.gradle.api.attributes.Usage;
import org.gradle.api.file.CopySpec;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.plugins.BasePlugin;
import org.gradle.api.plugins.JavaLibraryPlugin;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.Task;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.bundling.Jar;
import org.gradle.api.tasks.bundling.Zip;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import javax.inject.Inject;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

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

	private final ProviderFactory providerFactory;
	private final ObjectFactory objectFactory;

	@Inject
	public Pf4bootPlugin(
			ProviderFactory providerFactory,
			ObjectFactory objectFactory
	) {
		this.providerFactory = providerFactory;
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

		Properties filePluginProperties = loadPluginProperties(project);

		Pf4bootPluginExtension extension =
				project.getExtensions().create(PF4BOOT_PLUGIN, Pf4bootPluginExtension.class);

		TaskProvider<Zip> pf4bootTask =
				configurePf4bootTask(project, extension, filePluginProperties, bundle, bundleOnly, embed);

		configurePf4bootElements(project, pf4bootTask);
	}

	private Properties loadPluginProperties(Project project) {
		Properties properties = new Properties();

		File file = project.file("plugin.properties");
		if (!file.exists()) {
			return properties;
		}

		try (FileReader reader = new FileReader(file)) {
			properties.load(reader);
		} catch (IOException e) {
			throw new GradleException("Failed to read plugin.properties: " + file.getAbsolutePath(), e);
		}

		return properties;
	}

	private TaskProvider<Zip> configurePf4bootTask(
			Project project,
			Pf4bootPluginExtension extension,
			Properties basePluginProperties,
			Configuration bundle,
			Configuration bundleOnly,
			Configuration embed
	) {
		TaskProvider<Jar> jarTask = project.getTasks().named(JavaPlugin.JAR_TASK_NAME, Jar.class);

		return project.getTasks().register(PLUGIN_TASK_NAME, Zip.class, zip -> {
			zip.setGroup(BasePlugin.BUILD_GROUP);
			zip.setDescription("Build pf4boot plugin package.");

			zip.dependsOn(jarTask);

			zip.getDestinationDirectory().set(project.getLayout().getBuildDirectory().dir("libs"));
			//zip.getArchiveClassifier().set("pf4boot");
			zip.getArchiveExtension().set("zip");

			Path generatedPluginPropertiesPath =
					project.getLayout()
							.getBuildDirectory()
							.file("generated/pf4boot/plugin.properties")
							.get()
							.getAsFile()
							.toPath();

			zip.getInputs().property("project.version", String.valueOf(project.getVersion()));
			zip.getInputs().property("plugin.properties.file", basePluginProperties.toString());

			zip.doFirst(new Action<Task>() {
				@Override
				public void execute(Task task) {
					Properties effectiveProperties = new Properties();
					effectiveProperties.putAll(basePluginProperties);

					handlePluginConfig(extension, effectiveProperties);

					if (isNullOrEmpty(effectiveProperties.getProperty(PropKeys.PLUGIN_VERSION))) {
						effectiveProperties.put(PropKeys.PLUGIN_VERSION, String.valueOf(project.getVersion()));
					}

					validateRequiredPluginProperties(effectiveProperties);

					try {
						Files.createDirectories(generatedPluginPropertiesPath.getParent());
						try (OutputStream out = Files.newOutputStream(generatedPluginPropertiesPath)) {
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
					Properties effectiveProperties = new Properties();
					effectiveProperties.putAll(basePluginProperties);
					handlePluginConfig(extension, effectiveProperties);

					String pluginId = effectiveProperties.getProperty(PropKeys.PLUGIN_ID);
					LOG.lifecycle("built pf4boot plugin for {}.", pluginId);
				}
			});
		});
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
			throw new GradleException("Missing required pf4boot plugin property: " + PropKeys.PLUGIN_ID);
		}

		if (isNullOrEmpty(pluginClass)) {
			throw new GradleException("Missing required pf4boot plugin property: " + PropKeys.PLUGIN_CLASS);
		}

		if (isNullOrEmpty(pluginVersion)) {
			throw new GradleException("Missing required pf4boot plugin property: " + PropKeys.PLUGIN_VERSION);
		}
	}
}