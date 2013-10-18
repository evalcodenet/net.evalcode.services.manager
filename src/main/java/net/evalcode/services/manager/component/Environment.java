package net.evalcode.services.manager.component;


import net.evalcode.services.manager.internal.util.SystemProperty;
import com.google.inject.Stage;


/**
 * Environment
 *
 * @author carsten.schipke@gmail.com
 */
public enum Environment
{
  // PREDEFINED STAGES
  PRODUCTION("production", Stage.PRODUCTION),
  STAGING("staging", Stage.PRODUCTION),
  TESTING("testing", Stage.PRODUCTION),
  DEVELOPMENT("development", Stage.DEVELOPMENT);


  // CONSTRUCTION
  Environment(final String key, final Stage stage)
  {
    this.key=key;
    this.stage=stage;
  }


  // MEMBERS
  final String key;
  final Stage stage;


  // STATIC ACCESSORS
  public static Environment current()
  {
    final String currentEnvironmentName=SystemProperty.NET_EVALCODE_SERVICES_ENVIRONMENT.get();

    for(final Environment environment : values())
    {
      if(environment.key.equals(currentEnvironmentName))
        return environment;
    }

    throw new IllegalArgumentException(String.format(
      "Illegal/missing property value [property: %s].",
        SystemProperty.NET_EVALCODE_SERVICES_ENVIRONMENT.name()
    ));
  }


  // ACCESSORS/MUTATORS
  public boolean isProduction()
  {
    return PRODUCTION.key().equals(key());
  }

  public boolean isStaging()
  {
    return STAGING.key().equals(key());
  }

  public boolean isTesting()
  {
    return TESTING.key().equals(key());
  }

  public boolean isDevelopment()
  {
    return DEVELOPMENT.key().equals(key());
  }

  /**
   * @return String identifier of environment.
   */
  public String key()
  {
    return key;
  }

  /**
   * @return {@link Stage} of integrated IoC framework.
   */
  public Stage stage()
  {
    return stage;
  }


  // OVERRIDES/IMPLEMENTS
  @Override
  public String toString()
  {
    return key();
  }
}
