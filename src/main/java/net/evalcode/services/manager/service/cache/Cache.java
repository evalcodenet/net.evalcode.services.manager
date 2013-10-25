package net.evalcode.services.manager.service.cache;


import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import net.evalcode.services.manager.service.cache.impl.NoCacheKeyGenerator;
import net.evalcode.services.manager.service.cache.spi.CacheKeyGenerator;
import net.evalcode.services.manager.service.cache.spi.collection.MapValuePopulator;


/**
 * Cache
 *
 * <p> Cache invocation of decorated methods.
 *
 * @author carsten.schipke@gmail.com
 */
@Documented
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Cache
{
  // PROPERTIES
  Region region() default @Region;
  Key key() default @Key;
  Lifetime lifetime() default @Lifetime;


  /**
   * Map
   *
   * <p> Caches possible map values for methods returning
   * subsets for given input keys.
   *
   * @author carsten.schipke@gmail.com
   */
  @Documented
  @Target({ElementType.METHOD})
  @Retention(RetentionPolicy.RUNTIME)
  public @interface Map
  {
    // PROPERTIES
    Region region() default @Region;
    Key key() default @Key;
    Lifetime lifetime() default @Lifetime;
    Class<? extends MapValuePopulator<?, ?>> populator();


    /**
     * Keys
     *
     * <p> Defines method parameter as set of keys for cached collections.
     *
     * @author evalcode.net
     */
    @Documented
    @Target({ElementType.PARAMETER})
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Keys
    {

    }
  }


  /**
   * Flush
   *
   * @author evalcode.net
   */
  @Documented
  @Target({ElementType.METHOD})
  @Retention(RetentionPolicy.RUNTIME)
  public @interface Flush
  {
    // PROPERTIES
    Region region() default @Region;
    Key key() default @Key(type=Key.Type.ALL);
  }


  /**
   * Key
   *
   * @author evalcode.net
   */
  @Documented
  @Target({ElementType.METHOD})
  @Retention(RetentionPolicy.RUNTIME)
  public @interface Key
  {
    // PROPERTIES
    Type type() default Type.HASHCODE;
    String value() default "";
    Class<? extends CacheKeyGenerator> generator() default NoCacheKeyGenerator.class;


    /**
     * Type
     *
     * @author evalcode.net
     */
    public enum Type
    {
      // PREDEFINED TYPES
      ALL,
      GENERATOR,
      HASHCODE,
      SIGNATURE,
      VALUE;
    }
  }


  /**
   * KeySegment
   *
   * @author evalcode.net
   */
  @Documented
  @Target({ElementType.PARAMETER})
  @Retention(RetentionPolicy.RUNTIME)
  public @interface KeySegment
  {
    // PROPERTIES
    Key.Type type() default Key.Type.HASHCODE;
    String value() default "";
  }


  /**
   * Lifetime
   *
   * @author evalcode.net
   */
  @Documented
  @Target({ElementType.METHOD})
  @Retention(RetentionPolicy.RUNTIME)
  public @interface Lifetime
  {
    // PROPERTIES
    int seconds() default 0;
    int hits() default 0;
  }


  /**
   * Region
   *
   * @author evalcode.net
   */
  @Documented
  @Target({ElementType.METHOD})
  @Retention(RetentionPolicy.RUNTIME)
  public @interface Region
  {
    // PROPERTIES
    String value() default "";
    String defaultConfig() default "";
  }
}
