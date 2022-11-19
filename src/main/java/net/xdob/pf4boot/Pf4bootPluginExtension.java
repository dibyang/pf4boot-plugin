package net.xdob.pf4boot;

import org.gradle.api.provider.Property;

interface Pf4bootPluginExtension {
  Property<String> getId();

  Property<String> getPluginClass();

  Property<String> getDescription();

  Property<String> getProvider();

  Property<String> getDependencies();

  Property<String> getRequires();

  Property<String> getLicense();
}
