package net.evalcode.services.manager.internal.persistence;


import org.hibernate.cfg.ImprovedNamingStrategy;
import org.jvnet.inflector.Noun;


/**
 * PersistenceNamingStrategy
 *
 * @author carsten.schipke@gmail.com
 */
public class PersistenceNamingStrategy extends ImprovedNamingStrategy
{
  // PREDEFINED PROPERTIES
  static final long serialVersionUID=1L;


  // OVERRIDES/IMPLEMENTS
  @Override
  public String classToTableName(final String className)
  {
    return Noun.pluralOf(className);
  }
}
