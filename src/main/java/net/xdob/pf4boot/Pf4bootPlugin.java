package net.xdob.pf4boot;


import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.*;
import org.gradle.api.artifacts.type.ArtifactTypeDefinition;
import org.gradle.api.component.SoftwareComponentFactory;
import org.gradle.api.file.CopySpec;
import org.gradle.api.internal.artifacts.ArtifactAttributes;
import org.gradle.api.internal.artifacts.dsl.LazyPublishArtifact;
import org.gradle.api.internal.plugins.DefaultArtifactPublicationSet;
import org.gradle.api.internal.project.ProjectInternal;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.plugins.BasePlugin;
import org.gradle.api.plugins.JavaLibraryPlugin;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.bundling.Jar;
import org.gradle.api.tasks.bundling.Zip;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public class Pf4bootPlugin implements Plugin<ProjectInternal> {

  static final Logger LOG = LoggerFactory.getLogger(Pf4bootPlugin.class);

  private static final String PLUGIN_TASK_NAME = "pf4boot";
  public static final String PF4BOOT_PLUGIN = "pf4bootPlugin";

  private final ObjectFactory objectFactory;
  private final SoftwareComponentFactory softwareComponentFactory;

  @Inject
  public Pf4bootPlugin(ObjectFactory objectFactory, SoftwareComponentFactory softwareComponentFactory) {
    this.objectFactory = objectFactory;
    this.softwareComponentFactory = softwareComponentFactory;
  }

  public void apply(ProjectInternal project) {
    project.getPluginManager().apply(JavaPlugin.class);
    project.getPluginManager().apply(JavaLibraryPlugin.class);
    //project.getPluginManager().apply(JavaPlugin.class);



    Configuration implementation = project.getConfigurations().getByName(JavaPlugin.IMPLEMENTATION_CONFIGURATION_NAME);

    //Configuration apiElements = project.getConfigurations().getByName(JavaPlugin.API_ELEMENTS_CONFIGURATION_NAME);
    Configuration api = project.getConfigurations().getByName(JavaPlugin.API_CONFIGURATION_NAME);

    Configuration runtimeClasspath = project.getConfigurations().getByName(JavaPlugin.RUNTIME_CLASSPATH_CONFIGURATION_NAME);
    Configuration compileClasspath = project.getConfigurations().getByName(JavaPlugin.COMPILE_CLASSPATH_CONFIGURATION_NAME);


    Configuration bundle = project.getConfigurations().register("bundle",plugin -> {
      plugin.setCanBeConsumed(false);
      plugin.setCanBeResolved(true);
      plugin.setTransitive(true);
      plugin.setVisible(false);
    }).get();

    Configuration bundleOnly = project.getConfigurations().register("bundleOnly",plugin -> {
      plugin.setCanBeConsumed(false);
      plugin.setCanBeResolved(true);
      plugin.setTransitive(false);
      plugin.setVisible(false);
    }).get();
    bundleOnly.extendsFrom(bundle);


    compileClasspath.extendsFrom(bundle);
    runtimeClasspath.extendsFrom(bundle);
    api.extendsFrom(bundle);

    Configuration embed = project.getConfigurations().register("embed",plugin -> {
      plugin.setCanBeConsumed(false);
      plugin.setCanBeResolved(true);
      plugin.setTransitive(true);
      plugin.setVisible(false);
    }).get();

    compileClasspath.extendsFrom(embed);
    runtimeClasspath.extendsFrom(embed);
    api.extendsFrom(embed);

    Configuration plugin = project.getConfigurations().register("plugin",p -> {
      p.setCanBeConsumed(true);
      p.setCanBeResolved(true);
      p.setTransitive(true);
      p.setVisible(false);
    }).get();


    Configuration compileOnlyApi = project.getConfigurations().getByName(JavaPlugin.COMPILE_ONLY_API_CONFIGURATION_NAME);
    compileOnlyApi.extendsFrom(plugin);


    final Properties pluginProp = new Properties();
    File file = project.file("plugin.properties");
    if (file.exists()) {
      try (FileReader reader = new FileReader(file)) {
        pluginProp.load(reader);
      } catch (IOException e) {
        //e.printStackTrace();
        pluginProp.clear();
      }
    }
    Pf4bootPluginExtension pf4bootPlugin = project.getExtensions().create(PF4BOOT_PLUGIN, Pf4bootPluginExtension.class);

    configureArchivesAndComponent(project, pf4bootPlugin, pluginProp, bundleOnly, embed);
  }

  private boolean isNullOrEmpty(String s){
    return s==null || s.isEmpty();
  }

  private void setProperty(Properties pluginProp, String name, Property<String> property) {
    if(property.isPresent()) {
      pluginProp.put(name, property.get());
    }
  }

  private void configureArchivesAndComponent(Project project, final Pf4bootPluginExtension pf4bootPlugin,
                                             final Properties pluginProp,final Configuration... configs) {

    final Jar jar = (Jar) project.getTasks().getByName("jar");

    // Register a task
    TaskProvider<Zip> pf4boot = project.getTasks().register(PLUGIN_TASK_NAME, Zip.class, new Action<Zip>() {
          @Override
          public void execute(Zip zip) {
            zip.dependsOn(jar);
            zip.setGroup(BasePlugin.BUILD_GROUP);
            Path libs = project.getBuildDir().toPath().resolve("libs");
            zip.getDestinationDirectory().set(
                project.file(libs.toFile()));
            final Path plugin_prop_path = project.getBuildDir().toPath().resolve("tmp/plugin.properties");
            zip.from(plugin_prop_path);
            zip.into("lib", new Action<CopySpec>() {
              @Override
              public void execute(CopySpec c) {
                for (Configuration config : configs) {
                  c.from(config);
                }
                c.from(libs.resolve(jar.getArchiveFileName().getOrElse("")));
              }
            });

            zip.doFirst(new Action<Task>() {
              @Override
              public void execute(Task task) {
                handlePluginConfig(pf4bootPlugin, pluginProp);
                if(isNullOrEmpty(pluginProp.getProperty(PropKeys.PLUGIN_VERSION))) {
                  pluginProp.put(PropKeys.PLUGIN_VERSION, project.getVersion());
                }
                try (OutputStream out = Files.newOutputStream(plugin_prop_path)) {
                  pluginProp.store(out, "Auto create for Pf4boot Plugin");
                } catch (IOException e) {
                  //e.printStackTrace();
                }
              }
            });

            zip.doLast(new Action<Task>() {
              @Override
              public void execute(Task task) {
                String pluginId = pluginProp.getProperty(PropKeys.PLUGIN_ID);
                if (pluginId != null) {
                  System.out.println("build pf4boot plugin for " + pluginId + ".");
                }
              }
            });
          }
        });

    PublishArtifact pf4bootArtifact = new LazyPublishArtifact(pf4boot);
    Configuration apiElementConfiguration = project.getConfigurations().getByName(JavaPlugin.API_ELEMENTS_CONFIGURATION_NAME);
    Configuration runtimeConfiguration = project.getConfigurations().getByName(JavaPlugin.RUNTIME_CLASSPATH_CONFIGURATION_NAME);
    Configuration runtimeElementsConfiguration = project.getConfigurations().getByName(JavaPlugin.RUNTIME_ELEMENTS_CONFIGURATION_NAME);

    project.getExtensions().getByType(DefaultArtifactPublicationSet.class).addCandidate(pf4bootArtifact);

//    runtimeElementsConfiguration.getOutgoing().getArtifacts()
//        .removeIf(e->e.getType().equals(ArtifactTypeDefinition.JAR_TYPE));

    addZip(apiElementConfiguration, pf4bootArtifact);
    addZip(runtimeConfiguration, pf4bootArtifact);
    addRuntimeVariants(project, runtimeElementsConfiguration, pf4bootArtifact);

    //registerSoftwareComponents(project);
  }

  private void handlePluginConfig( Pf4bootPluginExtension pf4bootPlugin, Properties pluginProp) {

    setProperty(pluginProp, PropKeys.PLUGIN_ID, pf4bootPlugin.getId());
    setProperty(pluginProp, PropKeys.PLUGIN_CLASS, pf4bootPlugin.getPluginClass());

    setProperty(pluginProp, PropKeys.PLUGIN_PROVIDER, pf4bootPlugin.getProvider());
    setProperty(pluginProp, PropKeys.PLUGIN_DESCRIPTION, pf4bootPlugin.getDescription());
    setProperty(pluginProp, PropKeys.PLUGIN_DEPENDENCIES, pf4bootPlugin.getDependencies());
    setProperty(pluginProp, PropKeys.PLUGIN_REQUIRES, pf4bootPlugin.getRequires());
    setProperty(pluginProp, PropKeys.PLUGIN_LICENSE, pf4bootPlugin.getLicense());

    if(isNullOrEmpty(pluginProp.getProperty(PropKeys.PLUGIN_VERSION))) {
      Property<String> version = pf4bootPlugin.getVersion();
      if(version.isPresent()){
        pluginProp.put(PropKeys.PLUGIN_VERSION, version.get());
      }
    }
  }


//  private void registerSoftwareComponents(Project project) {
//    ConfigurationContainer configurations = project.getConfigurations();
//    // the main "Java" component
//    AdhocComponentWithVariants java = softwareComponentFactory.adhoc("java");
//    java.addVariantsFromConfiguration(configurations.getByName(JavaPlugin.API_ELEMENTS_CONFIGURATION_NAME), new JavaConfigurationVariantMapping("compile", false));
//    java.addVariantsFromConfiguration(configurations.getByName(JavaPlugin.RUNTIME_ELEMENTS_CONFIGURATION_NAME), new JavaConfigurationVariantMapping("runtime", false));
//    project.getComponents().add(java);
//  }

  private void addZip(Configuration configuration, PublishArtifact zipArtifact) {
    ConfigurationPublications publications = configuration.getOutgoing();

    // Configure an implicit variant
    publications.getArtifacts().add(zipArtifact);
    publications.getAttributes().attribute(ArtifactAttributes.ARTIFACT_FORMAT, ArtifactTypeDefinition.ZIP_TYPE);
  }

  private void addRuntimeVariants(Project project, Configuration configuration, PublishArtifact jarArtifact) {
    ConfigurationPublications publications = configuration.getOutgoing();

    // Configure an implicit variant
    publications.getArtifacts().add(jarArtifact);
    publications.getAttributes().attribute(ArtifactAttributes.ARTIFACT_FORMAT, ArtifactTypeDefinition.ZIP_TYPE);

  }

}

