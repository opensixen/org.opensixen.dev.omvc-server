package org.opensixen.dev.omvc.server;

import java.util.ArrayList;
import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;


/**
 * Server Activator
 * 
 * @author Eloy Gomez
 *
 */
public class Activator implements BundleActivator {

	private ArrayList<IRienaServer> services = new ArrayList<IRienaServer>();
	
	public Activator()	{
		services.add(new OpensixenIDGenerator());
		services.add(new RemoteConsole());
		services.add(new RevisionUploader());
	}
	
	@Override
	public void start(BundleContext context) throws Exception {	
		for (IRienaServer service: services)	{
			service.registerService(context);
		}
	}

	@Override
	public void stop(BundleContext context) throws Exception {	
		for (IRienaServer service: services)	{
			service.unregisterService(context);
		}
	}

}
