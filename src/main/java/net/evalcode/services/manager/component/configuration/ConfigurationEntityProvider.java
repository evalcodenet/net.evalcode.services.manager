package net.evalcode.services.manager.component.configuration;


import com.google.inject.Provider;


/**
 * ConfigurationEntityProvider
 *
 * @author carsten.schipke@gmail.com
 */
public class ConfigurationEntityProvider<T> implements Provider<T>
{
  // MEMBERS
  final ConfigurationEntityManager<T> configurationEntityManager;


  // CONSTRUCTION
  public ConfigurationEntityProvider(final ConfigurationEntityManager<T> configurationEntityManager)
  {
    this.configurationEntityManager=configurationEntityManager;
  }


  // OVERRIDES/IMPLEMENTS
  @Override
  public T get()
  {
    return configurationEntityManager.get();
  }
}
