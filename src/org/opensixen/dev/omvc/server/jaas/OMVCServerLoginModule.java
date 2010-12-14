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
package org.opensixen.dev.omvc.server.jaas;

import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.opensixen.dev.omvc.jaas.AnonymousPrincipal;
import org.opensixen.dev.omvc.jaas.DevPrincipal;
import org.opensixen.dev.omvc.model.Developer;
import org.opensixen.dev.omvc.util.HSession;


/**
 * 
 * 
 * @author Eloy Gomez
 * Indeos Consultoria http://www.indeos.es
 *
 */
public class OMVCServerLoginModule implements LoginModule {

	private Logger log = Logger.getLogger(getClass());
	
	private Subject subject;
	private CallbackHandler callbackHandler;
	private Map<String, ?> sharedState;
	private String username;
	private char[] password;
	private Map<String, ?> options;
	
	private boolean dev = false;

	public OMVCServerLoginModule() {
		
	}

	@Override
	public void initialize(Subject subject, CallbackHandler callbackHandler,
			Map<String, ?> sharedState, Map<String, ?> options) {
		this.subject = subject;
		this.callbackHandler = callbackHandler;
		this.sharedState = sharedState;
		this.options = options;

	}

	/* (non-Javadoc)
	 * @see javax.security.auth.spi.LoginModule#login()
	 */
	@Override
	public boolean login() throws LoginException {
		processCallbacks();
		
		if (loginDeveloper())	{
			return true;
		}
		
		// Ever return true
		return true;
	}

	
	/**
	 * check if logged user is repository admin
	 * @return
	 * @throws LoginException
	 */
	private boolean loginDeveloper() throws LoginException	{
		String pass = new String(password);
		
		log.info("Usuario: " + username + " Password: " + pass);
		
		// Look for developer info
		Criteria crit = HSession.getCriteria(Developer.class);
		crit.add(Restrictions.eq("userName", username));
		
		Developer developer = (Developer) crit.uniqueResult();
		
		if (developer == null)	 {
			log.warn("Invalid login: " + username);
			return false;
		}
		// TODO MD5 passwords
		if (developer.getPassword().equals(pass))	{
			dev = true;
		}
		
		else {
			log.warn("Invalid login: " + username);		
			return false;
		}
		return true;
		
	}
	
	/* (non-Javadoc)
	 * @see javax.security.auth.spi.LoginModule#commit()
	 */
	@Override
	public boolean commit() throws LoginException {
		if (dev == false)	{
			AnonymousPrincipal anon = new AnonymousPrincipal(username);
			subject.getPrincipals().add(anon);
		}
		
		else {
			DevPrincipal principal = new DevPrincipal(username);
			subject.getPrincipals().add(principal);
		}
		
		// Clean passwd
		for (int i = 0; i < password.length; i++)	{
				password[i] = ' ';
		}
		password = null;
		
		return true;
	}

	/* (non-Javadoc)
	 * @see javax.security.auth.spi.LoginModule#abort()
	 */
	@Override
	public boolean abort() throws LoginException {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see javax.security.auth.spi.LoginModule#logout()
	 */
	@Override
	public boolean logout() throws LoginException {
		// TODO Auto-generated method stub
		return false;
	}
	
	/**
	 * Get the username and password. This method does not return any value.
	 * Instead, it sets global name and password variables.
	 * 
	 * <p>
	 * Also note that this method will set the username and password values in
	 * the shared state in case subsequent LoginModules want to use them via
	 * use/tryFirstPass.
	 * 
	 * @param getPasswdFromSharedState
	 *            boolean that tells this method whether to retrieve the
	 *            password from the sharedState.
	 * @exception LoginException
	 *                if the username/password cannot be acquired.
	 */
	private void processCallbacks() throws LoginException {

		// prompt for a username and password
		if (callbackHandler == null)
			throw new LoginException("No CallbackHandler available " + "to acquire authentication information from the user");

		Callback[] callbacks = new Callback[2];
		callbacks[0] = new NameCallback("usuario");
		callbacks[1] = new PasswordCallback("passwd", false);

		try {
			 
			callbackHandler.handle(callbacks);
			
			username = ((NameCallback) callbacks[0]).getName();
			char[] tmpPassword = ((PasswordCallback) callbacks[1])
					.getPassword();
			password = new char[tmpPassword.length];
			System.arraycopy(tmpPassword, 0, password, 0, tmpPassword.length);
			((PasswordCallback) callbacks[1]).clearPassword();						

		} catch (java.io.IOException ioe) {
			throw new LoginException(ioe.toString());

		} catch (UnsupportedCallbackException uce) {
			throw new LoginException("Error: " + uce.getCallback().toString()
					+ " not available to acquire authentication information"
					+ " from the user");
		}
	}


}
