package net.evalcode.services.manager.internal.persistence;


import java.util.Properties;
import javax.persistence.spi.PersistenceUnitTransactionType;
import net.evalcode.services.manager.internal.persistence.PersistenceXml.PersistenceUnit;
import net.evalcode.services.manager.internal.util.SystemProperty;
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
  final PersistenceXml xml;
  volatile Properties properties=null;


  // CONSTRUCTION
  public PersistenceXmlMetadata(final PersistenceXml persistenceXml)
  {
    super();

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
    if(null==properties)
    {
      final Properties persistenceProperties=new Properties();

      for(final PersistenceUnit.Properties.Property p : xml.persistenceUnit.properties.values)
        persistenceProperties.put(p.name, substituteSystemProperties(p.value));

      synchronized(this)
      {
        if(null==properties)
          properties=persistenceProperties;

        return properties;
      }
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
