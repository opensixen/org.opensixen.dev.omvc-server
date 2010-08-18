package org.opensixen.dev.omvc.server;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.opensixen.dev.omvc.interfaces.IPO;
import org.opensixen.dev.omvc.interfaces.IRemoteCentralizedIDGenerator;
import org.opensixen.dev.omvc.interfaces.IRemoteConsole;
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
	public List<Project> getProjects() {
		Criteria crit = HSession.getCriteria(Project.class);
		return crit.list();
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

	

	/* (non-Javadoc)
	 * @see org.opensixen.dev.omvc.interfaces.IRemoteConsole#getRevisions()
	 */
	@Override
	public List<Revision> getRevisions() {
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
		if (revision.getDeveloper() == null)	{
			Criteria crit = HSession.getCriteria(Developer.class);
			crit.add(Restrictions.eq("developer_ID", 1));
			Developer dev = (Developer) crit.uniqueResult();
			revision.setDeveloper(dev);
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

}
