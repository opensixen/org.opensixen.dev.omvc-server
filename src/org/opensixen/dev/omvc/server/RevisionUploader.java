package org.opensixen.dev.omvc.server;

import java.util.Hashtable;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.opensixen.dev.omvc.interfaces.IRevisionUploader;
import org.opensixen.dev.omvc.model.Developer;
import org.opensixen.dev.omvc.model.Project;
import org.opensixen.dev.omvc.model.Revision;
import org.opensixen.dev.omvc.model.Script;
import org.opensixen.dev.omvc.util.HSession;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

public class RevisionUploader implements IRevisionUploader, IRienaServer {

	private Logger log = Logger.getLogger(getClass());
	
	private static ServiceRegistration serviceRegistration; 
	
	@Override
	public void registerService(BundleContext context) {
		Hashtable<String, String> properties = new Hashtable<String, String>();
		properties.put("riena.remote", "true");
        properties.put("riena.remote.protocol", "hessian");
        properties.put("riena.remote.path", IRevisionUploader.path);
        
        RevisionUploader uploader = new RevisionUploader();
        serviceRegistration = context.registerService(IRevisionUploader.class.getName(), uploader, properties);
	}

	@Override
	public void unregisterService(BundleContext context) {
		if (serviceRegistration != null)	{
			serviceRegistration.unregister();
			serviceRegistration = null;
		}		
	}

	@Override
	public int uploadRevison(Revision revision) {
		
		Criteria crit = HSession.getCriteria(Developer.class);
		crit.add(Restrictions.eq("developer_ID", 1));
		Developer dev = (Developer) crit.uniqueResult();
		revision.setDeveloper(dev);
		
		Session sess = HSession.getSession();
		sess.beginTransaction();
		try {
			sess.save(revision);
			for(Script script:revision.getScripts())	{
				script.setRevision(revision);
				sess.save(script);
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
	public List<Project> getProjects() {
		Criteria crit = HSession.getCriteria(Project.class);
		return crit.list();
	}
	
	@Override
	public boolean testService() {
		return true;
	}

}
