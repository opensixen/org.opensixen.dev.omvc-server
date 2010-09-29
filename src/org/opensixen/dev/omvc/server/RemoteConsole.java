package org.opensixen.dev.omvc.server;

import java.net.URL;
import java.security.Permissions;
import java.security.Principal;
import java.security.acl.Permission;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import javax.management.MBeanPermission;
import javax.security.auth.callback.*;
import javax.security.auth.login.LoginException;

import org.apache.log4j.Logger;
import org.eclipse.equinox.security.auth.ILoginContext;
import org.eclipse.equinox.security.auth.LoginContextFactory;
import org.eclipse.riena.internal.security.common.SentinelServiceImpl;
import org.eclipse.riena.security.common.authorization.PermissionClassFactory;
import org.eclipse.riena.security.common.authorization.Sentinel;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.opensixen.dev.omvc.interfaces.IPO;
import org.opensixen.dev.omvc.interfaces.IRemoteCentralizedIDGenerator;
import org.opensixen.dev.omvc.interfaces.IRemoteConsole;
import org.opensixen.dev.omvc.jaas.OMVCPermission;
import org.opensixen.dev.omvc.jaas.PermissionFactory;
import org.opensixen.dev.omvc.model.Developer;
import org.opensixen.dev.omvc.model.Project;
import org.opensixen.dev.omvc.model.Revision;
import org.opensixen.dev.omvc.model.Script;
import org.opensixen.dev.omvc.util.HSession;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

public class RemoteConsole implements IRemoteConsole, IRienaServer {

	private Logger log = Logger.getLogger(getClass());
	
	private static ServiceRegistration serviceRegistration; 
	
	
	
	/**
	 * Save any record into DB
	 */
	@Override
	public boolean save(IPO po) {
		if (!Sentinel.checkAccess(PermissionFactory.get(OMVCPermission.PERM_SAVEPO)))	{
			throw new SecurityException("Not privileges.");
		}
		
		return HSession.save(po);
	}

	@Override
	public  <T extends IPO> T load(Class<T> clazz, int id) {
		
		if (!Sentinel.checkAccess(PermissionFactory.get(OMVCPermission.PERM_LOADPO)))	{
			throw new SecurityException("Not enough privileges.");
		}
		
		Criteria crit = HSession.getCriteria(clazz);
		crit.add(Restrictions.idEq(id));
		return  (T) crit.uniqueResult();
	}

	@Override
	public <T extends IPO>ArrayList<T> getAll(Class<T> clazz) {
		if (!Sentinel.checkAccess(PermissionFactory.get(OMVCPermission.PERM_LISTPO)))	{
			throw new SecurityException("Not enough privileges.");
		}
		
		Criteria crit = HSession.getCriteria(clazz);
		return new ArrayList<T>(crit.list());
	}

	@Override
	public List<Project> getProjects() {
		Criteria crit = HSession.getCriteria(Project.class);
		return crit.list();
	}
	
	

	/* (non-Javadoc)
	 * @see org.opensixen.dev.omvc.interfaces.IRemoteConsole#getRevisions()
	 */
	@Override
	public List<Revision> getRevisions() {
		if (!Sentinel.checkAccess(PermissionFactory.get(OMVCPermission.PERM_LISTREV)))	{
			throw new SecurityException("Not enough privileges.");
		}
		
		Criteria crit = HSession.getCriteria(Revision.class);
		crit.addOrder(Order.desc("revision_ID"));
		return crit.list();
	}
	
	@Override
	public boolean testService() {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.opensixen.dev.omvc.interfaces.IRemoteConsole#getScripts(org.opensixen.dev.omvc.model.Revision)
	 */
	@Override
	public List<Script> getScripts(Revision revision) {
		Criteria crit = HSession.getCriteria(Script.class);
		crit.add(Restrictions.eq("revision", revision));
		return crit.list();
	}

	/* (non-Javadoc)
	 * @see org.opensixen.dev.omvc.interfaces.IRemoteConsole#uploadRevison(org.opensixen.dev.omvc.model.Revision)
	 */
	@Override
	public int uploadRevison(Revision revision) {
	
		if (!Sentinel.checkAccess(PermissionFactory.get(OMVCPermission.PERM_SAVEREV)))	{
			throw new SecurityException("Not enough privileges.");
		}
		
		if (revision.getDeveloper() == null)	{
			Criteria crit = HSession.getCriteria(Developer.class);
			crit.add(Restrictions.eq("developer_ID", 1));
			Developer dev = (Developer) crit.uniqueResult();
			revision.setDeveloper(dev);
			revision.setCreated(new Date());
		}
		
		
		Session sess = HSession.getSession();
		sess.beginTransaction();
		try {
			sess.saveOrUpdate(revision);
			for(Script script:revision.getScripts())	{
				script.setRevision(revision);
				sess.saveOrUpdate(script);
			}
			
			sess.getTransaction().commit();
		}
		catch (HibernateException e)	{
			sess.getTransaction().rollback();
			log.error("Error guardando la revision");
			return -1;
		}
		log.info("Revision guardada con exito.");
		return revision.getRevision_ID();

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

	
}
