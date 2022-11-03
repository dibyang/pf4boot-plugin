package net.xdob.pf4boot;


import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.bundling.Jar;
import org.gradle.api.tasks.bundling.ZipEntryCompression;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * pf4boot plugin.
 */
public class Pf4bootPlugin implements Plugin<Project> {

  public void apply(Project project) {
    Pf4bootPluginExtension pf4bootPlugin = project.getExtensions().create("pf4bootPlugin", Pf4bootPluginExtension.class);


    Configuration inline = project.getConfigurations().register("inline").getOrNull();

    Configuration compile = project.getConfigurations().getByName("compile");
    compile.extendsFrom(inline);
    project.getConfigurations().register("plugin", plugin -> plugin.setTransitive(false));

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


    final Jar jar = (Jar) project.getTasks().getByName("jar");
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
        jar.setEntryCompression(ZipEntryCompression.STORED);
        jar.from(inline, c -> c.into("lib"));
        Path path = project.getBuildDir().toPath().resolve("classes/java/main/plugin.properties");
        pluginProp.put(PropKeys.PLUGIN_VERSION, project.getVersion());
        try (OutputStream out = Files.newOutputStream(path)) {
          pluginProp.store(out, "Auto create for Pf4boot Plugin");
        } catch (IOException e) {
          //e.printStackTrace();
        }
        jar.manifest(manifest -> {
          Map<String, Object> map = new HashMap<>();
          map.put("Build-Time", LocalDateTime.now());
          handleValue(map, ManifestKeys.PLUGIN_ID, pluginProp, PropKeys.PLUGIN_ID);
          handleValue(map, ManifestKeys.PLUGIN_CLASS, pluginProp, PropKeys.PLUGIN_CLASS);
          handleValue(map, ManifestKeys.PLUGIN_PROVIDER, pluginProp, PropKeys.PLUGIN_PROVIDER);
          handleValue(map, ManifestKeys.PLUGIN_DESCRIPTION, pluginProp, PropKeys.PLUGIN_DESCRIPTION);
          handleValue(map, ManifestKeys.PLUGIN_DEPENDENCIES, pluginProp, PropKeys.PLUGIN_DEPENDENCIES);
          handleValue(map, ManifestKeys.PLUGIN_REQUIRES, pluginProp, PropKeys.PLUGIN_REQUIRES);
          handleValue(map, ManifestKeys.PLUGIN_LICENSE, pluginProp, PropKeys.PLUGIN_LICENSE);
          handleValue(map, ManifestKeys.PLUGIN_VERSION, pluginProp, PropKeys.PLUGIN_VERSION);
          manifest.attributes(map);
        });
      }

      // Register a task
      project.getTasks().register("buildPf4bootPlugin", task -> {
        task.dependsOn(jar);
        task.setGroup("build");

        task.doLast(s -> {
          String pluginId = pluginProp.getProperty(PropKeys.PLUGIN_ID);
          if(pluginId!=null) {
            System.out.println("build pf4boot plugin for " + pluginId + ".");
          }
        });
      });

    });
  }

  private void handleValue(Map<String, Object> map, String manifestKey, Properties pluginProp, String propKey) {
    String property = pluginProp.getProperty(propKey);
    if(property!=null&&!property.trim().isEmpty()){
      map.put(manifestKey, property);
    }
  }
}

interface Pf4bootPluginExtension {
  Property<String> getId();

  Property<String> getPluginClass();

  Property<String> getDescription();

  Property<String> getProvider();

  Property<String> getDependencies();

  Property<String> getRequires();

  Property<String> getLicense();
}