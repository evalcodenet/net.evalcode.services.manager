package net.evalcode.services.manager.service.jmx;


import net.evalcode.services.manager.component.annotation.Service;


/**
 * ServiceMBean
 *
 * @author carsten.schipke@gmail.com
 */
@Service
public interface ServiceMBean
{
  // ACCESSORS/MUTATORS
  String getName();
}
