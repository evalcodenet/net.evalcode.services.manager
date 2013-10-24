package net.evalcode.services.manager.internal.util;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * SystemProperty
 *
 * TODO Remove SystemProperty.class
 *
 * Load all properties in ComponentBundleConfiguration
 * and create one for the manager bundle.
 *
 * @author carsten.schipke@gmail.com
 */
public enum SystemProperty
{
  // PREDEFINED SYSTEM PROPERTIES
  NET_EVALCODE_SERVICES_ENVIRONMENT(
    "net.evalcode.services.environment", "production"
  ),
  NET_EVALCODE_SERVICES_LOCALE(
    "net.evalcode.services.locale", "en_US"
  ),
  NET_EVALCODE_SERVICES_ENCODING(
    "net.evalcode.services.encoding", "UTF-8"
  ),
  NET_EVALCODE_SERVICES_TIMEZONE(
    "net.evalcode.services.timezone", "Asia/Shanghai"
  ),
  NET_EVALCODE_SERVICES_HOME(
    "net.evalcode.services.home", "./"
  ),
  NET_EVALCODE_SERVICES_CONFIG(
    "net.evalcode.services.config", "./config"
  ),
  NET_EVALCODE_SERVICES_RESOURCES(
    "net.evalcode.services.resources", "./resources"
  ),
  // SMTP
  NET_EVALCODE_SERVICES_SMTP_HOST(
    "net.evalcode.services.smtp.host", "127.0.0.1"
  ),
  NET_EVALCODE_SERVICES_SMTP_USER(
    "net.evalcode.services.smtp.user", null
  ),
  NET_EVALCODE_SERVICES_SMTP_PASSWORD(
    "net.evalcode.services.smtp.password", null
  ),
  NET_EVALCODE_SERVICES_SMTP_SENDER(
    "net.evalcode.services.smtp.sender", "services@evalcode.net"
  ),
  // JMX
  NET_EVALCODE_SERVICES_JMX_NODE(
    "net.evalcode.services.jmx.node", "net.evalcode.services"
  );


  // PREDEFINED PROPERTIES
  static final String PATH_DEFAULT="default";
  static final String FILE_PROPERTIES="net.evalcode.services.properties";
  static final Logger LOG=LoggerFactory.getLogger(SystemProperty.class);
  static final CountDownLatch INITIALIZING=new CountDownLatch(2);
  static final ConcurrentMap<String, String> PROPERTIES=new ConcurrentHashMap<>();


  // MEMBERS
  final String key;
  final String defaultValue;


  // CONSTRUCTION
  SystemProperty(final String key, final String defaultValue)
  {
    this.key=key;
    this.defaultValue=defaultValue;
  }


  // STATIC ACCESSORS
  public static Path getConfigurationFilePath(final String fileName)
  {
    final Path rawPath=Paths.get(fileName);
    final File rawFile=rawPath.toFile();

    if(rawFile.exists())
      return rawPath;

    final Path path=getLocalConfigurationPath(fileName);
    final File file=path.toFile();

    if(file.exists())
      return path;

    return getGlobalConfigurationPath(fileName);
  }

  public static Path getGlobalConfigurationPath()
  {
    return Paths.get(NET_EVALCODE_SERVICES_CONFIG.get(), PATH_DEFAULT);
  }

  public static Path getGlobalConfigurationPath(final String subPath)
  {
    return Paths.get(NET_EVALCODE_SERVICES_CONFIG.get(), PATH_DEFAULT, subPath);
  }

  public static Path getLocalConfigurationPath()
  {
    return Paths.get(
      NET_EVALCODE_SERVICES_CONFIG.get(),
      NET_EVALCODE_SERVICES_ENVIRONMENT.get()
    );
  }

  public static Path getLocalConfigurationPath(final String subPath)
  {
    return Paths.get(
      NET_EVALCODE_SERVICES_CONFIG.get(),
      NET_EVALCODE_SERVICES_ENVIRONMENT.get(),
      subPath
    );
  }

  public static Path getResourcesPath()
  {
    return Paths.get(NET_EVALCODE_SERVICES_RESOURCES.get());
  }

  public static Path getResourcesPath(final String... subPath)
  {
    return Paths.get(NET_EVALCODE_SERVICES_RESOURCES.get(), subPath);
  }

  public static Charset getCharset()
  {
    if(null==NET_EVALCODE_SERVICES_ENCODING.get())
      return Charset.defaultCharset();

    return Charset.forName(NET_EVALCODE_SERVICES_ENCODING.get());
  }

  public static Locale getLocale()
  {
    if(null==NET_EVALCODE_SERVICES_LOCALE.get())
      return Locale.getDefault();

    return new Locale(NET_EVALCODE_SERVICES_LOCALE.get());
  }

  public static TimeZone getTimeZone()
  {
    if(null==NET_EVALCODE_SERVICES_TIMEZONE.get())
      return TimeZone.getDefault();

    return TimeZone.getTimeZone(NET_EVALCODE_SERVICES_TIMEZONE.get());
  }

  public static String get(final String key)
  {
    if(0<INITIALIZING.getCount())
    {
      synchronized(SystemProperty.class)
      {
        if(1<INITIALIZING.getCount())
        {
          reload();
        }
        else if(1==INITIALIZING.getCount())
        {
          try
          {
            INITIALIZING.await();
          }
          catch(final InterruptedException e)
          {
            Thread.currentThread().interrupt();

            return null;
          }
        }
      }
    }

    return PROPERTIES.get(key);
  }

  public static String get(final String key, final String defaultValue)
  {
    final String value=PROPERTIES.get(key);

    if(null==value)
      return defaultValue;

    return value;
  }

  public static String set(final String key, final String value)
  {
    return PROPERTIES.put(key, value);
  }

  public static Set<String> keySet()
  {
    if(0<INITIALIZING.getCount())
    {
      synchronized(SystemProperty.class)
      {
        if(1<INITIALIZING.getCount())
        {
          reload();
        }
        else if(1==INITIALIZING.getCount())
        {
          try
          {
            INITIALIZING.await();
          }
          catch(final InterruptedException e)
          {
            Thread.currentThread().interrupt();

            return null;
          }
        }
      }
    }

    return PROPERTIES.keySet();
  }

  public static void reload()
  {
    INITIALIZING.countDown();


    // load default values
    final Set<String> keys=new HashSet<String>();

    for(final SystemProperty systemProperty : values())
    {
      keys.add(systemProperty.key);

      if(null!=systemProperty.defaultValue)
      {
        LOG.debug("Initializing default property [name: {}, default-value: {}]",
          systemProperty.key, systemProperty.defaultValue
        );

        PROPERTIES.put(systemProperty.key, systemProperty.defaultValue);
      }
    }


    // load global properties
    final File globalPropertiesFile=new File(
      System.getProperty(NET_EVALCODE_SERVICES_CONFIG.key(),
        NET_EVALCODE_SERVICES_CONFIG.defaultValue())+
      File.separator+
      PATH_DEFAULT+
      File.separator+
      FILE_PROPERTIES
    );

    final Properties globalProperties=new Properties();

    if(globalPropertiesFile.exists())
    {
      try(final FileInputStream globalPropertiesFileInputStream=
        new FileInputStream(globalPropertiesFile))
      {
        LOG.debug("Loading global properties [file: {}].", globalPropertiesFile.getAbsolutePath());

        globalProperties.load(globalPropertiesFileInputStream);
      }
      catch(final IOException e)
      {
        LOG.debug(e.getMessage(), e);
      }
    }

    for(final Object key : globalProperties.keySet())
    {
      LOG.debug("Initializing global property [name: {}, value: {}]",
        key, globalProperties.get(key)
      );

      PROPERTIES.put((String)key, (String)globalProperties.get(key));
    }


    // load local properties
    final File localPropertiesFile=new File(
      System.getProperty(NET_EVALCODE_SERVICES_CONFIG.key())+
      File.separator+
      System.getProperty(NET_EVALCODE_SERVICES_ENVIRONMENT.key(),
        NET_EVALCODE_SERVICES_ENVIRONMENT.defaultValue())+
      File.separator+
      FILE_PROPERTIES
    );

    final Properties localProperties=new Properties();

    if(localPropertiesFile.exists())
    {
      try(final FileInputStream localPropertiesFileInputStream=
        new FileInputStream(localPropertiesFile))
      {
        LOG.debug("Loading local properties [file: {}].", localPropertiesFile.getAbsolutePath());

        localProperties.load(localPropertiesFileInputStream);
      }
      catch(final IOException e)
      {
        LOG.debug(e.getMessage(), e);
      }
    }

    for(final Object key : localProperties.keySet())
    {
      LOG.debug("Initializing local property [name: {}, value: {}]",
        key, globalProperties.get(key)
      );

      PROPERTIES.put((String)key, (String)localProperties.get(key));
    }


    // load system properties defined on startup/cli
    for(final String key : System.getProperties().stringPropertyNames())
    {
      final String value=System.getProperty(key);

      LOG.debug("Initializing system property [name: {}, value: {}]",
        key, value
      );

      PROPERTIES.put(key, value);
    }


    // resolve recursive references
    for(final String key : PROPERTIES.keySet())
      PROPERTIES.put(key, substituteProperties(PROPERTIES.get(key)));


    // (re-)populate resolved system properties for 3rd party libraries etc.
    for(final String key : PROPERTIES.keySet())
    {
      LOG.debug("Populating system property [name: {}, value: {}]", key, PROPERTIES.get(key));
      System.setProperty(key, PROPERTIES.get(key));
    }

    INITIALIZING.countDown();
  }


  // ACCESSORS/MUTATORS
  public String key()
  {
    return key;
  }

  public String get()
  {
    return get(key);
  }

  public String defaultValue()
  {
    return defaultValue;
  }


  // OVERRIDES/IMPLEMENTS
  @Override
  public String toString()
  {
    return String.format("key: %1$s, value: %2$s, defaultValue: %3$s",
      key(), get(key()), defaultValue()
    );
  }


  // HELPERS
  static String substituteProperties(final String value)
  {
    final Matcher matcher=Pattern.compile("\\$\\{(?<property>[\\w.]+)\\}", Pattern.CASE_INSENSITIVE).matcher(value);

    if(matcher.find())
    {
      final String property=matcher.group("property");

      if(PROPERTIES.containsKey(property))
        return substituteProperties(StringUtils.replace(value, "${"+property+"}", PROPERTIES.get(property)));
    }

    return value;
  }
}
