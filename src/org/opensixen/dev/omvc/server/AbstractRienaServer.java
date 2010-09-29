package org.opensixen.dev.omvc.server;

import java.util.Hashtable;

import org.apache.log4j.Logger;

import org.opensixen.riena.interfaces.IRienaService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

public abstract class AbstractRienaServer implements IRienaServer, IRienaService {

	protected Logger log = Logger.getLogger(getClass());
	
	private static ServiceRegistration serviceRegistration; 
		
	private String serviceName;
	
	private String servicePath;
	
	
	public AbstractRienaServer(String serviceName, String servicePath) {
		super();
		this.serviceName = serviceName;
		this.servicePath = servicePath;
	}

	@Override
	public boolean testService() {
		return true;
	}

	@Override
	public void registerService(BundleContext context) {
		Hashtable<String, String> properties = new Hashtable<String, String>();
		properties.put("riena.remote", "true");
        properties.put("riena.remote.protocol", "hessian");
        properties.put("riena.remote.path", servicePath);
        
        serviceRegistration = context.registerService(serviceName, this, properties);
	}

	@Override
	public void unregisterService(BundleContext context) {
		if (serviceRegistration != null)	{
			serviceRegistration.unregister();
			serviceRegistration = null;
		}	
	}

}
