package org.opensixen.dev.omvc.server;

import java.util.Hashtable;

import org.apache.log4j.Logger;
import org.eclipse.riena.security.common.authorization.Sentinel;
import org.opensixen.dev.omvc.interfaces.IRemoteCentralizedIDGenerator;
import org.opensixen.dev.omvc.jaas.OMVCPermission;
import org.opensixen.dev.omvc.jaas.PermissionFactory;
import org.opensixen.dev.omvc.model.Sequence;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

public class OpensixenIDGenerator implements IRemoteCentralizedIDGenerator, IRienaServer {

	private Logger log = Logger.getLogger(getClass());
	
	private static ServiceRegistration serviceRegistration; 
	
	@Override
	public int getNextID(String tableName) {
		if (!Sentinel.checkAccess(PermissionFactory.get(OMVCPermission.PERM_GETID)))	{
			throw new SecurityException("Not privileges.");
		}
		Sequence seq = Sequence.getSequence(tableName);
		return seq.getNext_ID();
	}

	@Override
	public void registerService(BundleContext context) {
			Hashtable<String, String> properties = new Hashtable<String, String>();
			properties.put("riena.remote", "true");
	        properties.put("riena.remote.protocol", "hessian");
	        properties.put("riena.remote.path", IRemoteCentralizedIDGenerator.path);
	        
	        OpensixenIDGenerator generator = new OpensixenIDGenerator();
	        serviceRegistration = context.registerService(IRemoteCentralizedIDGenerator.class.getName(), generator, properties);
		
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
