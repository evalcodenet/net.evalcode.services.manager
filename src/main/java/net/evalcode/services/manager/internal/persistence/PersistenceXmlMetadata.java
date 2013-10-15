package net.evalcode.services.manager.internal.persistence;


import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;
import javax.persistence.spi.PersistenceUnitTransactionType;
import net.evalcode.services.manager.internal.util.SystemProperty;
import net.evalcode.services.manager.persistence.PersistenceXml;
import net.evalcode.services.manager.persistence.PersistenceXml.PersistenceUnit;
import org.apache.commons.lang.StringUtils;
import org.hibernate.ejb.packaging.PersistenceMetadata;


/**
 * PersistenceXmlMetadata
 *
 * @author carsten.schipke@gmail.com
 */
public class PersistenceXmlMetadata extends PersistenceMetadata
{
  // MEMBERS
  private final AtomicReference<Properties> refProperties=new AtomicReference<>(null);
  private final PersistenceXml xml;


  // CONSTRUCTION
  public PersistenceXmlMetadata(final PersistenceXml persistenceXml)
  {
    this.xml=persistenceXml;
  }


  // OVERRIDES/IMPLEMENTS
  @Override
  public String getName()
  {
    return xml.persistenceUnit.name;
  }

  @Override
  public PersistenceUnitTransactionType getTransactionType()
  {
    if(PersistenceUnitTransactionType.JTA.name().equals(xml.persistenceUnit.transactionType))
      return PersistenceUnitTransactionType.JTA;

    return PersistenceUnitTransactionType.RESOURCE_LOCAL;
  }

  @Override
  public Properties getProps()
  {
    final Properties properties=refProperties.get();

    if(null==properties)
    {
      final Properties persistenceProperties=new Properties();

      for(final PersistenceUnit.Properties.Property p : xml.persistenceUnit.properties.values)
        persistenceProperties.put(p.name, substituteSystemProperties(p.value));

      if(refProperties.compareAndSet(properties, persistenceProperties))
        return persistenceProperties;

      return refProperties.get();
    }

    return properties;
  }

  @Override
  public boolean getExcludeUnlistedClasses()
  {
    return true;
  }


  // IMPLEMENTATION
  Object substituteSystemProperties(final String value)
  {
    for(final SystemProperty systemProperty : SystemProperty.values())
    {
      if(StringUtils.contains(value, systemProperty.key()))
      {
        return substituteSystemProperties(StringUtils.replace(
          value, "${"+systemProperty.key()+"}", SystemProperty.get(systemProperty.key())
        ));
      }
    }

    return value;
  }
}
