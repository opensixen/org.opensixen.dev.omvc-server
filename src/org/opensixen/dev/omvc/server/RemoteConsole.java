 /******* BEGIN LICENSE BLOCK *****
 * Versión: GPL 2.0/CDDL 1.0/EPL 1.0
 *
 * Los contenidos de este fichero están sujetos a la Licencia
 * Pública General de GNU versión 2.0 (la "Licencia"); no podrá
 * usar este fichero, excepto bajo las condiciones que otorga dicha 
 * Licencia y siempre de acuerdo con el contenido de la presente. 
 * Una copia completa de las condiciones de de dicha licencia,
 * traducida en castellano, deberá estar incluida con el presente
 * programa.
 * 
 * Adicionalmente, puede obtener una copia de la licencia en
 * http://www.gnu.org/licenses/gpl-2.0.html
 *
 * Este fichero es parte del programa opensiXen.
 *
 * OpensiXen es software libre: se puede usar, redistribuir, o
 * modificar; pero siempre bajo los términos de la Licencia 
 * Pública General de GNU, tal y como es publicada por la Free 
 * Software Foundation en su versión 2.0, o a su elección, en 
 * cualquier versión posterior.
 *
 * Este programa se distribuye con la esperanza de que sea útil,
 * pero SIN GARANTÍA ALGUNA; ni siquiera la garantía implícita 
 * MERCANTIL o de APTITUD PARA UN PROPÓSITO DETERMINADO. Consulte 
 * los detalles de la Licencia Pública General GNU para obtener una
 * información más detallada. 
 *
 * TODO EL CÓDIGO PUBLICADO JUNTO CON ESTE FICHERO FORMA PARTE DEL 
 * PROYECTO OPENSIXEN, PUDIENDO O NO ESTAR GOBERNADO POR ESTE MISMO
 * TIPO DE LICENCIA O UNA VARIANTE DE LA MISMA.
 *
 * El desarrollador/es inicial/es del código es
 *  FUNDESLE (Fundación para el desarrollo del Software Libre Empresarial).
 *  Indeos Consultoria S.L. - http://www.indeos.es
 *
 * Contribuyente(s):
 *  Eloy Gómez García <eloy@opensixen.org> 
 *
 * Alternativamente, y a elección del usuario, los contenidos de este
 * fichero podrán ser usados bajo los términos de la Licencia Común del
 * Desarrollo y la Distribución (CDDL) versión 1.0 o posterior; o bajo
 * los términos de la Licencia Pública Eclipse (EPL) versión 1.0. Una 
 * copia completa de las condiciones de dichas licencias, traducida en 
 * castellano, deberán de estar incluidas con el presente programa.
 * Adicionalmente, es posible obtener una copia original de dichas 
 * licencias en su versión original en
 *  http://www.opensource.org/licenses/cddl1.php  y en  
 *  http://www.opensource.org/licenses/eclipse-1.0.php
 *
 * Si el usuario desea el uso de SU versión modificada de este fichero 
 * sólo bajo los términos de una o más de las licencias, y no bajo los 
 * de las otra/s, puede indicar su decisión borrando las menciones a la/s
 * licencia/s sobrantes o no utilizadas por SU versión modificada.
 *
 * Si la presente licencia triple se mantiene íntegra, cualquier usuario 
 * puede utilizar este fichero bajo cualquiera de las tres licencias que 
 * lo gobiernan,  GPL 2.0/CDDL 1.0/EPL 1.0.
 *
 * ***** END LICENSE BLOCK ***** */

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


/**
 * 
 * RemoteConsole 
 *
 * @author Eloy Gomez
 * Indeos Consultoria http://www.indeos.es
 */
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
