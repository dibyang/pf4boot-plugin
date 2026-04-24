package net.xdob.pf4boot;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.attributes.Category;
import org.gradle.api.attributes.LibraryElements;
import org.gradle.api.attributes.Usage;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.tasks.bundling.Jar;
import org.gradle.api.attributes.Attribute;

/**
 * pf4boot.
 */
public class Pf4boot implements Plugin<Project> {

	public static final String PLUGIN_CONFIG_NAME = "plugin";
	public static final String PLUGIN_CLASSPATH_CONFIG_NAME = "pluginClasspath";

	public static final String PLATFORM_API_CONFIG_NAME = "platformApi";
	public static final String PLATFORM_CLASSPATH_CONFIG_NAME = "platformClasspath";



	public static final Attribute<String> PF4BOOT_PLUGIN_ATTRIBUTE =
			Attribute.of("net.xdob.pf4boot.plugin", String.class);
	public static final Attribute<String> PF4BOOT_ARTIFACT_ATTRIBUTE =
			Attribute.of("net.xdob.pf4boot.artifact", String.class);

	@Override
	public void apply(Project project) {
		project.getPluginManager().apply(JavaPlugin.class);

		Configuration compileClasspath =
				project.getConfigurations().getByName(JavaPlugin.COMPILE_CLASSPATH_CONFIGURATION_NAME);

		Configuration platformApi =
				project.getConfigurations().register(PLATFORM_API_CONFIG_NAME, conf -> {
					conf.setCanBeConsumed(false);
					conf.setCanBeResolved(false);
					conf.setTransitive(true);
					conf.setVisible(false);
					conf.setDescription("Platform APIs available at compile time.");
				}).get();

		Configuration platformClasspath =
				project.getConfigurations().register(PLATFORM_CLASSPATH_CONFIG_NAME, conf -> {
					conf.setCanBeConsumed(false);
					conf.setCanBeResolved(true);
					conf.setTransitive(true);
					conf.setVisible(false);
					conf.setDescription("Resolved platform classpath.");
				}).get();

		platformClasspath.extendsFrom(platformApi);
		compileClasspath.extendsFrom(platformApi);

		Configuration plugin =
				project.getConfigurations().register(PLUGIN_CONFIG_NAME, conf -> {
					conf.setCanBeConsumed(false);
					conf.setCanBeResolved(false);
					conf.setTransitive(true);
					conf.setVisible(false);
					conf.setDescription("Plugin dependencies.");
				}).get();

		Configuration pluginClasspath =
				project.getConfigurations().register(PLUGIN_CLASSPATH_CONFIG_NAME, conf -> {
					conf.setCanBeConsumed(false);
					conf.setCanBeResolved(true);
					conf.setTransitive(true);
					conf.setVisible(false);
					conf.setDescription("Resolved plugin runtime classpath.");
				}).get();

		pluginClasspath.getAttributes().attribute(
				PF4BOOT_ARTIFACT_ATTRIBUTE,
				"zip"
		);

		pluginClasspath.getAttributes().attribute(
				PF4BOOT_PLUGIN_ATTRIBUTE,
				"true"
		);

		pluginClasspath.extendsFrom(plugin);


	}
}