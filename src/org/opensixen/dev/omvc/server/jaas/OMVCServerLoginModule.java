/**
 * 
 */
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
import org.opensixen.dev.omvc.jaas.AnonymousPrincipal;
import org.opensixen.dev.omvc.jaas.DevPrincipal;


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
		log.info("Usuario: " + username + " Password: " + password.toString());
		
		if (username.equals("indeos"))	{
			dev = true;
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
