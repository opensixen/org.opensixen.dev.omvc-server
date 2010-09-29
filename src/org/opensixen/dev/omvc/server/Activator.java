package org.opensixen.dev.omvc.server;

import java.net.URL;
import java.util.ArrayList;

import org.eclipse.equinox.security.auth.ILoginContext;
import org.eclipse.equinox.security.auth.LoginContextFactory;
import org.eclipse.riena.core.RienaConstants;
import org.eclipse.riena.security.authorizationservice.IPermissionStore;
import org.eclipse.riena.security.sessionservice.ISessionStore;
import org.opensixen.dev.omvc.server.jaas.MemoryStore;
import org.opensixen.dev.omvc.server.jaas.PermissionStore;
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
	private static ILoginContext loginContext;
	private static BundleContext context;
	
	public static final String JAAS_CONFIG_FILE = "data/jaas_config.txt"; //$NON-NLS-1$
	
	private ServiceRegistration memoryStore;
	private ServiceRegistration filepermissionstore;
	
	public Activator()	{
		services.add(new OpensixenIDGenerator());
		services.add(new RemoteConsole());
		services.add(new RevisionDownloader());
	}
	
	@Override
	public void start(BundleContext context) throws Exception {	
		this.context = context;
		
		for (IRienaServer service: services)	{
			service.registerService(context);
		}
		// Setup authentication
		URL configUrl = Activator.getContext().getBundle().getEntry(Activator.JAAS_CONFIG_FILE);
		loginContext = LoginContextFactory.createContext("omvc", configUrl);
		
		// Create a ISessionStore
		memoryStore = getContext().registerService(ISessionStore.class.getName(), new MemoryStore(), RienaConstants.newDefaultServiceProperties());
		filepermissionstore = context.registerService(IPermissionStore.class.getName(), new PermissionStore(), RienaConstants.newDefaultServiceProperties());
	}

	@Override
	public void stop(BundleContext context) throws Exception {	
		for (IRienaServer service: services)	{
			service.unregisterService(context);
		}
	}

	
	public static BundleContext getContext()	{
		return context;
	}
	
	public static ILoginContext getLoginContext()	{
		return loginContext;
	}
}
