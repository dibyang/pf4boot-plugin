package net.xdob.pf4boot;


import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.*;
import org.gradle.api.artifacts.type.ArtifactTypeDefinition;
import org.gradle.api.component.AdhocComponentWithVariants;
import org.gradle.api.component.SoftwareComponentFactory;
import org.gradle.api.file.FileCollection;
import org.gradle.api.internal.artifacts.ArtifactAttributes;
import org.gradle.api.internal.artifacts.dsl.LazyPublishArtifact;
import org.gradle.api.internal.component.BuildableJavaComponent;
import org.gradle.api.internal.component.ComponentRegistry;
import org.gradle.api.internal.plugins.DefaultArtifactPublicationSet;
import org.gradle.api.internal.project.ProjectInternal;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.plugins.BasePlugin;
import org.gradle.api.plugins.JavaBasePlugin;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.plugins.internal.JavaConfigurationVariantMapping;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.Copy;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.bundling.Jar;
import org.gradle.api.tasks.bundling.Zip;
import org.gradle.api.tasks.bundling.ZipEntryCompression;
import org.gradle.internal.cleanup.BuildOutputCleanupRegistry;
import org.gradle.language.jvm.tasks.ProcessResources;

import javax.inject.Inject;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;

/**
 * pf4boot plugin.
 */
public class Pf4bootPlugin implements Plugin<ProjectInternal> {



  private static final String PLUGIN_TASK_NAME = "pf4boot";

  private final ObjectFactory objectFactory;
  private final SoftwareComponentFactory softwareComponentFactory;

  @Inject
  public Pf4bootPlugin(ObjectFactory objectFactory, SoftwareComponentFactory softwareComponentFactory) {
    this.objectFactory = objectFactory;
    this.softwareComponentFactory = softwareComponentFactory;
  }

  public void apply(ProjectInternal project) {

    project.getPluginManager().apply(JavaPlugin.class);


    Pf4bootPluginExtension pf4bootPlugin = project.getExtensions().create("pf4bootPlugin", Pf4bootPluginExtension.class);


    Configuration inline = project.getConfigurations().register("inline").getOrNull();

    Configuration compile = project.getConfigurations().getByName("compile");
    compile.extendsFrom(inline);
    project.getConfigurations().register("plugin", plugin -> {
      plugin.setTransitive(false);
    });

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
    project.afterEvaluate(p->{
      if (pf4bootPlugin.getId().getOrNull()!=null) {
        pluginProp.put(PropKeys.PLUGIN_ID, pf4bootPlugin.getId().getOrElse(""));
        pluginProp.put(PropKeys.PLUGIN_CLASS, pf4bootPlugin.getPluginClass().getOrElse(""));
        pluginProp.put(PropKeys.PLUGIN_PROVIDER, pf4bootPlugin.getProvider().getOrElse(""));
        pluginProp.put(PropKeys.PLUGIN_DESCRIPTION, pf4bootPlugin.getDescription().getOrElse(""));
        pluginProp.put(PropKeys.PLUGIN_DEPENDENCIES, pf4bootPlugin.getDependencies().getOrElse(""));
        pluginProp.put(PropKeys.PLUGIN_REQUIRES, pf4bootPlugin.getRequires().getOrElse(""));
        pluginProp.put(PropKeys.PLUGIN_LICENSE, pf4bootPlugin.getLicense().getOrElse(""));
      }
      if (pluginProp.containsKey(PropKeys.PLUGIN_ID)) {
        configureArchivesAndComponent(project, pluginProp, inline);
      }
    });

  }

  private void configureArchivesAndComponent(Project project, final Properties pluginProp,final Configuration inline) {

    final Jar jar = (Jar) project.getTasks().getByName("jar");

    // Register a task
    TaskProvider<Zip> pf4boot = project.getTasks().register(PLUGIN_TASK_NAME, Zip.class, task -> {
      task.dependsOn(jar);
      task.setGroup(BasePlugin.BUILD_GROUP);
      Path libs = project.getBuildDir().toPath().resolve("libs");
      task.getDestinationDirectory().set(
          project.file(libs.toFile()));
      final Path plugin_prop_path = project.getBuildDir().toPath().resolve("tmp/plugin.properties");
      task.from(plugin_prop_path);
      task.into("lib", c -> {
        c.from(inline);
        c.from(libs.resolve(jar.getArchiveFileName().getOrElse("")));
      });
      task.doFirst(t -> {
        pluginProp.put(PropKeys.PLUGIN_VERSION, project.getVersion());
        try (OutputStream out = Files.newOutputStream(plugin_prop_path)) {
          pluginProp.store(out, "Auto create for Pf4boot Plugin");
        } catch (IOException e) {
          //e.printStackTrace();
        }
      });
      task.doLast(s -> {
        String pluginId = pluginProp.getProperty(PropKeys.PLUGIN_ID);
        if (pluginId != null) {
          System.out.println("build pf4boot plugin for " + pluginId + ".");
        }
      });
    });

    PublishArtifact pf4bootArtifact = new LazyPublishArtifact(pf4boot);
    Configuration apiElementConfiguration = project.getConfigurations().getByName(JavaPlugin.API_ELEMENTS_CONFIGURATION_NAME);
    Configuration runtimeConfiguration = project.getConfigurations().getByName(JavaPlugin.RUNTIME_CONFIGURATION_NAME);
    Configuration runtimeElementsConfiguration = project.getConfigurations().getByName(JavaPlugin.RUNTIME_ELEMENTS_CONFIGURATION_NAME);

    project.getExtensions().getByType(DefaultArtifactPublicationSet.class).addCandidate(pf4bootArtifact);


    addZip(apiElementConfiguration, pf4bootArtifact);
    addZip(runtimeConfiguration, pf4bootArtifact);
    addRuntimeVariants(project, runtimeElementsConfiguration, pf4bootArtifact);

    //registerSoftwareComponents(project);
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
    publications.getAttributes().attribute(ArtifactAttributes.ARTIFACT_FORMAT, ArtifactTypeDefinition.JAR_TYPE);

  }

}

