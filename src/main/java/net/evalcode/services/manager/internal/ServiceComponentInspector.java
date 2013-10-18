package net.evalcode.services.manager.internal;


import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.evalcode.services.manager.component.ServiceComponentModule;
import net.evalcode.services.manager.component.annotation.Bind;
import net.evalcode.services.manager.component.annotation.Component;
import net.evalcode.services.manager.component.annotation.Service;
import net.evalcode.services.manager.component.annotation.Unbind;
import com.google.common.collect.Sets;


/**
 * ServiceComponentInspector
 *
 * @author carsten.schipke@gmail.com
 */
class ServiceComponentInspector
{
  // MEMBERS
  final Class<?> clazz;


  // CONSTRUCTION
  ServiceComponentInspector(final Class<?> componentClazz)
  {
    this.clazz=componentClazz;
  }


  // ACCESSORS/MUTATORS
  Class<?> getComponentClass()
  {
    return clazz;
  }

  Class<? extends ServiceComponentModule> getComponentModuleClass()
  {
    return clazz.getAnnotation(Component.class).module();
  }

  Set<Class<?>> getProvidedServices()
  {
    final Class<?>[] declaredProvidedServices=
      clazz.getAnnotation(Component.class).providedServices();

    if(0<declaredProvidedServices.length)
      return Sets.newHashSet(declaredProvidedServices);

    final Set<Class<?>> providedServices=new HashSet<Class<?>>();

    for(final Class<?> service : clazz.getInterfaces())
    {
      if(service.isAnnotationPresent(Service.class))
        providedServices.add(service);
    }

    return providedServices;
  }

  Map<Class<?>, Method> getServiceConnectors()
  {
    final Map<Class<?>, Method> serviceConnectors=new HashMap<Class<?>, Method>();

    for(final Method method : clazz.getMethods())
    {
      if(method.isAnnotationPresent(Bind.class) && 1==method.getParameterTypes().length)
        serviceConnectors.put(method.getParameterTypes()[0], method);
    }

    return serviceConnectors;
  }

  Map<Class<?>, Method> getServiceDisconnectors()
  {
    final Map<Class<?>, Method> serviceDisconnectors=new HashMap<Class<?>, Method>();

    for(final Method method : clazz.getMethods())
    {
      if(method.isAnnotationPresent(Unbind.class) && 1==method.getParameterTypes().length)
        serviceDisconnectors.put(method.getParameterTypes()[0], method);
    }

    return serviceDisconnectors;
  }

  Method getAnnotatedMethod(final Class<? extends Annotation> annotation)
  {
    for(final Method method : clazz.getMethods())
    {
      if(method.isAnnotationPresent(annotation))
        return method;
    }

    return null;
  }
}
