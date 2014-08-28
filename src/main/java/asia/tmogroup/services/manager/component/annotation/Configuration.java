package net.evalcode.services.manager.component.annotation;


import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Configuration
 *
 * <p> Declares decorated type as a configuration entity.
 *
 * <p> Configuration entities are simple (POJO) JAX-B entities.
 * The property {@link Configuration#value()} specifies the actual file mapped to decorated entity.
 *
 * <p> Configuration entities can directly be injected by their type, the component manager
 * will take care for unmarshalling the file, specified by the property
 * {@link Configuration#value()}, to an instance of decorated type.
 *
 * <p> See following example on how to declare and use a configuration entity:
 *
 * <ul>
 *   <li> Create entity and decorate with {@link Configuration &#064;Configuration([FILE])},
 *     whereby [FILE] is the corresponding configuration file - either JSON or XML.</li>
 *   <li> Add JAX-B annotations according to your configuration file.</li>
 * </ul>
 *
 * <pre>
 *   package net.evalcode.services.foo.configuration;
 *
 *   {@link javax.xml.bind.annotation.XmlRootElement &#064;XmlRootElement}
 *   {@link javax.xml.bind.annotation.XmlAccessorType &#064;XmlAccessorType}
 *     ({@link javax.xml.bind.annotation.XmlAccessType#FIELD XmlAccessType.FIELD})
 *   {@link Configuration &#064;Configuration("my-entity.json")}
 *   class MyEntity
 *   {
 *     private String host;
 *     private int port;
 *
 *     [..]
 *   }
 * </pre>
 *
 * <ul>
 *   <li> Make sure the package containing your configuration entities is exported by
 *     your bundle's manifest.</li>
 * </ul>
 *
 * <pre>
 *   META-INF/MANIFEST.MF
 *
 *   Export-Package: net.evalcode.services.foo.configuration
 * </pre>
 *
 * <ul>
 *   <li> Place configuration file in the configuration path according to your environment
 *   and bundle name.</li>
 * </ul>
 *
 * <pre>
 *   ${net.evalcode.services.config}/net.evalcode.services.foo/my-entity.json
 *
 *   {
 *     host: "domain.tld",
 *     port: "1337"
 *   }
 * </pre>
 *
 * <ul>
 *   <li> Inject entity on demand.</li>
 * </ul>
 *
 * <pre>
 *   package net.evalcode.services.foo;
 *
 *   {@link Component &#064;Component}
 *   class MyComponent
 *   {
 *     {@link javax.inject.Inject &#064;Inject}
 *     MyEntity myEntity;
 *
 *     [..]
 *   }
 * </pre>
 *
 * @see <a href="http://jaxb.java.net/">http://jaxb.java.net/</a>
 * @see <a href="http://sylvester:8001/projects/anduin/wiki/Anduin-development/">
 *        http://sylvester:8001/projects/anduin/wiki/Anduin-development/
 *      </a>
 *
 * @author carsten.schipke@gmail.com
 */
@Documented
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Configuration
{
  // PROPERTIES
  /**
   * <p> Specifies the mapping file for decorated entity by its plain filename.
   * Supported file formats are JSON and XML.
   *
   * <p> The file needs to be located in either the bundle's global or
   * local (environment specific) configuration path - according to your settings of
   * the system properties included here:
   *
   * <ul>
   *   <li> ${net.evalcode.services.config}/net.evalcode.services.bundle/[FILE]</li>
   *   <li> ${net.evalcode.services.config}/${net.evalcode.services.environment}/
   *         [net.evalcode.services.bundle]/[FILE]</li>
   * </ul>
   *
   * <p> Whereby [FILE] will be replaced by this property's value on lookup.
   *
   * <p> [net.evalcode.services.bundle] represents the name of the bundle providing
   * the configuration entity. For further information on bundle & environment configuration,
   * refer to related documentation.
   */
  String value();

  /**
   * <p> Optionally specifies class of a type to instantiate and return as
   * default configuration entity if specified configuration file is missing
   * or invalid.
   */
  Class<?> fallback() default Void.class;
}
