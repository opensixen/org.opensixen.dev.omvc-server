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
