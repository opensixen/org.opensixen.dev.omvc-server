package org.opensixen.dev.omvc.server;

import org.osgi.framework.BundleContext;

public interface IRienaServer {
	
	public void registerService(BundleContext context);
	public void unregisterService(BundleContext context);

}
