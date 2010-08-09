package org.opensixen.dev.omvc.server;

import java.util.ArrayList;
import java.util.Hashtable;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.opensixen.dev.omvc.interfaces.IPO;
import org.opensixen.dev.omvc.interfaces.IRemoteCentralizedIDGenerator;
import org.opensixen.dev.omvc.interfaces.IRemoteConsole;
import org.opensixen.dev.omvc.util.HSession;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

public class RemoteConsole implements IRemoteConsole, IRienaServer {

	private Logger log = Logger.getLogger(getClass());
	
	private static ServiceRegistration serviceRegistration; 
	
	@Override
	public boolean save(IPO po) {
		return HSession.save(po);
	}

	@Override
	public  <T extends IPO> T load(Class<T> clazz, int id) {
		Criteria crit = HSession.getCriteria(clazz);
		crit.add(Restrictions.idEq(id));
		return  (T) crit.uniqueResult();
	}

	@Override
	public <T extends IPO>ArrayList<T> getAll(Class<T> clazz) {
		Criteria crit = HSession.getCriteria(clazz);
		return new ArrayList<T>(crit.list());
	}

	@Override
	public void registerService(BundleContext context) {
		Hashtable<String, String> properties = new Hashtable<String, String>();
		properties.put("riena.remote", "true");
        properties.put("riena.remote.protocol", "hessian");
        properties.put("riena.remote.path", IRemoteConsole.path);
        
        RemoteConsole console = new RemoteConsole();
        serviceRegistration = context.registerService(IRemoteConsole.class.getName(), console, properties);
		
	}

	@Override
	public void unregisterService(BundleContext context) {
		if (serviceRegistration != null)	{
			serviceRegistration.unregister();
			serviceRegistration = null;
		}
	}

	@Override
	public boolean testService() {
		return true;
	}
}
