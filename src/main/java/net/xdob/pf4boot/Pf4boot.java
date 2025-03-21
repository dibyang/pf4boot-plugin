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
  public static final String PLUGIN = "plugin";


  private final ObjectFactory objectFactory;
  private final SoftwareComponentFactory softwareComponentFactory;

  @Inject
  public Pf4boot(ObjectFactory objectFactory, SoftwareComponentFactory softwareComponentFactory) {
    this.objectFactory = objectFactory;
    this.softwareComponentFactory = softwareComponentFactory;
  }

  public void apply(ProjectInternal project) {

    project.getPluginManager().apply(JavaPlugin.class);


    Configuration plugin = project.getConfigurations().register(PLUGIN, p -> {
      p.setCanBeConsumed(false);
      p.setCanBeResolved(true);
      p.setTransitive(false);
      p.setVisible(false);
    }).get();


  }



}

