/**
 * 
 */
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
