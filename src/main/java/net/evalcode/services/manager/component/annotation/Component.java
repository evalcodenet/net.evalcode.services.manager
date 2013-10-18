package net.evalcode.services.manager.component.annotation;


import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import net.evalcode.services.manager.component.ServiceComponentModule;


/**
 * Component
 *
 * <p> Declares annotated class as a component.
 *
 * <p> A class annotated with {@link Component &#064;Component} represents the entry point
 * for its bundle's implementations lifecycle.
 *
 * <p> Though it is possible to have multiple components in one bundle, the
 * prefered way is to not do this and enforce the concept of a loose coupled
 * service infrastructure by deploying single service-providing components
 * as independent OSGi bundles.
 *
 * <p> Runtime dependencies are managed by the component's bundle manifest
 * and equinox. Runtime features like de-/activation, service un-/bindings,
 * dependency & resource injection, configuration, management & monitoring etc.
 * are provided by the component manager bundle net.evalcode.services.manager
 *
 * <p> Simple component declaration:
 *
 * <pre>
 *   package net.evalcode.services;
 *
 *   {@link Component &#064;Component}
 *   class MyComponent
 *   {
 *     [..]
 *   }
 * </pre>
 *
 * <p> The component needs to be accessible for the component manager, which means
 * its enclosing package needs to be exported by its OSGi bundle's manifest:
 *
 * <pre>
 *   [META-INF/MANIFEST.MF]
 *   Export-Package: net.evalcode.services
 * </pre>
 *
 * <p> Following method annotations can extend component behavior.
 * Follow their links for further details:
 *
 * <ul>
 *   <li> Lifecycle Listener
 *     <ul>
 *       <li>
 *         {@link Activate &#064;Activate}
 *         - Invoked when component's bundle is started.
 *       </li>
 *       <li>
 *         {@link Deactivate &#064;Deactivate}
 *         - Invoked when component's bundle is requested to be stopped.
 *       </li>
 *     </ul>
 *   </li>
 *   <li> Service Listener
 *     <ul>
 *       <li>
 *         {@link Bind &#064;Bind}
 *         - Invoked when requested service becomes available.
 *       </li>
 *       <li>
 *         {@link Unbind &#064;Unbind}
 *         - Invoked when requested service becomes unavailable.
 *       </li>
 *     </ul>
 *   </li>
 * </ul>
 *
 * <p> A service component can implement 0:n service interfaces and thereby
 * represent a service (providing) component which can be dis-/connected to/from
 * other (service) components which are implementing corresponding service listeners.
 *
 * <p> Service interfaces are either directly implemented interfaces that are
 * decorated with the {@link Service &#064;Service} annotation, or interfaces specified
 * by {@link Component#providedServices()}.
 *
 * @author carsten.schipke@gmail.com
 */
@Documented
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Component
{
  // PROPERTIES
  /**
   * <p> Specifies class of a dependency & resource injection configuration module.
   *
   * <p> Every component becomes an dedicated dependency & resource injector assigned
   * on initialization by the component manager.
   *
   * <p> Currently integrated dependency injection provider is Google Guice - though
   * we try to avoid proprietary annotations and use the in JSR-330 specified javax.inject
   * annotations exclusively. Target is to have JEE6 compliant components, at least at
   * a later / more mature state of development, to eventually support deployment to
   * JEE6 containers.
   *
   * <p> For details regarding the default dependency injection configuration
   * see {@link ServiceComponentModule}.
   *
   * <p> The default module can be extended and replaced by specifying the custom
   * class in this parameter.
   *
   * <pre>
   *   package net.evalcode.services;
   *
   *   {@link Component &#064;Component}({@link Component#module() module}=
   *     net.evalcode.services.MyComponentModule.class
   *   )
   *   class MyComponent
   *   {
   *     [..]
   *   }
   * </pre>
   */
  Class<? extends ServiceComponentModule> module() default ServiceComponentModule.class;

  /**
   * <p> Specifies explicitely which services are provided by this component.
   *
   * <p> This can be useful in case you want to provide services based on
   * interfaces that can not be decorated with the {@link Service &#064;Service}
   * annotation, e.g. interfaces of 3rd party libraries - or if you want to
   * exclude / disable services for implemented interfaces.
   *
   * <p> Please note that if you use this property, the component manager will not
   * search for {@link Service &#064;Service}-annotated implemented interfaces
   * anymore and simply work based on your specified array of service interfaces.
   *
   * <pre>
   *   package net.evalcode.services;
   *
   *   {@link Component &#064;Component}({@link Component#providedServices() providedServices}={
   *     net.evalcode.services.FooService.class,
   *     org.eclipse.osgi.framework.console.CommandProvider.class
   *   })
   *   class MyComponent implements FooService, CommandProvider
   *   {
   *     [..]
   *   }
   * </pre>
   */
  Class<?>[] providedServices() default {};

  /**
   * <p> Specifies required / available system properties for internal configuration
   * management as well as API documentation.
   *
   * <pre>
   *   package net.evalcode.services;
   *
   *   {@link Component &#064;Component}({@link Component#properties() properties}={
   *     {@link Property &#064;Property}(name="net.evalcode.services.foo", defaultValue="bar"),
   *     {@link Property &#064;Property}(name="net.evalcode.services.bar", defaultValue="foo"),
   *   })
   *   class MyComponent
   *   {
   *     {@link javax.inject.Inject &#064;Inject}
   *     {@link javax.inject.Named &#064;Named}("net.evalcode.services.foo")
   *     String foo;
   *
   *     {@link javax.inject.Inject &#064;Inject}
   *     {@link javax.inject.Named &#064;Named}("net.evalcode.services.bar")
   *     String bar;
   *
   *     [..]
   *   }
   * </pre>
   *
   * <p> See {@link Property &#064;Property} for further details.
   */
  Property[] properties() default {};
}
