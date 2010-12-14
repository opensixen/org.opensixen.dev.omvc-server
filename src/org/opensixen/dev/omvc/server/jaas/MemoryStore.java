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

import java.security.Principal;
import java.util.Collection;
import java.util.HashMap;

import org.eclipse.riena.security.common.session.Session;
import org.eclipse.riena.security.sessionservice.ISessionStore;
import org.eclipse.riena.security.sessionservice.SessionEntry;

/**
 * Store for sessions in the memory (<code>HashMap</code>s)
 * 
 */
public class MemoryStore implements ISessionStore {

	private HashMap<String, SessionEntry> sessionTable = new HashMap<String, SessionEntry>();
	private HashMap<Principal, SessionList> userTable = new HashMap<Principal, SessionList>();

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.riena.security.sessionservice.ISessionStore#read(java.security
	 * .Principal)
	 */
	public synchronized Session[] read(Principal principal) {
		SessionList sl = userTable.get(principal);
		SessionEntry[] entries = sl.entries();
		Session[] sessions = new Session[entries.length];
		for (int i = 0; i < entries.length; i++) {
			sessions[i] = entries[i].getSession();
		}
		return sessions;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.riena.security.sessionservice.ISessionStore#read(org.eclipse
	 * .riena.security.common.session.Session)
	 */
	public synchronized SessionEntry read(Session session) {
		return sessionTable.get(session.getSessionId());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.riena.security.sessionservice.ISessionStore#write(org.eclipse
	 * .riena.security.sessionservice.SessionEntry)
	 */
	public synchronized void write(SessionEntry entry) {
		sessionTable.put(entry.getSession().getSessionId(), entry);
		for (Principal p : entry.getPrincipals().toArray(new Principal[entry.getPrincipals().size()])) {
			SessionList sl = userTable.get(p);
			if (sl == null) {
				sl = new SessionList();
			}
			sl.addEntry(entry);
			userTable.put(p, sl);

		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.riena.security.sessionservice.ISessionStore#delete(org.eclipse
	 * .riena.security.common.session.Session)
	 * 
	 * @pre session!=null
	 */
	public synchronized void delete(Session session) {
		// Assert.isTrue(session != null,"session must not be null" );

		SessionEntry entry = sessionTable.get(session.getSessionId());
		if (entry == null) {
			return;
		}
		sessionTable.remove(session.getSessionId());
		for (Principal p : entry.getPrincipals().toArray(new Principal[entry.getPrincipals().size()])) {
			SessionList sl = userTable.get(p);
			sl.removeEntry(session);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.riena.security.sessionservice.ISessionStore#delete(java.security
	 * .Principal)
	 */
	public synchronized void delete(Principal principal) {
		SessionList sl = userTable.get(principal);
		SessionEntry[] entries = sl.entries();
		for (int i = 0; i < entries.length; i++) {
			delete(entries[i].getSession());
		}
	}

	static class SessionList {
		private HashMap<String, SessionEntry> sessions = new HashMap<String, SessionEntry>();
		private final static SessionEntry[] EMPTY_SESSION_ENTRIES = new SessionEntry[0];

		/**
		 * adds a sesion entry
		 * 
		 * @param entry
		 *            session entry
		 */
		public void addEntry(SessionEntry entry) {
			sessions.put(entry.getSession().getSessionId(), entry);
		}

		/**
		 * removes the session entry for a session id
		 * 
		 * @param session
		 *            session id
		 */
		public void removeEntry(Session session) {
			sessions.remove(session.getSessionId());
		}

		/**
		 * returns the session entries
		 * 
		 * @return array of session entries
		 */
		public SessionEntry[] entries() {
			if (sessions.size() == 0) {
				return EMPTY_SESSION_ENTRIES;
			}
			Collection<SessionEntry> values = sessions.values();
			return values.toArray(new SessionEntry[values.size()]);
		}
	}
}
