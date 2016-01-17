package net.evalcode.services.manager.internal.persistence;


import java.util.Set;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


/**
 * PersistenceXml
 *
 * @author carsten.schipke@gmail.com
 */
@XmlRootElement(name="persistence")
@XmlAccessorType(XmlAccessType.NONE)
public class PersistenceXml
{
  // FIELDS
  @XmlElement(name="persistence-unit")
  public PersistenceUnit persistenceUnit;


  /**
   * PersistenceUnit
   *
   * @author carsten.schipke@gmail.com
   */
  @XmlRootElement
  @XmlAccessorType(XmlAccessType.NONE)
  public static class PersistenceUnit
  {
    // FIELDS
    @XmlAttribute(name="name")
    public String name;
    @XmlAttribute(name="transaction-type")
    public String transactionType;

    @XmlElement(name="properties")
    public Properties properties;


    /**
     * Properties
     *
     * @author carsten.schipke@gmail.com
     */
    @XmlRootElement
    @XmlAccessorType(XmlAccessType.NONE)
    public static class Properties
    {
      // FIELDS
      @XmlElement(name="property")
      public Set<Property> values;


      /**
       * Property
       *
       * @author carsten.schipke@gmail.com
       */
      @XmlRootElement
      @XmlAccessorType(XmlAccessType.NONE)
      public static class Property
      {
        // FIELDS
        @XmlAttribute(name="name")
        public String name;
        @XmlAttribute(name="value")
        public String value;
      }
    }
  }
}
