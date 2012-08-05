package net.evalcode.services.manager.configuration;


import com.google.inject.Provider;


/**
 * ConfigurationEntityProvider
 *
 * @author carsten.schipke@gmail.com
 */
public class ConfigurationEntityProvider<T> implements Provider<T>
{
  // MEMBERS
  private final ConfigurationEntityManager<T> configurationEntityManager;


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
