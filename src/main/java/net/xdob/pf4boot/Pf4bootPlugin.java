package net.xdob.pf4boot;


import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationPublications;
import org.gradle.api.artifacts.PublishArtifact;
import org.gradle.api.artifacts.UnknownConfigurationException;
import org.gradle.api.artifacts.type.ArtifactTypeDefinition;
import org.gradle.api.component.SoftwareComponentFactory;
import org.gradle.api.file.CopySpec;
import org.gradle.api.internal.artifacts.ArtifactAttributes;
import org.gradle.api.internal.artifacts.dsl.LazyPublishArtifact;
import org.gradle.api.internal.plugins.DefaultArtifactPublicationSet;
import org.gradle.api.internal.project.ProjectInternal;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.plugins.BasePlugin;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.bundling.Jar;
import org.gradle.api.tasks.bundling.Zip;

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


    Configuration inline = project.getConfigurations().register("inline").getOrNull();

    try {
      Configuration implementation = project.getConfigurations().getByName("implementation");
      implementation.extendsFrom(inline);
    }catch (UnknownConfigurationException e){
      //ignore UnknownConfigurationException
    }

    try {
      Configuration compile = project.getConfigurations().getByName("compile");
      compile.extendsFrom(inline);
    }catch (UnknownConfigurationException e){
      //ignore UnknownConfigurationException
    }

    try {
      Configuration api = project.getConfigurations().getByName("api");
      api.extendsFrom(inline);
    }catch (UnknownConfigurationException e){
      //ignore UnknownConfigurationException
    }

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
      if (pluginProp.containsKey(PropKeys.PLUGIN_ID)) {
        configureArchivesAndComponent(project, pluginProp, inline);
      }
    });

  }

  private void configureArchivesAndComponent(Project project, final Properties pluginProp,final Configuration inline) {

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
                c.from(inline);
                c.from(libs.resolve(jar.getArchiveFileName().getOrElse("")));
              }
            });

            zip.doFirst(new Action<Task>() {
              @Override
              public void execute(Task task) {
                pluginProp.put(PropKeys.PLUGIN_VERSION, project.getVersion());
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

