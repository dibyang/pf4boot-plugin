package net.xdob.pf4boot;


import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.*;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.component.SoftwareComponentFactory;
import org.gradle.api.internal.project.ProjectInternal;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.plugins.JavaPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Collections;
import java.util.Set;

/**
 * pf4boot.
 */
public class Pf4boot implements Plugin<ProjectInternal> {

  static final Logger LOG = LoggerFactory.getLogger(Pf4boot.class);
  public static final String PLUGIN_CONFIG_NAME = "plugin";
  public static final String PLUGIN_CLASSPATH_CONFIG_NAME = "pluginClasspath";
  public static final String PLATFORM_API_CONFIG_NAME = "platformApi";
  public static final String PLATFORM_CLASSPATH_CONFIG_NAME = "platformClasspath";

  private final ObjectFactory objectFactory;
  private final SoftwareComponentFactory softwareComponentFactory;

  @Inject
  public Pf4boot(ObjectFactory objectFactory, SoftwareComponentFactory softwareComponentFactory) {
    this.objectFactory = objectFactory;
    this.softwareComponentFactory = softwareComponentFactory;
  }

  public void apply(ProjectInternal project) {

    project.getPluginManager().apply(JavaPlugin.class);

    //Configuration runtimeClasspath = project.getConfigurations().getByName(JavaPlugin.RUNTIME_CLASSPATH_CONFIGURATION_NAME);
    Configuration compileClasspath = project.getConfigurations().getByName(JavaPlugin.COMPILE_CLASSPATH_CONFIGURATION_NAME);

    Configuration platformApi = project.getConfigurations().register(PLATFORM_API_CONFIG_NAME, p -> {
      p.setCanBeConsumed(false);
      p.setCanBeResolved(false);
      p.setTransitive(true);
      p.setVisible(false);
    }).get();

    compileClasspath.extendsFrom(platformApi);

    Configuration platformClasspath = project.getConfigurations().register(PLATFORM_CLASSPATH_CONFIG_NAME, p -> {
      p.setCanBeConsumed(true);
      p.setCanBeResolved(true);
      p.setTransitive(true);
      p.setVisible(false);
    }).get();
    platformClasspath.extendsFrom(platformApi);

    Configuration plugin = project.getConfigurations().register(PLUGIN_CONFIG_NAME, p -> {
      p.setCanBeConsumed(true);
      p.setCanBeResolved(false);
      p.setTransitive(true);
      p.setVisible(false);
    }).get();

    //Configuration compileOnlyApi = project.getConfigurations().getByName(JavaPlugin.COMPILE_ONLY_API_CONFIGURATION_NAME);
    compileClasspath.extendsFrom(plugin);

    Configuration pluginClasspath = project.getConfigurations().register(PLUGIN_CLASSPATH_CONFIG_NAME, p -> {
      p.setCanBeConsumed(false);
      p.setCanBeResolved(true);
      p.setTransitive(false);
      p.setVisible(false);
    }).get();
    pluginClasspath.extendsFrom(plugin);

  }



}

